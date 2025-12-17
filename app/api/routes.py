from fastapi import APIRouter, Depends, UploadFile, File, Header, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.session import get_db
from app.services.notification import NotificationService

from app.schemas import NotificationResponse
from typing import Optional

router = APIRouter()


@router.post("")
async def send_notification(
    userId: str, 
    msg: str, 
    idempotency_key: Optional[str] = Header(None, alias="Idempotency-Key"),
    db: AsyncSession = Depends(get_db)
):
    service = NotificationService(db)
    result = await service.send_notification(userId, msg, idempotency_key)
    
    if result.startswith("Rate limit") or result.startswith("Duplicate request"):
        raise HTTPException(status_code=429, detail=result)
        
    return result

@router.get("/status")
async def get_status(id: int, db: AsyncSession = Depends(get_db)):
    service = NotificationService(db)
    status = await service.get_status(id)
    if not status:
        raise HTTPException(status_code=404, detail="Notification not found")
    return status


