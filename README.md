# Radiation Monitoring System

Система мониторинга радиационной обстановки.

## Технологии

- Java 21
- Spring Boot 3.5
- PostgreSQL 16
- Nginx
- Google Cloud Platform
- Leaflet
- Chart.js

## Возможности

- Мониторинг датчиков
- Мониторинг станций
- Heartbeat Raspberry Pi
- Карта станций
- История измерений
- События и тревоги
- HTTPS доступ

## Демо

https://radiation.dimlb.id.lv

## API

/api/detectors

/api/measurements/latest

/api/stations

/api/heartbeat

### Настройка сервера

Перед обновлением работающей базы остановите старый сервер, сделайте резервную копию и выполните
`db/migrate_utc_and_messages.sql`. Скрипт однократно переводит старые временные метки из
`Europe/Riga` в UTC. Если старый сервер работал в другом часовом поясе, замените его имя в скрипте.
Для локальной БД после остановки сервера: `psql -h localhost -p 5433 -U javauser -d radiation -v ON_ERROR_STOP=1 -f db/migrate_utc_and_messages.sql`.

Перед запуском задайте переменные окружения `DB_PASSWORD`, `RADIATION_ADMIN_TOKEN` и
`RADIATION_DEVICE_TOKENS`. Формат последней: `1:длинный-секрет-станции-1,2:длинный-секрет-станции-2`.
ID слева должен совпадать с ID станции в базе. Токены устройств должны быть различными и не
короче 16 символов. Не сохраняйте секреты в репозитории. Без настроенных токенов чтение работает,
а запись через API закрыта. При желании изменить пользователя БД задайте `DB_USER`.

Для локального запуска секреты можно хранить в игнорируемом Git файле `.env.local.ps1`:
`$env:DB_PASSWORD = '...'`, `$env:RADIATION_ADMIN_TOKEN = '...'`,
`$env:RADIATION_DEVICE_TOKENS = '1:...,2:...'`. Затем запустите `./run-local.ps1`.
Не копируйте этот файл на сервер или Raspberry Pi: каждой Pi передаётся только её собственный токен.

Обычный запуск: `./mvnw spring-boot:run`. Демо-симуляторы включаются только с профилем `demo`:
`./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`. Не включайте его для реальных станций.

Устройство отправляет `Authorization: Bearer <его токен>` и JSON:

`POST /api/measurements`: `{"detectorId":3,"value":0.546,"unit":"uSv/h","messageId":"UUID","measuredAt":"2026-09-23T12:00:00Z"}`.

`POST /api/heartbeat`: `{"messageId":"UUID","cpuTemp":42.0,"freeDiskGb":16.0,"memoryPercent":38.0}`.

Повтор с тем же `messageId` и тем же телом возвращает ранее сохранённую запись. `measuredAt` —
время измерения на устройстве, `createdAt` и `lastSeen` — время приёма сервером в UTC.
Порог предупреждения по умолчанию — `0.5 uSv/h`, тревоги — `1.0 uSv/h`.
Событие записывается при переходе между нормой, предупреждением и тревогой; длительное
превышение одного порога не создаёт одинаковое событие при каждом измерении.
`POST /api/stations` и `POST /api/detectors` требуют административный токен. Создавать события
напрямую через API нельзя. Панель предоставляет только чтение.

## Автор

Dmitrijs Lubjanovs
