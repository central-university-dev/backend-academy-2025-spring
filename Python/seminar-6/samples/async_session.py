import asyncio

from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker

from samples.base import Base, engine
from example_1_declarative_base import User, Profile, Posts
from example_3_hybrid_property import UserHybrid


# Функция для создания таблиц
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


if __name__ == '__main__':
    asyncio.run(init_db())
