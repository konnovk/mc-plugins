# Metrics Plugin

Paper-плагин, который экспортирует метрики JVM и Minecraft в формате Prometheus. Плагин не отправляет данные сам: Prometheus периодически запрашивает HTTP endpoint.

## Сборка и установка

Из корня репозитория:

```bash
mvn -pl metrics-plugin -am clean package
```

Скопировать `metrics-plugin/target/metrics-plugin-dev.jar` в каталог `plugins/` Paper-сервера и перезапустить сервер.

По умолчанию endpoint доступен только на текущей машине:

```text
http://127.0.0.1:9464/metrics
```

Проверка:

```bash
curl http://127.0.0.1:9464/metrics
```

## Конфигурация

Файл `plugins/MetricsPlugin/config.yml` создаётся при первом запуске:

```yaml
metrics:
  enabled: true
  host: "127.0.0.1"
  port: 9464
  path: "/metrics"

  jvm:
    enabled: true

  minecraft:
    enabled: true
    update-interval-ticks: 20
    per-world-metrics: true
```

Для применения изменений нужен перезапуск плагина или сервера.

### Docker

Если Prometheus запущен в другом контейнере, `127.0.0.1` укажет на контейнер Prometheus, а не на Paper. В этом случае установите `host: "0.0.0.0"`, разрешите доступ только из приватной Docker-сети и используйте в `prometheus.yml` имя Paper-контейнера:

```yaml
targets:
  - "paper:9464"
```

Не публикуйте порт `9464` в интернет без сетевых ограничений или защищённого reverse proxy.

## Prometheus

Пример находится в [`examples/prometheus.yml`](examples/prometheus.yml):

```yaml
scrape_configs:
  - job_name: paper
    scrape_interval: 15s
    static_configs:
      - targets:
          - "127.0.0.1:9464"
```

## Метрики

При `metrics.jvm.enabled: true` регистрируются стандартные JVM- и process-метрики Prometheus Java Client: память, пулы памяти, GC, потоки, загрузка классов, JIT, buffer pools, CPU и uptime процесса.

Метрики Paper:

| Метрика | Labels | Описание |
|---|---|---|
| `minecraft_players_online` | — | Игроки онлайн |
| `minecraft_players_max` | — | Максимум игроков |
| `minecraft_tps` | `period="1m/5m/15m"` | TPS за разные интервалы |
| `minecraft_tick_duration_seconds` | — | Средняя длительность тика |
| `minecraft_worlds_loaded` | — | Загруженные миры |
| `minecraft_plugins_loaded` | — | Загруженные плагины |
| `minecraft_entities_loaded` | `world`, если включены per-world-метрики | Сущности |
| `minecraft_chunks_loaded` | `world`, если включены per-world-метрики | Чанки |
| `minecraft_server_info` | `minecraft_version`, `server_version` | Информация о версии; значение всегда `1` |

При `per-world-metrics: false` сущности и чанки суммируются по всем мирам, а label `world` не используется.

Bukkit API вызывается только в синхронной scheduler-задаче. HTTP-потоки Prometheus лишь читают снапшот реестра и не обращаются к серверу Minecraft.

Для метрик дисков, сети и ресурсов всей хостовой ОС рекомендуется отдельно установить `node_exporter`.
