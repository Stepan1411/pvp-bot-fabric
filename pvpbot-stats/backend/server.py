#!/usr/bin/env python3
"""
Backend для сбора статистики PVPBOT
Хранение данных в локальных файлах + GitHub backup
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import time
import os
import subprocess
from datetime import datetime
from pathlib import Path
from threading import Thread, Lock
import atexit
from collections import deque

app = Flask(__name__)
CORS(app)

# Логи в памяти (последние 500 строк)
log_buffer = deque(maxlen=500)

def log(message):
    """Логирует сообщение в консоль и в буфер"""
    timestamp = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')
    log_line = f"[{timestamp}] {message}"
    print(log_line)
    log_buffer.append(log_line)

# GitHub настройки
GITHUB_REPO = "https://github.com/Stepan1411/pvpbot-stats-data.git"
GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN', '')  # Personal Access Token
GITHUB_BRANCH = "main"

# Папка для данных - используем абсолютный путь
DATA_DIR = Path(os.path.abspath('./data'))

# Инициализация Git репозитория
def init_git_repo():
    """Инициализирует Git репозиторий для данных"""
    try:
        if not DATA_DIR.exists():
            log("[GIT] Data directory doesn't exist, creating...")
            DATA_DIR.mkdir(parents=True, exist_ok=True)
            
            # Пробуем клонировать репозиторий
            log("[GIT] Cloning data repository...")
            repo_url = GITHUB_REPO.replace('https://', f'https://{GITHUB_TOKEN}@') if GITHUB_TOKEN else GITHUB_REPO
            result = subprocess.run(['git', 'clone', repo_url, str(DATA_DIR)], 
                                  capture_output=True, text=True, timeout=30)
            if result.returncode == 0:
                log(f"[GIT] Repository cloned successfully")
            else:
                log(f"[GIT] Clone failed: {result.stderr}")
                log(f"[GIT] Will continue with local storage only")
        else:
            log("[GIT] Data directory exists, checking Git status...")
            
            # Проверяем, является ли это Git репозиторием
            git_dir = DATA_DIR / '.git'
            if not git_dir.exists():
                log("[GIT] Not a git repository, initializing...")
                subprocess.run(['git', '-C', str(DATA_DIR), 'init'], 
                              capture_output=True, text=True, timeout=10)
                
                # Добавляем remote
                repo_url = GITHUB_REPO.replace('https://', f'https://{GITHUB_TOKEN}@') if GITHUB_TOKEN else GITHUB_REPO
                subprocess.run(['git', '-C', str(DATA_DIR), 'remote', 'add', 'origin', repo_url], 
                              capture_output=True, text=True, timeout=5)
                
                log("[GIT] Git repository initialized")
            else:
                # Настраиваем стратегию pull (rebase)
                subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'pull.rebase', 'true'], 
                              capture_output=True, text=True, timeout=5)
                
                # Пробуем пулить последние изменения
                log("[GIT] Pulling latest changes...")
                result = subprocess.run(['git', '-C', str(DATA_DIR), 'pull', '--rebase'], 
                                      capture_output=True, text=True, timeout=30)
                if result.returncode == 0:
                    log(f"[GIT] Pulled latest changes")
                else:
                    # Если pull не удался, просто логируем но НЕ удаляем данные
                    log(f"[GIT] Pull failed: {result.stderr}")
                    log(f"[GIT] Will continue with local data")
                    
                    # Пробуем fetch чтобы обновить remote refs
                    subprocess.run(['git', '-C', str(DATA_DIR), 'fetch', 'origin'], 
                                  capture_output=True, text=True, timeout=30)
    except Exception as e:
        log(f"[GIT] Error initializing repository: {e}")
        log(f"[GIT] Will continue with local storage")
        # Создаём папку если её нет
        DATA_DIR.mkdir(parents=True, exist_ok=True)

# Инициализируем Git репозиторий при импорте
init_git_repo()

# Файлы данных
SERVERS_FILE = DATA_DIR / "servers.json"
GLOBAL_HISTORY_FILE = DATA_DIR / "global_history.json"
GLOBAL_STATS_FILE = DATA_DIR / "global_stats.json"

def get_server_history_file(server_id):
    """Возвращает путь к файлу истории сервера"""
    return DATA_DIR / f"server_{server_id}.json"

def git_commit_and_push():
    """Делает commit и push изменений в GitHub"""
    # Используем блокировку чтобы только один процесс мог делать Git операции
    if not git_lock.acquire(blocking=False):
        log("[GIT] Another git operation in progress, skipping")
        return False
    
    try:
        # Удаляем lock файл если он существует (остался от прерванного процесса)
        lock_file = DATA_DIR / '.git' / 'index.lock'
        if lock_file.exists():
            try:
                lock_file.unlink()
                log("[GIT] Removed stale index.lock file")
            except Exception as e:
                log(f"[GIT] Failed to remove lock file: {e}")
        
        # Проверяем и настраиваем Git конфигурацию
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.email', 'bot@pvpbot.stats'], 
                      capture_output=True, text=True, timeout=5)
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.name', 'PVPBOT Stats Bot'], 
                      capture_output=True, text=True, timeout=5)
        
        # Проверяем и настраиваем remote origin
        repo_url = GITHUB_REPO.replace('https://', f'https://{GITHUB_TOKEN}@') if GITHUB_TOKEN else GITHUB_REPO
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'remote', 'get-url', 'origin'], 
                              capture_output=True, text=True, timeout=5)
        
        if result.returncode != 0:
            # Origin не существует, добавляем
            log("[GIT] Adding origin remote...")
            subprocess.run(['git', '-C', str(DATA_DIR), 'remote', 'add', 'origin', repo_url], 
                          capture_output=True, text=True, timeout=5)
        else:
            # Origin существует, обновляем URL
            subprocess.run(['git', '-C', str(DATA_DIR), 'remote', 'set-url', 'origin', repo_url], 
                          capture_output=True, text=True, timeout=5)
        
        # Проверяем, есть ли изменения
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'status', '--porcelain'], 
                              capture_output=True, text=True, timeout=10)
        
        if not result.stdout.strip():
            log("[GIT] No changes to commit")
            return True
        
        # Логируем какие файлы изменились
        changed_files = result.stdout.strip().split('\n')
        log(f"[GIT] Changed files: {', '.join([f.strip() for f in changed_files])}")
        
        # Проверяем что важные файлы существуют
        important_files = ['servers.json', 'global_stats.json', 'global_history.json']
        for filename in important_files:
            filepath = DATA_DIR / filename
            if filepath.exists():
                log(f"[GIT] ✓ {filename} exists ({filepath.stat().st_size} bytes)")
            else:
                log(f"[GIT] ✗ {filename} MISSING!")
        
        # Добавляем все файлы
        subprocess.run(['git', '-C', str(DATA_DIR), 'add', '.'], 
                      capture_output=True, text=True, timeout=10)
        
        # Коммитим
        timestamp = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S UTC')
        commit_msg = f"Auto-save data: {timestamp}"
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'commit', '-m', commit_msg], 
                              capture_output=True, text=True, timeout=10)
        
        if result.returncode != 0:
            log(f"[GIT] Commit failed: {result.stderr}")
            return False
        
        # Пробуем пушить
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'push', 'origin', GITHUB_BRANCH], 
                              capture_output=True, text=True, timeout=30)
        
        if result.returncode == 0:
            log(f"[GIT] Successfully pushed to GitHub")
            return True
        else:
            # Проверяем, не повреждён ли репозиторий
            if 'index-pack failed' in result.stderr or 'did not receive expected object' in result.stderr:
                log(f"[GIT] Repository corrupted: {result.stderr}")
                log(f"[GIT] Auto-fix is disabled. Please manually fix or contact admin.")
                return False
            
            # Если push отклонён из-за удалённых изменений
            if 'rejected' in result.stderr or 'fetch first' in result.stderr:
                log(f"[GIT] Push rejected, fetching and rebasing...")
                
                # Fetch изменения
                subprocess.run(['git', '-C', str(DATA_DIR), 'fetch', 'origin'], 
                              capture_output=True, text=True, timeout=30)
                
                # Rebase с автоматическим разрешением конфликтов (наши изменения приоритетнее)
                subprocess.run(['git', '-C', str(DATA_DIR), 'rebase', '-X', 'ours', f'origin/{GITHUB_BRANCH}'], 
                              capture_output=True, text=True, timeout=30)
                
                # Повторный push
                result = subprocess.run(['git', '-C', str(DATA_DIR), 'push', 'origin', GITHUB_BRANCH], 
                                      capture_output=True, text=True, timeout=30)
                
                if result.returncode == 0:
                    log(f"[GIT] Successfully pushed after rebase")
                    return True
                else:
                    log(f"[GIT] Push failed after rebase: {result.stderr}")
                    return False
            
            # Другие ошибки
            log(f"[GIT] Push failed: {result.stderr}")
            return False
            
    except subprocess.TimeoutExpired:
        log("[GIT] Git operation timed out")
        return False
    except Exception as e:
        log(f"[GIT] Error during git commit/push: {e}")
        return False
    finally:
        git_lock.release()
        log("[GIT] Released git lock")

def fix_corrupted_repo():
    """Исправляет повреждённый Git репозиторий - ОТКЛЮЧЕНО для безопасности"""
    log("[GIT] CRITICAL: Repository corruption detected!")
    log("[GIT] Auto-fix is DISABLED to prevent data loss")
    log("[GIT] Please manually fix the repository or contact admin")
    log("[GIT] Data files are preserved in the data/ directory")
    return False

# Пароль для админки
ADMIN_PASSWORD = os.environ.get('ADMIN_PASSWORD', '')

# Хранилище
servers = {}
history = {
    "timestamps": [],
    "servers": [],
    "bots": [],
    "spawned": [],
    "killed": []
}

# Глобальные счетчики (не сбрасываются)
global_stats = {
    "total_spawned": 0,
    "total_killed": 0
}

# Блокировки
data_lock = Lock()
git_lock = Lock()  # Блокировка для Git операций
background_thread = None
stop_background = False
initialized = False
force_backup = False  # Флаг для принудительного backup

def load_data():
    """Загружает данные серверов из файла"""
    try:
        if SERVERS_FILE.exists():
            with open(SERVERS_FILE, 'r') as f:
                return json.load(f), load_global_stats()
    except Exception as e:
        log(f"[DATA] Failed to load servers: {e}")
    return {}, {"total_spawned": 0, "total_killed": 0}

def save_data():
    """Сохраняет данные серверов в файл"""
    try:
        with open(SERVERS_FILE, 'w') as f:
            json.dump(servers, f, indent=2)
    except Exception as e:
        log(f"[DATA] Failed to save servers: {e}")

def load_global_stats():
    """Загружает глобальные счетчики"""
    try:
        if GLOBAL_STATS_FILE.exists():
            with open(GLOBAL_STATS_FILE, 'r') as f:
                return json.load(f)
    except Exception as e:
        log(f"[DATA] Failed to load global stats: {e}")
    return {"total_spawned": 0, "total_killed": 0}

def save_global_stats():
    """Сохраняет глобальные счетчики"""
    try:
        with open(GLOBAL_STATS_FILE, 'w') as f:
            json.dump(global_stats, f, indent=2)
        log(f"[DATA] Saved global stats: spawned={global_stats['total_spawned']}, killed={global_stats['total_killed']}")
    except Exception as e:
        log(f"[DATA] Failed to save global stats: {e}")

def load_history():
    """Загружает глобальную историю из файла"""
    global history
    try:
        if GLOBAL_HISTORY_FILE.exists():
            with open(GLOBAL_HISTORY_FILE, 'r') as f:
                history = json.load(f)
                log(f"[HISTORY] Loaded {len(history['timestamps'])} points")
                cleanup_old_history()
    except Exception as e:
        log(f"[HISTORY] Failed to load history: {e}")

def save_history():
    """Сохраняет глобальную историю в файл"""
    try:
        with open(GLOBAL_HISTORY_FILE, 'w') as f:
            json.dump(history, f, indent=2)
    except Exception as e:
        log(f"[HISTORY] Failed to save history: {e}")

def load_server_history(server_id):
    """Загружает историю конкретного сервера"""
    try:
        history_file = get_server_history_file(server_id)
        if history_file.exists():
            with open(history_file, 'r') as f:
                return json.load(f)
    except Exception as e:
        log(f"[HISTORY] Failed to load server history for {server_id}: {e}")
    return {"timestamps": [], "bots": [], "players": []}

def save_server_history(server_id, history_data):
    """Сохраняет историю конкретного сервера"""
    try:
        history_file = get_server_history_file(server_id)
        with open(history_file, 'w') as f:
            json.dump(history_data, f, indent=2)
        log(f"[DATA] Saved server history to {history_file.name} ({len(history_data['timestamps'])} points)")
    except Exception as e:
        log(f"[HISTORY] Failed to save server history for {server_id}: {e}")

def cleanup_old_history():
    """Удаляет данные старше 1 года"""
    one_year_ago = time.time() - (365 * 24 * 60 * 60)
    valid_indices = [i for i, ts in enumerate(history['timestamps']) if ts > one_year_ago]
    
    if len(valid_indices) < len(history['timestamps']):
        history['timestamps'] = [history['timestamps'][i] for i in valid_indices]
        history['servers'] = [history['servers'][i] for i in valid_indices]
        history['bots'] = [history['bots'][i] for i in valid_indices]
        history['spawned'] = [history['spawned'][i] for i in valid_indices]
        history['killed'] = [history['killed'][i] for i in valid_indices]
        log(f"[HISTORY] Cleaned up old data, {len(valid_indices)} points remaining")

def cleanup_server_history(server_id, history_data):
    """Удаляет данные сервера старше 7 дней"""
    seven_days_ago = time.time() - (7 * 24 * 60 * 60)
    valid_indices = [i for i, ts in enumerate(history_data['timestamps']) if ts > seven_days_ago]
    
    if len(valid_indices) < len(history_data['timestamps']):
        history_data['timestamps'] = [history_data['timestamps'][i] for i in valid_indices]
        history_data['bots'] = [history_data['bots'][i] for i in valid_indices]
        history_data['players'] = [history_data['players'][i] for i in valid_indices]
    return history_data

def add_to_history(stats):
    """Добавляет точку в глобальную историю"""
    with data_lock:
        current_time = time.time()
        
        history['timestamps'].append(current_time)
        history['servers'].append(stats['servers_online'])
        history['bots'].append(stats['bots_active'])
        history['spawned'].append(stats['bots_spawned_total'])
        history['killed'].append(stats['bots_killed_total'])
        
        # Ограничиваем размер
        max_points = 100000
        if len(history['timestamps']) > max_points:
            history['timestamps'] = history['timestamps'][-max_points:]
            history['servers'] = history['servers'][-max_points:]
            history['bots'] = history['bots'][-max_points:]
            history['spawned'] = history['spawned'][-max_points:]
            history['killed'] = history['killed'][-max_points:]
        
        # Сохраняем при каждом добавлении
        save_history()

def add_to_server_history(server_id, bots_count, players_count):
    """Добавляет точку в историю конкретного сервера"""
    current_time = time.time()
    history_data = load_server_history(server_id)
    
    history_data['timestamps'].append(current_time)
    history_data['bots'].append(bots_count)
    history_data['players'].append(players_count)
    
    # Очищаем старые данные (7 дней)
    history_data = cleanup_server_history(server_id, history_data)
    
    # Сохраняем
    save_server_history(server_id, history_data)
    log(f"[HISTORY] Added server history point for {server_id[:8]}... - Bots: {bots_count}, Players: {players_count}, Total points: {len(history_data['timestamps'])}")

def get_stats():
    """Возвращает текущую статистику"""
    current_time = time.time()
    # Сервер считается активным если отправлял данные в последние 10 секунд (2 пропущенных пакета)
    active_servers = {
        sid: data for sid, data in servers.items()
        if current_time - data['last_seen'] < 10
    }
    
    servers_online = len(active_servers)
    bots_active = sum(data['bots_count'] for data in active_servers.values())
    
    # Используем глобальные счетчики вместо суммы по серверам
    bots_spawned_total = global_stats['total_spawned']
    bots_killed_total = global_stats['total_killed']
    
    return {
        "servers_online": servers_online,
        "bots_active": bots_active,
        "bots_spawned_total": bots_spawned_total,
        "bots_killed_total": bots_killed_total,
        "total_downloads": 0,
        "mod_version": "1.0.0",
        "last_update": datetime.utcnow().isoformat() + "Z",
        "servers": [
            {
                "id": sid[:8] + "...",
                "bots": data['bots_count'],
                "last_seen": datetime.fromtimestamp(data['last_seen']).isoformat() + "Z"
            }
            for sid, data in active_servers.items()
        ]
    }

@app.route('/api/stats', methods=['POST'])
def receive_stats():
    """Принимает статистику от серверов"""
    try:
        data = request.json
        
        if not data or 'server_id' not in data:
            return jsonify({"error": "Invalid data"}), 400
        
        server_id = data['server_id']
        
        with data_lock:
            # Инициализируем сервер если новый
            if server_id not in servers:
                servers[server_id] = {
                    'bots_spawned_total': 0,
                    'bots_killed_total': 0,
                    'first_seen': time.time()
                }
            
            # Обновляем глобальные счетчики (только если значения увеличились)
            old_spawned = servers[server_id].get('bots_spawned_total', 0)
            old_killed = servers[server_id].get('bots_killed_total', 0)
            new_spawned = data.get('bots_spawned_total', 0)
            new_killed = data.get('bots_killed_total', 0)
            
            if new_spawned > old_spawned:
                global_stats['total_spawned'] += (new_spawned - old_spawned)
            if new_killed > old_killed:
                global_stats['total_killed'] += (new_killed - old_killed)
            
            # Обновляем данные сервера
            servers[server_id].update({
                'bots_count': data.get('bots_count', 0),
                'real_players_count': data.get('real_players_count', 0),
                'total_players_count': data.get('total_players_count', 0),
                'bots_spawned_total': new_spawned,
                'bots_killed_total': new_killed,
                'mod_version': data.get('mod_version', 'unknown'),
                'minecraft_version': data.get('minecraft_version', 'unknown'),
                'bots_list': data.get('bots_list', []),
                'players_list': data.get('players_list', []),
                'server_core': data.get('server_core', 'Unknown'),
                'last_seen': time.time()
            })
            
            save_data()
            save_global_stats()
        
        # Добавляем точку в глобальную историю при каждом запросе (каждые 5 секунд)
        stats = get_stats()
        add_to_history(stats)
        
        # Добавляем точку в историю сервера при каждом запросе (каждые 5 секунд)
        add_to_server_history(server_id, data.get('bots_count', 0), data.get('real_players_count', 0))
        
        log(f"[STATS] Received from {server_id[:8]}... - Bots: {data.get('bots_count', 0)}, Players: {data.get('real_players_count', 0)}")
        
        return jsonify({"success": True}), 200
        
    except Exception as e:
        log(f"Error in receive_stats: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/stats', methods=['GET'])
def get_stats_endpoint():
    """Возвращает текущую статистику"""
    try:
        stats = get_stats()
        return jsonify(stats), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/history', methods=['GET'])
def get_history_endpoint():
    """Возвращает историю для графиков"""
    try:
        with data_lock:
            return jsonify(history), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    """Health check"""
    return jsonify({
        "status": "ok",
        "data_points": len(history['timestamps']),
        "servers": len(servers),
        "global_stats": global_stats
    }), 200

@app.route('/api/logs', methods=['GET'])
def get_logs():
    """Возвращает последние логи"""
    try:
        lines = request.args.get('lines', 100, type=int)
        lines = min(lines, 500)  # Максимум 500 строк
        
        logs_list = list(log_buffer)
        if lines < len(logs_list):
            logs_list = logs_list[-lines:]
        
        return jsonify({
            "logs": logs_list,
            "total_lines": len(log_buffer),
            "returned_lines": len(logs_list)
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/servers', methods=['GET'])
def get_admin_servers():
    """Возвращает детальную информацию о серверах для админки"""
    try:
        # Проверяем пароль
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        current_time = time.time()
        servers_list = []
        
        for server_id, data in servers.items():
            is_online = (current_time - data['last_seen']) < 10
            
            servers_list.append({
                'id': server_id,
                'id_short': server_id[:8] + '...',
                'is_online': is_online,
                'bots_count': data.get('bots_count', 0),
                'real_players_count': data.get('real_players_count', 0),
                'total_players_count': data.get('total_players_count', 0),
                'bots_spawned_total': data.get('bots_spawned_total', 0),
                'bots_killed_total': data.get('bots_killed_total', 0),
                'mod_version': data.get('mod_version', 'unknown'),
                'minecraft_version': data.get('minecraft_version', 'unknown'),
                'first_seen': data.get('first_seen', current_time),
                'last_seen': data['last_seen'],
                'last_seen_ago': int(current_time - data['last_seen']),
                'uptime': format_uptime(current_time - data.get('first_seen', data['last_seen'])),
                'uptime_seconds': current_time - data.get('first_seen', data['last_seen'])
            })
        
        # Сортируем: онлайн сначала (по uptime), потом офлайн (по last_seen)
        servers_list.sort(key=lambda x: (not x['is_online'], -x['uptime_seconds'] if x['is_online'] else -x['last_seen']))
        
        return jsonify({
            'servers': servers_list,
            'total_servers': len(servers_list),
            'online_servers': sum(1 for s in servers_list if s['is_online'])
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/server/<server_id>', methods=['GET'])
def get_server_details(server_id):
    """Возвращает детальную информацию о конкретном сервере"""
    try:
        # Проверяем пароль
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        if server_id not in servers:
            return jsonify({"error": "Server not found"}), 404
        
        data = servers[server_id]
        
        return jsonify({
            'server_id': server_id,
            'bots': data.get('bots_list', []),
            'players': data.get('players_list', []),
            'server_core': data.get('server_core', 'Unknown'),
            'minecraft_version': data.get('minecraft_version', 'unknown'),
            'mod_version': data.get('mod_version', 'unknown'),
            'first_seen': data.get('first_seen', 0),
            'last_seen': data['last_seen']
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/server/<server_id>/history', methods=['GET'])
def get_server_history_endpoint(server_id):
    """Возвращает историю конкретного сервера для графиков"""
    try:
        # Проверяем пароль
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        if server_id not in servers:
            return jsonify({"error": "Server not found"}), 404
        
        history_data = load_server_history(server_id)
        log(f"[API] Returning history for {server_id[:8]}... - {len(history_data.get('timestamps', []))} points")
        return jsonify(history_data), 200
    except Exception as e:
        log(f"[API] Error getting server history: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/auth', methods=['POST'])
def admin_auth():
    """Проверяет пароль админки"""
    try:
        data = request.json
        password = data.get('password', '')
        
        if password == ADMIN_PASSWORD:
            return jsonify({"success": True}), 200
        else:
            return jsonify({"error": "Invalid password"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/test-admin', methods=['GET'])
def test_admin():
    """Тестовый эндпоинт для проверки"""
    # Проверяем существование файлов
    files_status = {}
    for filename in ['servers.json', 'global_stats.json', 'global_history.json']:
        filepath = DATA_DIR / filename
        files_status[filename] = {
            'exists': filepath.exists(),
            'size': filepath.stat().st_size if filepath.exists() else 0
        }
    
    return jsonify({
        "message": "Admin route is working",
        "cwd": str(Path.cwd()),
        "file_location": str(Path(__file__).parent),
        "admin_html_path": str(Path(__file__).parent.parent / 'admin.html'),
        "admin_html_exists": (Path(__file__).parent.parent / 'admin.html').exists(),
        "data_dir": str(DATA_DIR),
        "data_dir_exists": DATA_DIR.exists(),
        "files": files_status,
        "global_stats": global_stats,
        "servers_count": len(servers),
        "history_points": len(history['timestamps'])
    }), 200

def format_uptime(seconds):
    """Форматирует время в читаемый вид"""
    if seconds < 60:
        return f"{int(seconds)}s"
    elif seconds < 3600:
        return f"{int(seconds / 60)}m"
    elif seconds < 86400:
        return f"{int(seconds / 3600)}h"
    else:
        return f"{int(seconds / 86400)}d"

def background_stats_collector():
    """Фоновый поток для сбора статистики каждые 5 секунд"""
    global stop_background, force_backup
    log("[BACKGROUND] Stats collector started")
    
    last_git_push = time.time()
    git_push_interval = 5 * 60  # 5 минут
    
    while not stop_background:
        try:
            # Ждем 5 секунд
            for _ in range(5):
                if stop_background:
                    break
                time.sleep(1)
            
            if stop_background:
                break
            
            # Добавляем точку только если нет активных серверов
            stats = get_stats()
            if stats['servers_online'] == 0:
                add_to_history(stats)
                log(f"[BACKGROUND] Added history point (no active servers)")
            
            # Проверяем, нужно ли делать git push
            current_time = time.time()
            should_push = (current_time - last_git_push >= git_push_interval) or force_backup
            
            if should_push:
                if force_backup:
                    log("[BACKGROUND] Force backup requested, starting Git commit and push...")
                    force_backup = False  # Сбрасываем флаг
                else:
                    log("[BACKGROUND] Starting Git commit and push...")
                
                if git_commit_and_push():
                    last_git_push = current_time
                    log("[BACKGROUND] Git push completed successfully")
                else:
                    log("[BACKGROUND] Git push failed, will retry in 5 minutes")
            
        except Exception as e:
            log(f"[BACKGROUND] Error: {e}")
    
    # Финальный push при остановке
    log("[BACKGROUND] Stopping, doing final git push...")
    git_commit_and_push()
    log("[BACKGROUND] Stats collector stopped")

def start_background_collector():
    """Запускает фоновый сборщик статистики"""
    global background_thread
    if background_thread is None or not background_thread.is_alive():
        background_thread = Thread(target=background_stats_collector, daemon=True)
        background_thread.start()

def stop_background_collector():
    """Останавливает фоновый сборщик"""
    global stop_background
    stop_background = True
    if background_thread:
        background_thread.join(timeout=5)

def initialize():
    """Инициализация при старте приложения"""
    global initialized, servers, global_stats, history
    
    if initialized:
        return
    
    log("[STARTUP] Starting PVPBOT Stats Backend...")
    log(f"[STARTUP] Data directory: {DATA_DIR}")
    log(f"[STARTUP] GitHub repo: {GITHUB_REPO}")
    log(f"[STARTUP] GitHub token: {'SET' if GITHUB_TOKEN else 'NOT SET'}")
    
    # Настраиваем Git
    try:
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.email', 'bot@pvpbot.stats'], 
                      capture_output=True, text=True, timeout=5)
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.name', 'PVPBOT Stats Bot'], 
                      capture_output=True, text=True, timeout=5)
        log("[STARTUP] Git configured")
    except Exception as e:
        log(f"[STARTUP] Failed to configure git: {e}")
    
    # Загружаем данные при старте
    try:
        servers_loaded, global_stats_loaded = load_data()
        servers.update(servers_loaded)
        global_stats.update(global_stats_loaded)
        log(f"[STARTUP] Loaded {len(servers)} servers from local storage")
    except Exception as e:
        log(f"[STARTUP] Failed to load servers: {e}")
    
    try:
        load_history()
        log(f"[STARTUP] History loaded: {len(history['timestamps'])} points")
    except Exception as e:
        log(f"[STARTUP] Failed to load history: {e}")
        import traceback
        traceback.print_exc()
    
    log(f"[STARTUP] Total spawned: {global_stats['total_spawned']}")
    log(f"[STARTUP] Total killed: {global_stats['total_killed']}")
    
    # Создаём файлы если их нет (для Git commit)
    if not SERVERS_FILE.exists():
        save_data()
        log("[STARTUP] Created servers.json")
    if not GLOBAL_STATS_FILE.exists():
        save_global_stats()
        log("[STARTUP] Created global_stats.json")
    if not GLOBAL_HISTORY_FILE.exists():
        save_history()
        log("[STARTUP] Created global_history.json")
    
    # Запускаем фоновый сборщик статистики
    start_background_collector()
    
    # Регистрируем остановку при выходе
    atexit.register(stop_background_collector)
    
    initialized = True
    log("[STARTUP] Initialization complete")

@app.route('/api/admin/backup', methods=['POST'])
def manual_git_backup():
    """Ручной бэкап в Git - устанавливает флаг для background потока"""
    global force_backup
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        log("[API] Manual backup requested - setting force_backup flag")
        force_backup = True
        
        return jsonify({"success": True, "message": "Backup scheduled, will execute within 5 seconds"}), 200
    except Exception as e:
        log(f"[API] Manual backup error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/reload', methods=['POST'])
def reload_from_git():
    """Перезагружает данные из Git"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        # Делаем git reset
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'fetch', 'origin'], 
                              capture_output=True, text=True, timeout=30)
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'reset', '--hard', f'origin/{GITHUB_BRANCH}'], 
                              capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            # Перезагружаем данные
            global servers, global_stats, history
            servers.clear()
            servers_loaded, global_stats_loaded = load_data()
            servers.update(servers_loaded)
            global_stats.update(global_stats_loaded)
            load_history()
            
            return jsonify({"success": True, "message": "Data reloaded from Git"}), 200
        else:
            return jsonify({"error": "Git reset failed"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/stats', methods=['PUT'])
def update_global_stats():
    """Обновляет глобальные статистики"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        data = request.json
        global_stats['total_spawned'] = data.get('total_spawned', 0)
        global_stats['total_killed'] = data.get('total_killed', 0)
        save_global_stats()
        
        return jsonify({"success": True, "message": "Global stats updated"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/stats/reset', methods=['POST'])
def reset_global_stats():
    """Сбрасывает глобальные статистики"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        global_stats['total_spawned'] = 0
        global_stats['total_killed'] = 0
        save_global_stats()
        
        return jsonify({"success": True, "message": "Global stats reset"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/server/<server_id>', methods=['DELETE'])
def delete_server(server_id):
    """Удаляет данные сервера"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        if server_id in servers:
            del servers[server_id]
            save_data()
            
            # Удаляем файл истории сервера
            history_file = get_server_history_file(server_id)
            if history_file.exists():
                history_file.unlink()
            
            return jsonify({"success": True, "message": "Server data deleted"}), 200
        else:
            return jsonify({"error": "Server not found"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/history', methods=['DELETE'])
def clear_all_history():
    """Очищает всю историю"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        global history
        history = {
            "timestamps": [],
            "servers": [],
            "bots": [],
            "spawned": [],
            "killed": []
        }
        save_history()
        
        # Удаляем все файлы истории серверов
        for file in DATA_DIR.glob("server_*.json"):
            file.unlink()
        
        return jsonify({"success": True, "message": "All history cleared"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/backups', methods=['GET'])
def list_backups():
    """Возвращает список бэкапов из Git"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        # Получаем список коммитов
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'log', '--pretty=format:%H|%ai|%s', '-20'], 
                              capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            backups = []
            for line in result.stdout.strip().split('\n'):
                if line:
                    commit, date, message = line.split('|', 2)
                    backups.append({
                        'commit': commit[:8],
                        'date': date,
                        'message': message
                    })
            return jsonify({"backups": backups}), 200
        else:
            return jsonify({"error": "Failed to list backups"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/backup/<commit>', methods=['POST'])
def load_specific_backup(commit):
    """Загружает конкретный бэкап"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        # Делаем git reset к конкретному коммиту
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'reset', '--hard', commit], 
                              capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            # Перезагружаем данные
            global servers, global_stats, history
            servers.clear()
            servers_loaded, global_stats_loaded = load_data()
            servers.update(servers_loaded)
            global_stats.update(global_stats_loaded)
            load_history()
            
            return jsonify({"success": True, "message": f"Backup {commit} loaded"}), 200
        else:
            return jsonify({"error": "Failed to load backup"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/admin/git/reinit', methods=['POST'])
def reinit_git_repo():
    """Переинициализирует Git репозиторий (ОПАСНО - только для восстановления)"""
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or auth_header != f"Bearer {ADMIN_PASSWORD}":
            return jsonify({"error": "Unauthorized"}), 401
        
        data = request.json or {}
        confirm = data.get('confirm', '')
        
        if confirm != 'DELETE_GIT_REPO':
            return jsonify({"error": "Confirmation required. Send {\"confirm\": \"DELETE_GIT_REPO\"}"}), 400
        
        log("[API] Git repository re-initialization requested")
        
        # Удаляем только .git папку, НЕ данные
        git_dir = DATA_DIR / '.git'
        if git_dir.exists():
            import shutil
            shutil.rmtree(git_dir)
            log("[GIT] Removed .git directory")
        
        # Инициализируем заново
        subprocess.run(['git', '-C', str(DATA_DIR), 'init'], 
                      capture_output=True, text=True, timeout=10)
        
        # Добавляем remote
        repo_url = GITHUB_REPO.replace('https://', f'https://{GITHUB_TOKEN}@') if GITHUB_TOKEN else GITHUB_REPO
        subprocess.run(['git', '-C', str(DATA_DIR), 'remote', 'add', 'origin', repo_url], 
                      capture_output=True, text=True, timeout=5)
        
        # Настраиваем Git
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.email', 'bot@pvpbot.stats'], 
                      capture_output=True, text=True, timeout=5)
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'user.name', 'PVPBOT Stats Bot'], 
                      capture_output=True, text=True, timeout=5)
        subprocess.run(['git', '-C', str(DATA_DIR), 'config', 'pull.rebase', 'true'], 
                      capture_output=True, text=True, timeout=5)
        
        # Пробуем pull
        result = subprocess.run(['git', '-C', str(DATA_DIR), 'pull', 'origin', GITHUB_BRANCH], 
                              capture_output=True, text=True, timeout=30)
        
        if result.returncode == 0:
            log("[GIT] Repository re-initialized and pulled successfully")
            return jsonify({"success": True, "message": "Git repository re-initialized"}), 200
        else:
            log(f"[GIT] Pull failed after re-init: {result.stderr}")
            return jsonify({"success": False, "message": "Re-initialized but pull failed", "error": result.stderr}), 200
            
    except Exception as e:
        log(f"[API] Git re-init error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/admin')
def admin_page():
    """Админ-панель (встроенный HTML)"""
    return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PVPBOT Admin Panel</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #1a1a2e; color: #eaeaea; padding: 20px; }
        .container { max-width: 1400px; margin: 0 auto; }
        .login-container { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
        .login-box { background: #16213e; padding: 40px; border-radius: 12px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); width: 100%; max-width: 400px; }
        .login-box h1 { margin-bottom: 30px; text-align: center; color: #667eea; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #a0a0a0; }
        .form-group input { width: 100%; padding: 12px; background: #0f3460; border: 1px solid #2a2a4e; border-radius: 6px; color: #eaeaea; font-size: 16px; }
        .form-group input:focus { outline: none; border-color: #667eea; }
        .btn { width: 100%; padding: 12px; background: linear-gradient(135deg, #667eea, #764ba2); border: none; border-radius: 6px; color: white; font-size: 16px; font-weight: 600; cursor: pointer; transition: transform 0.2s; }
        .btn:hover { transform: translateY(-2px); }
        .error-message { color: #ff4444; text-align: center; margin-top: 15px; display: none; }
        .admin-panel { display: none; }
        header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #2a2a4e; }
        header h1 { color: #667eea; }
        .header-actions { display: flex; gap: 10px; align-items: center; }
        .search-box { padding: 8px 12px; background: #0f3460; border: 1px solid #2a2a4e; border-radius: 6px; color: #eaeaea; width: 300px; }
        .search-box:focus { outline: none; border-color: #667eea; }
        .logout-btn { padding: 10px 20px; background: #ff4444; border: none; border-radius: 6px; color: white; cursor: pointer; font-weight: 600; }
        .back-btn { padding: 10px 20px; background: #667eea; border: none; border-radius: 6px; color: white; cursor: pointer; font-weight: 600; }
        .stats-summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: #16213e; padding: 20px; border-radius: 8px; text-align: center; }
        .stat-card h3 { color: #a0a0a0; font-size: 14px; margin-bottom: 10px; }
        .stat-card .value { font-size: 32px; font-weight: bold; color: #667eea; }
        .servers-list { background: #16213e; border-radius: 8px; overflow: hidden; }
        .servers-list h2 { padding: 20px; background: #0f3460; margin: 0; }
        .server-item { padding: 20px; border-bottom: 1px solid #2a2a4e; cursor: pointer; transition: background 0.2s; }
        .server-item:hover { background: #0f3460; }
        .server-item:last-child { border-bottom: none; }
        .server-item.offline { opacity: 0.6; background: #1a1a2e; }
        .server-item.offline:hover { background: #0f3460; opacity: 0.8; }
        .server-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
        .server-id { font-weight: 600; color: #667eea; font-family: monospace; }
        .server-id-full { font-size: 12px; color: #a0a0a0; margin-top: 5px; font-family: monospace; }
        .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-online { background: #4caf50; color: white; }
        .status-offline { background: #666; color: white; }
        .server-stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; margin-top: 15px; }
        .server-stat { font-size: 14px; color: #a0a0a0; }
        .server-stat strong { color: #eaeaea; }
        .loading { text-align: center; padding: 40px; color: #a0a0a0; }
        .server-detail { display: none; }
        .detail-header { background: #16213e; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 20px; }
        .detail-card { background: #16213e; padding: 20px; border-radius: 8px; }
        .detail-card h3 { color: #667eea; margin-bottom: 15px; }
        .detail-list { list-style: none; max-height: 400px; overflow-y: auto; }
        .detail-list li { padding: 8px 0; border-bottom: 1px solid #2a2a4e; color: #a0a0a0; }
        .detail-list li:last-child { border-bottom: none; }
        .op-badge { background: #ff9800; color: white; padding: 2px 6px; border-radius: 4px; font-size: 11px; margin-left: 8px; font-weight: 600; }
        .search-list { padding: 8px; background: #0f3460; border: 1px solid #2a2a4e; border-radius: 6px; color: #eaeaea; width: 100%; margin-bottom: 10px; }
        .search-list:focus { outline: none; border-color: #667eea; }
        .time-filters { display: flex; gap: 10px; margin-bottom: 15px; justify-content: center; }
        .time-filter-btn { padding: 8px 16px; background: #0f3460; border: 1px solid #2a2a4e; border-radius: 6px; color: #a0a0a0; cursor: pointer; font-size: 14px; transition: all 0.2s; }
        .time-filter-btn:hover { background: #667eea; color: white; border-color: #667eea; }
        .time-filter-btn.active { background: #667eea; color: white; border-color: #667eea; }
        .action-btn { width: 100%; padding: 12px; margin: 8px 0; background: #667eea; border: none; border-radius: 6px; color: white; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .action-btn:hover { background: #5568d3; transform: translateY(-2px); }
        .action-btn.danger { background: #ff4444; }
        .action-btn.danger:hover { background: #cc0000; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; color: #a0a0a0; font-size: 14px; }
        .form-input { width: 100%; padding: 10px; background: #0f3460; border: 1px solid #2a2a4e; border-radius: 6px; color: #eaeaea; font-size: 14px; }
        .form-input:focus { outline: none; border-color: #667eea; }
        .action-result { margin-top: 20px; padding: 15px; border-radius: 8px; font-size: 14px; }
        .action-result.success { background: #4caf50; color: white; }
        .action-result.error { background: #ff4444; color: white; }
        .action-result.info { background: #2196f3; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <div class="login-container" id="loginContainer">
            <div class="login-box">
                <h1>🔐 Admin Login</h1>
                <form id="loginForm" action="javascript:void(0);">
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" placeholder="Enter admin password" required>
                    </div>
                    <button type="button" class="btn" onclick="handleLogin(event)">Login</button>
                    <div class="error-message" id="errorMessage">Invalid password</div>
                </form>
            </div>
        </div>
        <div class="admin-panel" id="adminPanel">
            <header>
                <h1>📊 PVPBOT Admin Panel</h1>
                <div class="header-actions">
                    <input type="text" class="search-box" id="searchBox" placeholder="Search by UUID..." />
                    <button class="back-btn" onclick="showActionsPanel()" style="background: #ff9800;">⚙️ Actions</button>
                    <button class="logout-btn" onclick="logout()">Logout</button>
                </div>
            </header>
            <div id="serversList">
                <div class="stats-summary">
                    <div class="stat-card"><h3>Total Servers</h3><div class="value" id="totalServers">0</div></div>
                    <div class="stat-card"><h3>Online Servers</h3><div class="value" id="onlineServers">0</div></div>
                    <div class="stat-card"><h3>Total Bots</h3><div class="value" id="totalBots">0</div></div>
                    <div class="stat-card"><h3>Real Players</h3><div class="value" id="totalPlayers">0</div></div>
                </div>
                <div class="servers-list">
                    <h2>Connected Servers</h2>
                    <div id="serversListContent" class="loading">Loading...</div>
                </div>
            </div>
            <div class="server-detail" id="serverDetail">
                <header>
                    <h1 id="detailTitle">Server Details</h1>
                    <button class="back-btn" onclick="showServersList()">← Back</button>
                </header>
                <div class="detail-header">
                    <div class="server-id-full" id="detailUUID">UUID: ...</div>
                    <div class="server-stats" id="detailStats"></div>
                </div>
                <div class="detail-grid">
                    <div class="detail-card">
                        <h3>🤖 Bots List (<span id="botsCount">0</span>)</h3>
                        <input type="text" class="search-list" id="searchBots" placeholder="Search bots..." />
                        <ul class="detail-list" id="botsList"></ul>
                    </div>
                    <div class="detail-card">
                        <h3>👥 Real Players (<span id="playersCount">0</span>)</h3>
                        <input type="text" class="search-list" id="searchPlayers" placeholder="Search players..." />
                        <ul class="detail-list" id="playersList"></ul>
                    </div>
                    <div class="detail-card">
                        <h3>⚙️ Server Info</h3>
                        <ul class="detail-list" id="serverInfo"></ul>
                    </div>
                </div>
                <div class="detail-grid">
                    <div class="detail-card">
                        <h3>📊 Bots Online</h3>
                        <div class="time-filters">
                            <button class="time-filter-btn active" onclick="changeTimeFilter('30m', 'bots')">30M</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1h', 'bots')">1H</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1d', 'bots')">1D</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1w', 'bots')">1W</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1m', 'bots')">1M</button>
                        </div>
                        <div style="position: relative; height: 300px;">
                            <canvas id="botsChart"></canvas>
                        </div>
                    </div>
                    <div class="detail-card">
                        <h3>📊 Players Online</h3>
                        <div class="time-filters">
                            <button class="time-filter-btn active" onclick="changeTimeFilter('30m', 'players')">30M</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1h', 'players')">1H</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1d', 'players')">1D</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1w', 'players')">1W</button>
                            <button class="time-filter-btn" onclick="changeTimeFilter('1m', 'players')">1M</button>
                        </div>
                        <div style="position: relative; height: 300px;">
                            <canvas id="playersChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="server-detail" id="actionsPanel" style="display: none;">
                <header>
                    <h1>⚙️ Admin Actions</h1>
                    <button class="back-btn" onclick="showServersList()">← Back</button>
                </header>
                
                <div class="detail-grid">
                    <div class="detail-card">
                        <h3>💾 Backup & Restore</h3>
                        <button class="action-btn" onclick="createBackup()">Create Backup Now</button>
                        <button class="action-btn" onclick="loadLatestBackup()">Load Latest Backup</button>
                        <button class="action-btn" onclick="showBackupsList()">Browse Backups</button>
                        <div id="backupsListContainer" style="display: none; margin-top: 15px;">
                            <h4>Available Backups:</h4>
                            <ul class="detail-list" id="backupsList"></ul>
                        </div>
                    </div>
                    
                    <div class="detail-card">
                        <h3>🗑️ Data Management</h3>
                        <button class="action-btn danger" onclick="deleteServerData()">Delete Server Data</button>
                        <button class="action-btn danger" onclick="clearAllHistory()">Clear All History</button>
                        <button class="action-btn danger" onclick="resetGlobalStats()">Reset Global Stats</button>
                        <p style="color: #ff4444; font-size: 12px; margin-top: 10px;">⚠️ Dangerous actions - cannot be undone!</p>
                    </div>
                    
                    <div class="detail-card">
                        <h3>✏️ Edit Global Stats</h3>
                        <div class="form-group">
                            <label>Bots Spawned (Total):</label>
                            <input type="number" id="editSpawned" class="form-input" placeholder="0" />
                        </div>
                        <div class="form-group">
                            <label>Bots Killed (Total):</label>
                            <input type="number" id="editKilled" class="form-input" placeholder="0" />
                        </div>
                        <button class="action-btn" onclick="updateGlobalStats()">Update Stats</button>
                    </div>
                    
                    <div class="detail-card">
                        <h3>🔄 System Actions</h3>
                        <button class="action-btn" onclick="forceGitPush()">Force Git Push</button>
                        <button class="action-btn" onclick="reloadFromGit()">Reload from Git</button>
                        <button class="action-btn" onclick="viewSystemLogs()">View System Logs</button>
                        <button class="action-btn danger" onclick="reinitGitRepo()">⚠️ Re-init Git Repo</button>
                        <p style="color: #ff9800; font-size: 12px; margin-top: 10px;">⚠️ Re-init only if Git is broken!</p>
                    </div>
                </div>
                
                <div id="actionResult" class="action-result" style="display: none;"></div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns@3.0.0/dist/chartjs-adapter-date-fns.bundle.min.js"></script>
    <script>
        const API_URL = window.location.origin;
        let authToken = localStorage.getItem('adminToken');
        let allServers = [];
        let currentServerId = null;
        let detailUpdateInterval = null;
        let allBots = [];
        let allPlayers = [];
        let botsChart = null;
        let playersChart = null;
        let fullHistoryData = null;
        let currentTimeFilter = '30m';
        
        function handleLogin(e) {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            
            var password = document.getElementById('password').value;
            var errorMsg = document.getElementById('errorMessage');
            errorMsg.style.display = 'none';
            
            fetch(API_URL + '/api/admin/auth', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ password: password })
            })
            .then(function(response) {
                if (response.ok) {
                    authToken = password;
                    localStorage.setItem('adminToken', password);
                    showAdminPanel();
                } else {
                    errorMsg.textContent = 'Invalid password';
                    errorMsg.style.display = 'block';
                }
            })
            .catch(function(error) {
                errorMsg.textContent = 'Connection error';
                errorMsg.style.display = 'block';
            });
            
            return false;
        }
        
        window.addEventListener('DOMContentLoaded', function() {
            if (authToken) showAdminPanel();
            
            document.getElementById('searchBox').addEventListener('input', function(e) {
                var query = e.target.value.toLowerCase();
                renderServers(allServers.filter(function(s) { return s.id.toLowerCase().includes(query); }));
            });
            
            document.getElementById('searchBots').addEventListener('input', function(e) {
                var query = e.target.value.toLowerCase();
                renderBotsList(allBots.filter(function(b) { return b.toLowerCase().includes(query); }));
            });
            
            document.getElementById('searchPlayers').addEventListener('input', function(e) {
                var query = e.target.value.toLowerCase();
                renderPlayersList(allPlayers.filter(function(p) { return p.name.toLowerCase().includes(query); }));
            });
        });
        
        // Обработка навигации по URL
        window.addEventListener('popstate', (e) => {
            if (e.state && e.state.serverId) {
                showServerDetail(e.state.serverId, false);
            } else {
                showServersList(false);
            }
        });
        
        // Проверяем URL при загрузке
        window.addEventListener('load', () => {
            const path = window.location.pathname;
            const match = path.match(/\\/admin\\/([a-f0-9-]+)/);
            if (match && authToken) {
                const serverId = match[1];
                // Ждем загрузки серверов
                setTimeout(() => {
                    if (allServers.find(s => s.id === serverId)) {
                        showServerDetail(serverId, false);
                    }
                }, 1000);
            }
        });
        
        function showAdminPanel() {
            document.getElementById('loginContainer').style.display = 'none';
            document.getElementById('adminPanel').style.display = 'block';
            loadServers();
            setInterval(loadServers, 5000);
        }
        
        function logout() {
            localStorage.removeItem('adminToken');
            location.reload();
        }
        
        function showServersList(updateHistory = true) {
            currentServerId = null;
            if (detailUpdateInterval) {
                clearInterval(detailUpdateInterval);
                detailUpdateInterval = null;
            }
            document.getElementById('serversList').style.display = 'block';
            document.getElementById('serverDetail').style.display = 'none';
            document.getElementById('actionsPanel').style.display = 'none';
            
            if (updateHistory) {
                history.pushState({}, 'Admin Panel', '/admin');
            }
        }
        
        function showActionsPanel() {
            currentServerId = null;
            if (detailUpdateInterval) {
                clearInterval(detailUpdateInterval);
                detailUpdateInterval = null;
            }
            document.getElementById('serversList').style.display = 'none';
            document.getElementById('serverDetail').style.display = 'none';
            document.getElementById('actionsPanel').style.display = 'block';
            
            // Загружаем текущие глобальные статистики
            loadCurrentGlobalStats();
        }
        
        function loadCurrentGlobalStats() {
            fetch(API_URL + '/health')
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.global_stats) {
                    document.getElementById('editSpawned').value = data.global_stats.total_spawned;
                    document.getElementById('editKilled').value = data.global_stats.total_killed;
                }
            })
            .catch(function(error) {
                console.error('Failed to load global stats:', error);
            });
        }
        
        function showActionResult(message, type) {
            if (!type) type = 'success';
            var resultDiv = document.getElementById('actionResult');
            resultDiv.textContent = message;
            resultDiv.className = 'action-result ' + type;
            resultDiv.style.display = 'block';
            setTimeout(function() {
                resultDiv.style.display = 'none';
            }, 5000);
        }
        
        function createBackup() {
            if (!confirm('Create a backup now? This will commit and push all data to GitHub.')) return;
            
            fetch(API_URL + '/api/admin/backup', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success || data.commit) {
                    showActionResult('✅ Backup created successfully!', 'success');
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function forceGitPush() {
            if (!confirm('Force Git push now?')) return;
            createBackup();
        }
        
        function reloadFromGit() {
            if (!confirm('Reload all data from Git? This will discard local changes!')) return;
            
            fetch(API_URL + '/api/admin/reload', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Data reloaded from Git!', 'success');
                    setTimeout(function() { location.reload(); }, 2000);
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function updateGlobalStats() {
            var spawned = parseInt(document.getElementById('editSpawned').value) || 0;
            var killed = parseInt(document.getElementById('editKilled').value) || 0;
            
            if (!confirm('Update global stats to:\\nSpawned: ' + spawned + '\\nKilled: ' + killed)) return;
            
            fetch(API_URL + '/api/admin/stats', {
                method: 'PUT',
                headers: { 
                    'Authorization': 'Bearer ' + authToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ total_spawned: spawned, total_killed: killed })
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Global stats updated!', 'success');
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function deleteServerData() {
            var serverId = prompt('Enter server UUID to delete:');
            if (!serverId) return;
            
            if (!confirm('Delete all data for server ' + serverId + '? This cannot be undone!')) return;
            
            fetch(API_URL + '/api/admin/server/' + serverId, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Server data deleted!', 'success');
                    setTimeout(function() { showServersList(); }, 2000);
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function clearAllHistory() {
            if (!confirm('Clear ALL history data? This cannot be undone!')) return;
            if (!confirm('Are you ABSOLUTELY sure? This will delete all historical data!')) return;
            
            fetch(API_URL + '/api/admin/history', {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ All history cleared!', 'success');
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function resetGlobalStats() {
            if (!confirm('Reset global stats to 0? This cannot be undone!')) return;
            
            fetch(API_URL + '/api/admin/stats/reset', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Global stats reset!', 'success');
                    loadCurrentGlobalStats();
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function viewSystemLogs() {
            fetch(API_URL + '/api/logs?lines=100')
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.logs) {
                    var logs = data.logs.join('\\n');
                    alert('System Logs (last 100 lines):\\n\\n' + logs);
                } else {
                    showActionResult('❌ Failed to load logs', 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function reinitGitRepo() {
            if (!confirm('⚠️ WARNING: This will DELETE the .git directory and re-initialize it!\\n\\nYour data files will be preserved, but Git history will be reset.\\n\\nOnly do this if Git is completely broken!\\n\\nContinue?')) return;
            if (!confirm('Are you ABSOLUTELY sure? Type DELETE_GIT_REPO in the next prompt.')) return;
            
            var confirmation = prompt('Type DELETE_GIT_REPO to confirm:');
            if (confirmation !== 'DELETE_GIT_REPO') {
                showActionResult('❌ Cancelled - confirmation did not match', 'info');
                return;
            }
            
            fetch(API_URL + '/api/admin/git/reinit', {
                method: 'POST',
                headers: { 
                    'Authorization': 'Bearer ' + authToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ confirm: 'DELETE_GIT_REPO' })
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Git repository re-initialized!', 'success');
                } else {
                    showActionResult('⚠️ ' + (data.message || 'Partial success'), 'info');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function showBackupsList() {
            var container = document.getElementById('backupsListContainer');
            var list = document.getElementById('backupsList');
            
            if (container.style.display === 'block') {
                container.style.display = 'none';
                return;
            }
            
            fetch(API_URL + '/api/admin/backups', {
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.backups) {
                    var html = '';
                    for (var i = 0; i < data.backups.length; i++) {
                        var b = data.backups[i];
                        html += '<li>' + b.date + ' - ' + b.commit + 
                            ' <button onclick="loadBackup(\'' + b.commit + '\')" style="margin-left: 10px; padding: 4px 8px; background: #667eea; border: none; border-radius: 4px; color: white; cursor: pointer;">Load</button></li>';
                    }
                    list.innerHTML = html;
                    container.style.display = 'block';
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function loadBackup(commit) {
            if (!confirm('Load backup from commit ' + commit + '? This will discard current data!')) return;
            
            fetch(API_URL + '/api/admin/backup/' + commit, {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    showActionResult('✅ Backup loaded!', 'success');
                    setTimeout(function() { location.reload(); }, 2000);
                } else {
                    showActionResult('❌ ' + (data.error || 'Unknown error'), 'error');
                }
            })
            .catch(function(error) {
                showActionResult('❌ Error: ' + error.message, 'error');
            });
        }
        
        function loadLatestBackup() {
            if (!confirm('Load latest backup from Git? This will discard local changes!')) return;
            reloadFromGit();
        }
        
        function showServerDetail(serverId, updateHistory) {
            if (updateHistory === undefined) updateHistory = true;
            var server = null;
            for (var i = 0; i < allServers.length; i++) {
                if (allServers[i].id === serverId) {
                    server = allServers[i];
                    break;
                }
            }
            if (!server) return;
            
            currentServerId = serverId;
            document.getElementById('serversList').style.display = 'none';
            document.getElementById('serverDetail').style.display = 'block';
            document.getElementById('detailTitle').textContent = 'Server: ' + server.id.substring(0, 8) + '...';
            document.getElementById('detailUUID').textContent = 'UUID: ' + server.id;
            
            if (updateHistory) {
                history.pushState({serverId: serverId}, 'Server ' + serverId.substring(0, 8), '/admin/' + serverId);
            }
            
            updateServerDetailStats(server);
            loadServerDetails(serverId);
            
            if (detailUpdateInterval) clearInterval(detailUpdateInterval);
            detailUpdateInterval = setInterval(function() {
                if (currentServerId === serverId) {
                    loadServerDetails(serverId);
                    var currentServer = null;
                    for (var i = 0; i < allServers.length; i++) {
                        if (allServers[i].id === serverId) {
                            currentServer = allServers[i];
                            break;
                        }
                    }
                    if (currentServer) {
                        updateServerDetailStats(currentServer);
                    }
                }
            }, 5000);
        }
        
        function changeTimeFilter(filter, chartType) {
            currentTimeFilter = filter;
            
            // Обновляем активную кнопку для всех графиков
            const allButtons = document.querySelectorAll('.time-filter-btn');
            allButtons.forEach(btn => {
                if (btn.textContent === filter.toUpperCase()) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });
            
            // Перерисовываем графики с новым фильтром
            if (fullHistoryData) {
                renderCharts(fullHistoryData);
            }
        }
        
        function filterDataByTime(timestamps, data) {
            const now = Date.now() / 1000;
            let cutoffTime;
            
            switch(currentTimeFilter) {
                case '30m': cutoffTime = now - (30 * 60); break;
                case '1h': cutoffTime = now - (60 * 60); break;
                case '1d': cutoffTime = now - (24 * 60 * 60); break;
                case '1w': cutoffTime = now - (7 * 24 * 60 * 60); break;
                case '1m': cutoffTime = now - (30 * 24 * 60 * 60); break;
                default: cutoffTime = now - (30 * 60);
            }
            
            const filtered = { timestamps: [], data: [] };
            for (let i = 0; i < timestamps.length; i++) {
                if (timestamps[i] >= cutoffTime) {
                    filtered.timestamps.push(timestamps[i]);
                    filtered.data.push(data[i]);
                }
            }
            
            return filtered;
        }
        
        function updateServerDetailStats(server) {
            var firstSeen = Date.now() / 1000;
            for (var i = 0; i < allServers.length; i++) {
                if (allServers[i].id === server.id) {
                    firstSeen = allServers[i].first_seen || firstSeen;
                    break;
                }
            }
            var uptime = formatUptime(Date.now() / 1000 - firstSeen);
            
            document.getElementById('detailStats').innerHTML = 
                '<div class="server-stat">🤖 Bots: <strong>' + server.bots_count + '</strong></div>' +
                '<div class="server-stat">👥 Players: <strong>' + server.real_players_count + '</strong></div>' +
                '<div class="server-stat">📊 Total: <strong>' + server.total_players_count + '</strong></div>' +
                '<div class="server-stat">⚔️ Spawned: <strong>' + server.bots_spawned_total + '</strong></div>' +
                '<div class="server-stat">💀 Killed: <strong>' + server.bots_killed_total + '</strong></div>' +
                '<div class="server-stat">🎮 Version: <strong>' + server.minecraft_version + '</strong></div>' +
                '<div class="server-stat">📦 Mod: <strong>' + server.mod_version + '</strong></div>' +
                '<div class="server-stat">⏱️ Uptime: <strong>' + uptime + '</strong></div>';
        }
        
        function formatUptime(seconds) {
            if (seconds < 60) return Math.floor(seconds) + 's';
            if (seconds < 3600) return Math.floor(seconds / 60) + 'm';
            if (seconds < 86400) return Math.floor(seconds / 3600) + 'h';
            return Math.floor(seconds / 86400) + 'd';
        }
        
        function loadServerDetails(serverId) {
            fetch(API_URL + '/api/admin/server/' + serverId, {
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Failed to load server details');
            })
            .then(function(data) {
                allBots = data.bots || [];
                allPlayers = data.players || [];
                
                allPlayers.sort(function(a, b) {
                    if (a.is_op && !b.is_op) return -1;
                    if (!a.is_op && b.is_op) return 1;
                    return a.name.localeCompare(b.name);
                });
                
                document.getElementById('botsCount').textContent = allBots.length;
                document.getElementById('playersCount').textContent = allPlayers.length;
                
                renderBotsList(allBots);
                renderPlayersList(allPlayers);
                
                var serverCore = data.server_core || 'Unknown';
                var firstSeenDate = new Date(data.first_seen * 1000).toLocaleString();
                var lastSeenDate = new Date(data.last_seen * 1000).toLocaleString();
                
                document.getElementById('serverInfo').innerHTML = '<li>Core: <strong>' + serverCore + '</strong></li>' +
                    '<li>Minecraft: <strong>' + data.minecraft_version + '</strong></li>' +
                    '<li>Mod Version: <strong>' + data.mod_version + '</strong></li>' +
                    '<li>First Seen: <strong>' + firstSeenDate + '</strong></li>' +
                    '<li>Last Seen: <strong>' + lastSeenDate + '</strong></li>';
                
                return fetch(API_URL + '/api/admin/server/' + serverId + '/history', {
                    headers: { 'Authorization': 'Bearer ' + authToken }
                });
            })
            .then(function(historyResponse) {
                if (historyResponse.ok) {
                    return historyResponse.json();
                }
                throw new Error('Failed to load history');
            })
            .then(function(historyData) {
                console.log('History data loaded:', historyData);
                renderCharts(historyData);
            })
            .catch(function(error) {
                console.error('Error loading server details:', error);
                renderCharts({timestamps: [], bots: [], players: []});
            });
        }
        
        function renderCharts(historyData) {
            fullHistoryData = historyData;
            const timestamps = historyData.timestamps || [];
            const bots = historyData.bots || [];
            const players = historyData.players || [];
            
            console.log('Rendering charts with data:', {
                timestamps: timestamps.length,
                bots: bots.length,
                players: players.length,
                filter: currentTimeFilter
            });
            
            // Фильтруем данные по времени
            const filteredBots = filterDataByTime(timestamps, bots);
            const filteredPlayers = filterDataByTime(timestamps, players);
            
            // Преобразуем timestamps в читаемые метки
            const botsLabels = filteredBots.timestamps.map(ts => {
                const date = new Date(ts * 1000);
                return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
            });
            
            const playersLabels = filteredPlayers.timestamps.map(ts => {
                const date = new Date(ts * 1000);
                return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
            });
            
            // График ботов
            const botsCtx = document.getElementById('botsChart');
            if (!botsCtx) {
                console.error('Canvas botsChart not found');
                return;
            }
            
            if (botsChart) {
                // Обновляем существующий график
                botsChart.data.labels = botsLabels;
                botsChart.data.datasets[0].data = filteredBots.data;
                botsChart.update('none'); // 'none' = без анимации
                console.log('Bots chart updated');
            } else {
                // Создаём новый график
                botsChart = new Chart(botsCtx.getContext('2d'), {
                    type: 'line',
                    data: {
                        labels: botsLabels,
                        datasets: [{
                            label: 'Bots Online',
                            data: filteredBots.data,
                            borderColor: '#667eea',
                            backgroundColor: 'rgba(102, 126, 234, 0.1)',
                            tension: 0.4,
                            fill: true,
                            pointRadius: 3,
                            pointHoverRadius: 5,
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        animation: false,
                        transitions: {
                            active: { animation: { duration: 0 } },
                            resize: { animation: { duration: 0 } },
                            show: { animation: { duration: 0 } },
                            hide: { animation: { duration: 0 } }
                        },
                        plugins: {
                            legend: { 
                                display: true,
                                labels: { color: '#eaeaea' }
                            },
                            tooltip: {
                                mode: 'index',
                                intersect: false
                            }
                        },
                        scales: {
                            x: {
                                ticks: { 
                                    color: '#a0a0a0',
                                    maxRotation: 45,
                                    minRotation: 45
                                },
                                grid: { color: '#2a2a4e' }
                            },
                            y: {
                                beginAtZero: true,
                                ticks: { 
                                    color: '#a0a0a0', 
                                    precision: 0,
                                    stepSize: 1
                                },
                                grid: { color: '#2a2a4e' }
                            }
                        }
                    }
                });
                console.log('Bots chart created');
            }
            
            // График игроков
            const playersCtx = document.getElementById('playersChart');
            if (!playersCtx) {
                console.error('Canvas playersChart not found');
                return;
            }
            
            if (playersChart) {
                // Обновляем существующий график
                playersChart.data.labels = playersLabels;
                playersChart.data.datasets[0].data = filteredPlayers.data;
                playersChart.update('none'); // 'none' = без анимации
                console.log('Players chart updated');
            } else {
                // Создаём новый график
                playersChart = new Chart(playersCtx.getContext('2d'), {
                    type: 'line',
                    data: {
                        labels: playersLabels,
                        datasets: [{
                            label: 'Players Online',
                            data: filteredPlayers.data,
                            borderColor: '#4caf50',
                            backgroundColor: 'rgba(76, 175, 80, 0.1)',
                            tension: 0.4,
                            fill: true,
                            pointRadius: 3,
                            pointHoverRadius: 5,
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        animation: false,
                        transitions: {
                            active: { animation: { duration: 0 } },
                            resize: { animation: { duration: 0 } },
                            show: { animation: { duration: 0 } },
                            hide: { animation: { duration: 0 } }
                        },
                        plugins: {
                            legend: { 
                                display: true,
                                labels: { color: '#eaeaea' }
                            },
                            tooltip: {
                                mode: 'index',
                                intersect: false
                            }
                        },
                        scales: {
                            x: {
                                ticks: { 
                                    color: '#a0a0a0',
                                    maxRotation: 45,
                                    minRotation: 45
                                },
                                grid: { color: '#2a2a4e' }
                            },
                            y: {
                                beginAtZero: true,
                                ticks: { 
                                    color: '#a0a0a0', 
                                    precision: 0,
                                    stepSize: 1
                                },
                                grid: { color: '#2a2a4e' }
                            }
                        }
                    }
                });
                console.log('Players chart created');
            }
            
            console.log('Charts rendered successfully: ' + filteredBots.timestamps.length + ' data points (filtered from ' + timestamps.length + ')');
        }
        
        function renderBotsList(bots) {
            var botsList = document.getElementById('botsList');
            if (bots.length === 0) {
                botsList.innerHTML = '<li>No bots found</li>';
            } else {
                var html = '';
                for (var i = 0; i < bots.length; i++) {
                    html += '<li>' + bots[i] + '</li>';
                }
                botsList.innerHTML = html;
            }
        }
        
        function renderPlayersList(players) {
            var playersList = document.getElementById('playersList');
            if (players.length === 0) {
                playersList.innerHTML = '<li>No players found</li>';
            } else {
                var html = '';
                for (var i = 0; i < players.length; i++) {
                    var p = players[i];
                    var name = typeof p === 'string' ? p : p.name;
                    var isOp = typeof p === 'object' && p.is_op;
                    html += '<li>' + name + (isOp ? '<span class="op-badge">OP</span>' : '') + '</li>';
                }
                playersList.innerHTML = html;
            }
        }
        
        function loadServers() {
            fetch(API_URL + '/api/admin/servers', {
                headers: { 'Authorization': 'Bearer ' + authToken }
            })
            .then(function(response) {
                if (response.status === 401) {
                    logout();
                    throw new Error('Unauthorized');
                }
                return response.json();
            })
            .then(function(data) {
                allServers = data.servers;
                
                document.getElementById('totalServers').textContent = data.total_servers;
                document.getElementById('onlineServers').textContent = data.online_servers;
                
                var totalBots = 0;
                var totalPlayers = 0;
                for (var i = 0; i < data.servers.length; i++) {
                    var server = data.servers[i];
                    if (server.is_online) {
                        totalBots += server.bots_count;
                        totalPlayers += server.real_players_count;
                    }
                }
                
                document.getElementById('totalBots').textContent = totalBots;
                document.getElementById('totalPlayers').textContent = totalPlayers;
                
                renderServers(allServers);
                
                if (currentServerId) {
                    var currentServer = null;
                    for (var i = 0; i < allServers.length; i++) {
                        if (allServers[i].id === currentServerId) {
                            currentServer = allServers[i];
                            break;
                        }
                    }
                    if (currentServer) {
                        updateServerDetailStats(currentServer);
                    }
                }
            })
            .catch(function(error) {
                console.error('Failed to load servers:', error);
            });
        }
        
        function renderServers(servers) {
            var serversList = document.getElementById('serversListContent');
            if (servers.length === 0) {
                serversList.innerHTML = '<div class="loading">No servers found</div>';
            } else {
                var html = '';
                for (var i = 0; i < servers.length; i++) {
                    var server = servers[i];
                    var onlineClass = server.is_online ? '' : 'offline';
                    var statusClass = server.is_online ? 'status-online' : 'status-offline';
                    var statusText = server.is_online ? 'ONLINE' : 'OFFLINE';
                    var idShort = server.id.substring(0, 8) + '...';
                    
                    html += '<div class="server-item ' + onlineClass + '" onclick="showServerDetail(\'' + server.id + '\')">' +
                        '<div class="server-header">' +
                        '<div><span class="server-id">' + idShort + '</span>' +
                        '<div class="server-id-full">' + server.id + '</div></div>' +
                        '<span class="status-badge ' + statusClass + '">' + statusText + '</span>' +
                        '</div>' +
                        '<div class="server-stats">' +
                        '<div class="server-stat">🤖 Bots: <strong>' + server.bots_count + '</strong></div>' +
                        '<div class="server-stat">👥 Players: <strong>' + server.real_players_count + '</strong></div>' +
                        '<div class="server-stat">📊 Total: <strong>' + server.total_players_count + '</strong></div>' +
                        '<div class="server-stat">⚔️ Spawned: <strong>' + server.bots_spawned_total + '</strong></div>' +
                        '<div class="server-stat">💀 Killed: <strong>' + server.bots_killed_total + '</strong></div>' +
                        '<div class="server-stat">🎮 Version: <strong>' + server.minecraft_version + '</strong></div>' +
                        '<div class="server-stat">📦 Mod: <strong>' + server.mod_version + '</strong></div>' +
                        '<div class="server-stat">⏱️ Uptime: <strong>' + server.uptime + '</strong></div>' +
                        '</div></div>';
                }
                serversList.innerHTML = html;
            }
        }
    </script>
</body>
</html>"""

@app.route('/admin/<server_id>')
def admin_server_page(server_id):
    """Админ-панель для конкретного сервера - возвращает ту же страницу"""
    return admin_page()

# Инициализируем при импорте модуля (после определения всех функций)
initialize()

if __name__ == '__main__':
    # Если запускаем напрямую через python
    # Replit использует порт 5000, Railway использует переменную PORT
    port = int(os.environ.get('PORT', 5000))
    log(f"[STARTUP] Starting server on port {port}")
    app.run(host='0.0.0.0', port=port, debug=False)
