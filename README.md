# NotificationService

A distributed, cloud-native notification microservice built using Spring Boot, Kafka, Redis, and MySQL — orchestrated with Docker Compose, monitored via Actuator/Prometheus, and integrated with AWS S3 for secure asset storage.

Supports asynchronous notifications, rate-limiting, idempotency, and cloud-based object storage integration.


📌 Features
✅ Notification API

1. Send notifications to users

2. Stores status in MySQL

3. Kafka message publishing

4. Redis-based idempotency

5. Redis-based rate limiting


✅ File Upload

1. Upload files to AWS S3

2. Pre-signed URL support

✅ Monitoring

1. Spring Boot Actuator

2. Prometheus endpoint /actuator/prometheus

3. Health checks for Redis/MySQL/Kafka


🐳 Docker Compose Setup
 Runs:
    MySQL
    Redis
    Kafka + Zookeeper
    Notification Service (Spring Boot)



docker compose up -d --build
docker compose ps
curl http://<PUBLIC_IP>:8080/actuator/health
curl -X POST "http://<PUBLIC_IP>:8080/api/notify?userId=1&msg=Hello"
curl "http://<PUBLIC_IP>:8080/api/notify/status?id=1"
curl -X POST "http://<PUBLIC_IP>:8080/api/notify/upload" -F "file=@/path/to/file.jpg"
for i in {1..70}; do 
  curl -X POST "http://<PUBLIC_IP>:8080/api/notify?userId=1&msg=Test-$i"; 
  echo; 
done
