import asyncio

from samples.base import AsyncSessionLocal
from samples.example_1_declarative_base import User


# sqlite, не работает.
async def transfer_object():
    async with AsyncSessionLocal() as session1:
        user = User(username="transfer_user")
        session1.add(user)
        await session1.flush()  # Отправляет объект в БД без коммита

        async with AsyncSessionLocal() as session2:
            # await session1.rollback()
            user_merged = await session2.merge(user)  # Перенос объекта в новую сессию
            await session2.commit()


if __name__ == '__main__':
    asyncio.run(transfer_object())
