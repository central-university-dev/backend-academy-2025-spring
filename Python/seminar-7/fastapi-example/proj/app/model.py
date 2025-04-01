import typing as tp

from sqlmodel import SQLModel, Field


class CharacterBase(SQLModel):
    name: tp.Optional[str] = None
    gender: tp.Optional[str] = None
    culture: tp.Optional[str] = None
    has_spouse: bool


class CharacterCreate(CharacterBase):
    pass


class Character(CharacterBase, table=True):
    id: int = Field(primary_key=True)
