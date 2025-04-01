import asyncio
import httpx
import time

import tqdm.asyncio


ENDPOINT = "https://www.anapioficeandfire.com/api/characters/{}"
POST_ENDPOINT = "http://localhost:8000/character"


async def get_info(idx: int, client: httpx.AsyncClient):
    resp = await client.get(ENDPOINT.format(idx))
    pay = resp.json()
    return {
        "name": pay["name"],
        "gender": pay["gender"],
        "culture": pay["culture"],
        "has_spouse": True if pay.get("has_spouse", False) != "" else False,
    }


async def post(payload, client: httpx.AsyncClient):
    await client.post(POST_ENDPOINT, json=payload)


async def main():
    tasks = []
    results = []
    async with httpx.AsyncClient(timeout=10) as client:
        for idx in range(1, 21):
            tasks.append(asyncio.create_task(get_info(idx, client)))

        # results = await asyncio.gather(*tasks)
        for task in tqdm.asyncio.tqdm.as_completed(tasks):
            result = await task
            results.append(result)

        tasks = []
        for res in results:
            tasks.append(asyncio.create_task(post(res, client)))

        # results = await asyncio.gather(*tasks)
        for task in tqdm.asyncio.tqdm.as_completed(tasks):
            await task

    return results


start = time.perf_counter()
names = asyncio.run(main())
end = time.perf_counter()
for name in names:
    print(f"Name: {name}")
print(end - start)
