from aiokafka import AIOKafkaProducer
from app.core.config import settings
import asyncio

class KafkaProducerService:
    producer: AIOKafkaProducer = None

    @classmethod
    async def start(cls):
        cls.producer = AIOKafkaProducer(
            bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS
        )
        await cls.producer.start()

    @classmethod
    async def stop(cls):
        if cls.producer:
            await cls.producer.stop()

    @classmethod
    async def send_notification(cls, notification_id: str):
        if not cls.producer:
            await cls.start()
        await cls.producer.send_and_wait(
            settings.KAFKA_TOPIC_NOTIFICATIONS, 
            notification_id.encode('utf-8')
        )
