# mc-plugins

Набор плагинов для Paper.

## Модули

- [`monoplugin`](monoplugin/) — основной игровой плагин.
- [`metrics-plugin`](metrics-plugin/) — экспорт JVM- и Paper-метрик для Prometheus.

## Сборка

Требуется JDK 25.

```bash
mvn clean package
```

Собрать только плагин метрик:

```bash
mvn -pl metrics-plugin -am clean package
```
