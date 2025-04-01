import typing as tp
from fastapi import FastAPI, Depends
from sqlalchemy.ext.asyncio import AsyncSession
import logging

from app.db import get_session
from app.model import CharacterCreate, Character
from . import crud

app = FastAPI()
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


@app.get("/ping")
async def get_pong():
    return {"ping": "pong"}


@app.get("/character/{id}", response_model=Character)
async def get_characters_by_id(
    id: int, session: AsyncSession = Depends(get_session)
):
    result = await crud.get_characters_by_id(id, session)
    logger.debug(type(result))
    return result


@app.get("/character", response_model=tp.List[Character])
async def get_all_characters(session: AsyncSession = Depends(get_session)):
    result = await crud.get_all_characters(session)
    logger.debug(type(result))
    return result


@app.post("/character", response_model=Character)
async def post_character(
    payload: CharacterCreate, session: AsyncSession = Depends(get_session)
):
    result = await crud.post_character(payload, session)
    return result
