import asyncio
from typing import List

from sqlalchemy import func, Column, Integer, String, select
from sqlalchemy.ext.hybrid import hybrid_property
from samples.base import Base, AsyncSessionLocal


class UserHybrid(Base):
    __tablename__ = "users_hybrid"

    id = Column(Integer, primary_key=True, autoincrement=True)
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)

    @hybrid_property
    def full_name(self) -> str:
        return f"{self.first_name} {self.last_name}"

    @full_name.expression  # этот запрос будет использоваться при работе с этим проперти в SQL
    def full_name(self) -> str:
        return self.first_name + " " + self.last_name


async def get_users_with_full_name(full_name: str) -> List[UserHybrid]:
    async with AsyncSessionLocal() as session:
        stmt = select(UserHybrid).where(UserHybrid.full_name == full_name)
        result = await session.execute(stmt)
        users = result.scalars().all()
        print([x.full_name for x in users])


async def get_sorted_users():
    async with AsyncSessionLocal() as session:
        stmt = select(UserHybrid).order_by(UserHybrid.full_name.desc())
        result = await session.execute(stmt)
        users = result.scalars().all()
        print([x.full_name for x in users])


if __name__ == '__main__':
    asyncio.run(get_users_with_full_name('John Wick'))
    asyncio.run(get_sorted_users())
