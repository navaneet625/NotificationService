from fastapi import FastAPI
from app.core.config import settings
from app.api.routes import router as notification_router
from contextlib import asynccontextmanager
import asyncio
from app.db.session import engine, Base
from app.kafka.producer import KafkaProducerService
from app.kafka.consumer import consume_notifications

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    await KafkaProducerService.start()
    consumer_task = asyncio.create_task(consume_notifications())
    
    yield
    
    # Shutdown
    await KafkaProducerService.stop()
    consumer_task.cancel()
    try:
        await consumer_task
    except asyncio.CancelledError:
        pass

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
)

@app.get("/health")
def health_check():
    return {"status": "ok"}

app.include_router(notification_router, prefix="/api/notify", tags=["notifications"])
