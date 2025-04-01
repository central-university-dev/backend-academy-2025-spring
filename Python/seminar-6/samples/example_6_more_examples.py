import asyncio

from sqlalchemy import func, select, desc

from samples.base import AsyncSessionLocal
from samples.example_1_declarative_base import User, Posts
from sqlalchemy import over


# CTE
async def get_top_users():
    async with AsyncSessionLocal() as session:
        cte_stmt = (
            select(User.id, User.username, func.count(Posts.id).label("post_count"))
            .join(Posts, Posts.user_id == User.id)
            .group_by(User.id)
            .order_by(desc("post_count"))
            .cte("top_users")
        )
        stmt = select(cte_stmt).limit(5)
        result = await session.execute(stmt)
        return result.scalars().all()


# Оконные функции
async def get_ranked_users():
    async with AsyncSessionLocal() as session:
        stmt = (
            select(
                User.id,
                User.username,
                func.rank().over(order_by=desc(User.id)).label("rank")
            )
        )
        result = await session.execute(stmt)
        return result.scalars().all()


from sqlalchemy import text


# RAW SQL
async def raw_sql_query(pattern: str):
    async with AsyncSessionLocal() as session:
        stmt = text("SELECT * FROM users WHERE username LIKE '{pattern}'".format(pattern=pattern))
        result = await session.execute(stmt)
        return result.fetchall()


# Subquery
async def get_users_with_latest_post():
    async with AsyncSessionLocal() as session:
        subq = (
            select(Posts.user_id, func.max(Posts.id).label("latest_post_id"))
            .group_by(Posts.user_id)
            .subquery()
        )
        stmt = select(User, Posts).join(subq, User.id == subq.c.user_id).join(Posts, Posts.id == subq.c.latest_post_id)
        result = await session.execute(stmt)
        return result.scalars().all()


if __name__ == '__main__':
    print(asyncio.run(get_top_users()))
    print(asyncio.run(get_ranked_users()))
    print(asyncio.run(raw_sql_query('test%')))
    print([x.username for x in asyncio.run(get_users_with_latest_post())])