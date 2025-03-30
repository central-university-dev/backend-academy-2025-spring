import asyncio

from sqlalchemy import select, func

from alchemy_examples.alchemy_session import users, async_session


async def count_users():
    async with async_session() as session:
        stmt = select(func.count()).select_from(users)

        result = await session.execute(stmt)

        return result.scalar()


if __name__ == '__main__':
    user = asyncio.run(count_users())
    print(user)
