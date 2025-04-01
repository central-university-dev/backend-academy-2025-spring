import asyncio
import sys

from sqlalchemy import select
from sqlalchemy.orm import Query

from samples.base import AsyncSessionLocal
from samples.example_1_declarative_base import User


# Create
async def add_user(username: str):
    async with AsyncSessionLocal() as session:
        async with session.begin():
            user = User(username=username)
            session.add(user)
            await session.commit()


# Read
async def get_user(username: str) -> User | None:
    async with AsyncSessionLocal() as session:
        async with session.begin():
            user_query = select(User).where(User.username == username)
            result = await session.execute(user_query)
            return result.scalar()


# Update(нерабочий)
async def update_user(username: str, new_username: str):
    user = await get_user(username)
    async with AsyncSessionLocal() as session:
        async with session.begin():
            user.username = new_username
            await session.commit()


# Update
async def update_user(username: str, new_username: str):
    user = await get_user(username)
    async with AsyncSessionLocal() as session:
        async with session.begin():
            session.add(user)
            user.username = new_username
            await session.commit()


# Delete
async def delete_user(username: str):
    user = await get_user(username)

    async with AsyncSessionLocal() as session:
        async with session.begin():
            await session.delete(user)
            await session.commit()


if __name__ == "__main__":
    asyncio.run(add_user("test_user"))
    print(asyncio.run(get_user("test_user")))
    asyncio.run(update_user("test_user", "test_user_new"))
    asyncio.run(delete_user('test_user_new'))
