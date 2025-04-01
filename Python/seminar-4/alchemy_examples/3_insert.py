import asyncio

from sqlalchemy import insert

from alchemy_examples.alchemy_session import users, async_session


async def insert_user(name: str, email: str):
    async with async_session() as session:

        stmt = insert(users).values(name=name, email=email)

        await session.execute(stmt)
        await session.commit()


if __name__ == '__main__':
    asyncio.run(insert_user("Alice", "alice@example.com"))
