import json
import redis.asyncio as redis
from app.core.config import settings
from app.schemas import NotificationResponse
from typing import Optional

class RedisClient:
    _client: Optional[redis.Redis] = None

    @classmethod
    def get_client(cls) -> redis.Redis:
        if cls._client is None:
            cls._client = redis.Redis(
                host=settings.REDIS_HOST,
                port=settings.REDIS_PORT,
                decode_responses=True
            )
        return cls._client

class RateLimiter:
    LIMIT = 60
    WINDOW = 60  # seconds

    @staticmethod
    async def allow_request(key: str) -> bool:
        client = RedisClient.get_client()
        redis_key = f"ratelimit:{key}"
        
        current = await client.incr(redis_key)
        if current == 1:
            await client.expire(redis_key, RateLimiter.WINDOW)
        
        return current <= RateLimiter.LIMIT

class IdempotencyService:
    TTL = 600  # 10 minutes

    @staticmethod
    async def reserve(idempotency_key: str, value: str) -> bool:
        client = RedisClient.get_client()
        key = f"idem:{idempotency_key}"
        return await client.set(key, value, ex=IdempotencyService.TTL, nx=True)

    @staticmethod
    async def get(idempotency_key: str) -> Optional[str]:
        client = RedisClient.get_client()
        return await client.get(f"idem:{idempotency_key}")

class CacheService:
    TTL = 300  # 5 minutes

    @staticmethod
    async def put(notification: NotificationResponse):
        client = RedisClient.get_client()
        key = f"notification:{notification.id}"
        await client.set(key, notification.model_dump_json(), ex=CacheService.TTL)

    @staticmethod
    async def get(notification_id: int) -> Optional[dict]:
        client = RedisClient.get_client()
        key = f"notification:{notification_id}"
        data = await client.get(key)
        if data:
            return json.loads(data)
        return None
