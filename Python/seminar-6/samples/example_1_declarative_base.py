from sqlalchemy import Column, Integer, String, ForeignKey, Index
from sqlalchemy.orm import relationship

from samples.base import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True)
    username = Column(String, unique=True, nullable=False)
    profile = relationship("Profile", back_populates="user", uselist=False)
    posts = relationship("Posts", back_populates="user", uselist=True)

    __table_args__ = (
        Index('user_username_idx', username),  # создание индекса через аргументы
    )


class Profile(Base):
    __tablename__ = "profiles"

    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)
    bio = Column(String, index=True)  # Создание индекса через параметр
    user = relationship("User", back_populates="profile")


class Posts(Base):
    __tablename__ = "posts"

    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=False, nullable=False)
    post_message = Column(String, index=False)
    user = relationship("User", back_populates="posts")