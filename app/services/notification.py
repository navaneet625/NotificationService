from sqlalchemy.ext.asyncio import AsyncSession
from app.models import Notification
from app.services.redis_ops import RateLimiter, IdempotencyService, CacheService
from app.kafka.producer import KafkaProducerService
from app.schemas import NotificationResponse
import logging

logger = logging.getLogger(__name__)

class NotificationService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def send_notification(self, user_id: str, message: str, idempotency_key: str = None) -> str:
        # 1. Rate Limiting
        if not await RateLimiter.allow_request(f"user:{user_id}"):
            logger.warning(f"RateLimitBlocked userId={user_id}")
            return "Rate limit exceeded. Try later."

        # 2. Idempotency Check
        if idempotency_key:
            if not await IdempotencyService.reserve(idempotency_key, "inprogress"):
                logger.info(f"DuplicateRequest userId={user_id} idempotencyKey={idempotency_key}")
                return "Duplicate request detected (idempotency)."

        # 3. Save DB record
        new_notification = Notification(user_id=user_id, message=message, status="PROCESSING")
        self.db.add(new_notification)
        await self.db.commit()
        await self.db.refresh(new_notification)
        
        logger.info(f"NotificationCreated id={new_notification.id} userId={user_id}")

        # 4. Cache Update
        response_model = NotificationResponse.model_validate(new_notification)
        await CacheService.put(response_model)

        # 5. Publish to Kafka
        await KafkaProducerService.send_notification(str(new_notification.id))
        logger.info(f"KafkaEnqueued id={new_notification.id}")

        # 6. Update Idempotency with final ID
        if idempotency_key:
            await IdempotencyService.reserve(idempotency_key, str(new_notification.id))

        return f"Queued Notification ID: {new_notification.id}"

    async def get_status(self, notification_id: int):
        # Check Cache
        cached = await CacheService.get(notification_id)
        if cached:
            return cached

        # Check DB
        notification = await self.db.get(Notification, notification_id)
        if notification:
            return notification
        return None
