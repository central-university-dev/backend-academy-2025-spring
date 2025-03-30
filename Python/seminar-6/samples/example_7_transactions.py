import asyncio

from samples.base import AsyncSessionLocal
from samples.example_1_declarative_base import User, Profile


async def savepoint_example():
    async with AsyncSessionLocal() as session:
        async with session.begin():
            user = User(username="savepoint_user")
            session.add(user)

            savepoint = await session.begin_nested()  # Создание savepoint
            session.add(Profile(user_id=user.id, bio="Nested Savepoint"))
            await savepoint.rollback()  # Откат savepoint

            await session.commit()  # Основной коммит


if __name__ == '__main__':
    asyncio.run(savepoint_example())