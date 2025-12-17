from aiokafka import AIOKafkaConsumer
from app.core.config import settings
from app.db.session import AsyncSessionLocal
from app.models import Notification
from app.services.redis_ops import CacheService
from app.schemas import NotificationResponse
import asyncio
import logging

logger = logging.getLogger(__name__)

async def consume_notifications():
    consumer = AIOKafkaConsumer(
        settings.KAFKA_TOPIC_NOTIFICATIONS,
        bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
        group_id=settings.KAFKA_GROUP_ID
    )
    await consumer.start()
    try:
        async for msg in consumer:
            notification_id_str = msg.value.decode('utf-8')
            logger.info(f"Consumed notification ID: {notification_id_str}")
            
            try:
                notification_id = int(notification_id_str)
                async with AsyncSessionLocal() as session:
                    notification = await session.get(Notification, notification_id)
                    if notification:
                        notification.status = "SENT"
                        await session.commit()
                        await session.refresh(notification)
                        
                        # Update cache
                        response_model = NotificationResponse.model_validate(notification)
                        await CacheService.put(response_model)
                        logger.info(f"Notification {notification_id} marked as SENT")
            except Exception as e:
                logger.error(f"Error processing notification {notification_id_str}: {e}")
    finally:
        await consumer.stop()
