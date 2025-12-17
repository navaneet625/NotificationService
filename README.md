# NotificationService

A distributed, cloud-native notification microservice built with **FastAPI** (Python), integrating **Kafka**, **Redis**, and **MySQL**, orchestrated with **Docker Compose**.

## Features

### Notification API
1. **Send Notifications**: Asynchronous processing via Kafka.
2. **Status Tracking**: Stores status in MySQL and Redis.
3. **Idempotency**: Redis-based idempotency key support to prevent duplicate processing.
4. **Rate Limiting**: Redis-based rate limiting (60 requests/minute).

### Architecture
- **Language**: Python 3.11 (FastAPI)
- **Database**: MySQL 8 (Async SQLAlchemy)
- **Broker**: Kafka (AIOKafka)
- **Cache/KV**: Redis 7 (Async)
- **Containerization**: Docker & Docker Compose

## Docker Compose Setup

### Services
- `notification-service`: FastAPI Application (Port 8000)
- `mysql`: Database (Port 3306)
- `redis`: Cache & Rate Limiting (Port 6379)
- `kafka` + `zookeeper`: Message Broker (Port 9094)

### Running the App
```bash
docker compose up -d --build
```

### API Usage

#### 1. Check Health
```bash
curl http://localhost:8000/health
```

#### 2. Send Notification
```bash
curl -X POST "http://localhost:8000/api/notify?userId=1&msg=Hello"
```
**Response**: `Queued Notification ID: <id>`

#### 3. Check Status
```bash
curl "http://localhost:8000/api/notify/status?id=<id>"
```

#### 4. Test Rate Limiting
```bash
for i in {1..70}; do 
  curl -X POST "http://localhost:8000/api/notify?userId=1&msg=Test-$i"; 
  echo; 
done
```

#### 5. Test Idempotency
```bash
curl -X POST "http://localhost:8000/api/notify?userId=1&msg=Repeat" -H "Idempotency-Key: test-key-1"
```
Repeating the above command should return a duplicate request error.

