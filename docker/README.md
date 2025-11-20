# IMRBS Docker Compose 開發環境

## 啟動服務

```bash
cd docker
docker-compose up -d
```

## 停止服務

```bash
docker-compose down
```

## 服務資訊

### PostgreSQL 16
- **Port**: 5432
- **Database**: imrbs
- **User**: imrbs_user
- **Password**: imrbs_pass
- **Connection String**: `postgresql://imrbs_user:imrbs_pass@localhost:5432/imrbs`

### Redis 7
- **Port**: 6379
- **Password**: imrbs_redis_pass
- **Connection String**: `redis://:imrbs_redis_pass@localhost:6379`

### RabbitMQ 3.13
- **AMQP Port**: 5672
- **Management UI**: http://localhost:15672
- **User**: imrbs_user
- **Password**: imrbs_pass
- **Virtual Host**: /imrbs
- **Connection String**: `amqp://imrbs_user:imrbs_pass@localhost:5672/imrbs`

## 健康檢查

```bash
# PostgreSQL
docker exec imrbs-postgres pg_isready -U imrbs_user -d imrbs

# Redis
docker exec imrbs-redis redis-cli -a imrbs_redis_pass ping

# RabbitMQ
docker exec imrbs-rabbitmq rabbitmq-diagnostics ping
```

## 清理數據

```bash
docker-compose down -v
```
