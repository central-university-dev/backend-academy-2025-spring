import asyncio

from sqlalchemy import select, update

from alchemy_examples.alchemy_session import users, async_session


async def update_user(name: str, new_email:str):
    async with async_session() as session:
        stmt = update(users).where(users.c.name == name).values(email=new_email)

        await session.execute(stmt)
        await session.commit()


if __name__ == '__main__':
    user = asyncio.run(update_user('Alice', 'new-alice@example.com'))
    print(user)
