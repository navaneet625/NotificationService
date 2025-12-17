from sqlalchemy import Column, Integer, String, BigInteger
from app.db.session import Base

class Notification(Base):
    __tablename__ = "notification"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    user_id = Column(String(255), nullable=False)
    message = Column(String(1000), nullable=True)
    status = Column(String(50), default="PROCESSING")
