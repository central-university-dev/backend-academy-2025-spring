import asyncio

from sqlalchemy import select

from alchemy_examples.alchemy_session import users, async_session


async def get_user(email: str):
    async with async_session() as session:
        stmt = select(users).where(users.c.email == email)

        result = await session.execute(stmt)

        return result.fetchone()


if __name__ == '__main__':
    user = asyncio.run(get_user('alice@example.com'))
    print(user)
