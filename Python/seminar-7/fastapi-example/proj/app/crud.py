import typing as tp
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.model import CharacterCreate, Character


async def get_all_characters(session: AsyncSession) -> tp.List[Character]:
    result = await session.execute(select(Character))
    return result.scalars().all()


async def get_characters_by_id(idx: int, session: AsyncSession) -> Character:
    result = await session.execute(select(Character).query(Character.id == id))
    return result.first()


async def post_character(
    payload: CharacterCreate, session: AsyncSession
) -> Character:
    character = Character(
        name=payload.name,
        gender=payload.gender,
        has_spouse=payload.has_spouse,
        culture=payload.culture,
    )
    session.add(character)
    await session.commit()
    await session.refresh(character)
    return character
