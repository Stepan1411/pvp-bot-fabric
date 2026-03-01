# JitPack Build Check

## 🔗 Ссылка для проверки

https://jitpack.io/#Stepan1411/pvp-bot-fabric/v0.0.12

## ✅ Что должно произойти

1. Откройте ссылку выше
2. Нажмите кнопку "Get it" рядом с `v0.0.12`
3. JitPack начнет сборку проекта
4. Через 2-5 минут статус должен стать зеленым ✅

## 📋 Ожидаемый лог сборки

```
Build starting...
Git: v0.0.12
Init SDKMan
Running JDK install command: sdk install java 21.0.2-open
Installing: java 21.0.2-open
Done installing!
Found gradle
Gradle wrapper files:
-rwxr-xr-x 1 jitpack jitpack 8733 gradlew
-rw-r--r-- 1 jitpack jitpack 2937 gradlew.bat
Running: ./gradlew clean build publishToMavenLocal -x test --no-daemon --stacktrace
BUILD SUCCESSFUL
```

## ❌ Если сборка не прошла

### Проверьте логи

Нажмите на красный крестик ❌ чтобы увидеть полный лог.

### Частые проблемы

#### 1. "gradlew: Permission denied"
**Решение:** Уже исправлено в `jitpack.yml` через `chmod +x gradlew`

#### 2. "Gradle wrapper not found"
**Решение:** Уже исправлено - `gradlew` добавлен в репозиторий

#### 3. "Could not create service of type ScriptPluginFactory"
**Решение:** Уже исправлено - обновлен Gradle wrapper до 8.14.1

#### 4. "publishToMavenLocal task not found"
**Проверьте:** В `build.gradle` должен быть плагин `maven-publish`

## 🔧 Если нужно исправить

### 1. Обновить jitpack.yml

```yaml
jdk:
  - openjdk21
before_install:
  - sdk install java 21.0.2-open
  - sdk use java 21.0.2-open
install:
  - chmod +x gradlew
  - ./gradlew clean build publishToMavenLocal -x test --no-daemon
```

### 2. Закоммитить и запушить

```bash
git add jitpack.yml
git commit -m "Update jitpack.yml"
git push origin main
```

### 3. Пересоздать тег

```bash
git tag -d v0.0.12
git tag -a v0.0.12 -m "Version 0.0.12"
git push origin v0.0.12 --force
```

### 4. Очистить кэш JitPack

На странице JitPack нажмите "Look up" снова.

## 📦 После успешной сборки

### Использование в проекте

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation 'com.github.Stepan1411:pvp-bot-fabric:v0.0.12'
}
```

### Проверка

```bash
./gradlew dependencies | grep pvp-bot-fabric
```

Должно показать:
```
\--- com.github.Stepan1411:pvp-bot-fabric:v0.0.12
```

## 🎯 Статус проверки

- [ ] Открыл ссылку JitPack
- [ ] Нажал "Get it"
- [ ] Сборка запустилась
- [ ] Сборка завершилась успешно ✅
- [ ] Протестировал в проекте
- [ ] Все работает!

## 📞 Помощь

Если сборка не прошла:

1. Скопируйте полный лог с JitPack
2. Создайте issue на GitHub
3. Приложите лог и описание проблемы

---

**Ожидаемое время сборки:** 2-5 минут  
**Версия:** v0.0.12  
**Коммит:** 29db8bf
