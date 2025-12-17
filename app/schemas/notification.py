from pydantic import BaseModel, Field, ConfigDict
from typing import Optional

class NotificationBase(BaseModel):
    user_id: str = Field(..., alias="userId")
    message: Optional[str] = None

class NotificationCreate(NotificationBase):
    pass

class NotificationResponse(NotificationBase):
    id: int
    status: str

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)
