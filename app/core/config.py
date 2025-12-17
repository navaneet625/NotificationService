from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Notification Service"
    API_V1_STR: str = "/api"
    
    # Database
    MYSQL_USER: str = "root"
    MYSQL_PASSWORD: str = "root"
    MYSQL_HOST: str = "mysql"
    MYSQL_PORT: int = 3306
    MYSQL_DATABASE: str = "notification_db"
    
    # Redis
    REDIS_HOST: str = "redis"
    REDIS_PORT: int = 6379
    
    # Kafka
    KAFKA_BOOTSTRAP_SERVERS: str = "kafka:9092"
    KAFKA_TOPIC_NOTIFICATIONS: str = "notifications"
    KAFKA_GROUP_ID: str = "notification-group"
    


    class Config:
        case_sensitive = True

settings = Settings()
