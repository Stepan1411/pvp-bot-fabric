# translate_md_fast.py
import os
import glob
from deep_translator import GoogleTranslator
from tqdm import tqdm
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
import hashlib
import pickle
import json

class MarkdownTranslator:
    def __init__(self, cache_file='translation_cache.json'):
        self.cache_file = cache_file
        self.translation_cache = self._load_cache()
        
        self.languages = {
            'zh-CN': 'chinese (simplified)',
            'ru': 'russian',
            'fr': 'french',
            'es': 'spanish',
            'en': 'english',
            'de': 'german'
        }
        
        self.max_workers = 16
        self.request_delay = 0.1
        self.max_retries = 16
    
    def _load_cache(self):
        if os.path.exists(self.cache_file):
            try:
                with open(self.cache_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except:
                pass
        return {}
    
    def _save_cache(self):
        try:
            with open(self.cache_file, 'w', encoding='utf-8') as f:
                json.dump(self.translation_cache, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"⚠️ Не удалось сохранить кэш: {e}")
    
    def _get_cache_key(self, text, lang):
        return f"{lang}:{hashlib.md5(text.encode()).hexdigest()}"
    
    def translate_text(self, text, dest_lang):
        """Перевод одной строки с кэшированием"""
        if text is None:
            return ""
        
        text = str(text).strip()
        
        if not text:
            return text
        
        cache_key = self._get_cache_key(text, dest_lang)
        if cache_key in self.translation_cache:
            return self.translation_cache[cache_key]
        
        for attempt in range(self.max_retries):
            try:
                translator = GoogleTranslator(source='auto', target=dest_lang)
                result = translator.translate(text)
                
                if result is None or not isinstance(result, str):
                    result = text
                
                result = result.strip()
                self.translation_cache[cache_key] = result
                
                time.sleep(self.request_delay)
                return result
                
            except Exception as e:
                if attempt == self.max_retries - 1:
                    print(f"\n⚠️ Ошибка: {str(e)[:50]}")
                    return text
                time.sleep(1 * (attempt + 1))
        
        return text
    
    def translate_markdown_file(self, input_path, output_path, dest_lang):
        """Перевод Markdown файла (построчно, надёжно)"""
        try:
            with open(input_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            lines = content.split('\n')
            translated_lines = []
            in_code_block = False
            
            for line in tqdm(lines, desc=f"    {dest_lang}", leave=False):
                line_str = str(line) if line is not None else ""
                
                # Блоки кода не переводим
                if '```' in line_str:
                    in_code_block = not in_code_block
                    translated_lines.append(line_str)
                    continue
                
                # Пустые строки и код - без перевода
                if line_str.strip() == '' or in_code_block:
                    translated_lines.append(line_str)
                    continue
                
                # Короткие строки (заголовки, списки) - переводим
                if len(line_str.strip()) > 3:
                    translated = self.translate_text(line_str, dest_lang)
                    translated_lines.append(translated)
                else:
                    translated_lines.append(line_str)
            
            # Сохраняем
            output_dir = os.path.dirname(output_path)
            if output_dir:
                os.makedirs(output_dir, exist_ok=True)
            
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(translated_lines))
            
            return True
            
        except Exception as e:
            print(f"❌ Ошибка файла {input_path}: {e}")
            return False
    
    def translate_file_task(self, args):
        md_file, folder_path, languages = args
        relative_path = os.path.relpath(md_file, folder_path)
        filename = os.path.basename(md_file)
        
        results = []
        for lang in languages:
            output_dir = os.path.join(folder_path, f'{lang}')
            output_path = os.path.join(output_dir, relative_path)
            
            output_dir_path = os.path.dirname(output_path)
            if output_dir_path:
                os.makedirs(output_dir_path, exist_ok=True)
            
            success = self.translate_markdown_file(md_file, output_path, self.languages[lang])
            results.append((filename, lang, success))
        
        return results
    
    def translate_folder(self, folder_path, languages=None):
        if languages is None:
            languages = list(self.languages.keys())
        
        md_files = glob.glob(os.path.join(folder_path, '**', '*.md'), recursive=True)
        
        if not md_files:
            print("❌ .md файлы не найдены!")
            return
        
        print(f"📁 Найдено файлов: {len(md_files)}")
        print(f"🌐 Языки: {', '.join(languages)}")
        print(f"⚡ Потоки: {self.max_workers}\n")
        
        for lang in languages:
            os.makedirs(os.path.join(folder_path, f'{lang}'), exist_ok=True)
        
        tasks = [(md_file, folder_path, languages) for md_file in md_files]
        
        try:
            with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
                futures = [executor.submit(self.translate_file_task, task) for task in tasks]
                
                for future in tqdm(as_completed(futures), total=len(futures), desc="📄 Файлы"):
                    results = future.result()
                    for filename, lang, success in results:
                        if success:
                            print(f"✅ {filename} → {lang}")
        except Exception as e:
            print(f"❌ Критическая ошибка: {e}")
        
        self._save_cache()
        
        print("\n🎉 Перевод завершен!")
        print(f"💾 Кэш: {self.cache_file} ({len(self.translation_cache)} записей)")

if __name__ == "__main__":
    folder_path = "."
    
    translator = MarkdownTranslator()
    translator.max_workers = 16
    translator.request_delay = 0.2
    
    translator.translate_folder(folder_path)