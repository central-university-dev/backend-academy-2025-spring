import asyncio
import websockets
import json


async def send_messages(websocket):
    while True:
        message = await asyncio.get_event_loop().run_in_executor(None, input, "Your message: ")
        if message.lower() == "quit":
            break
        await websocket.send(message)


async def receive_messages(websocket):
    async for message in websocket:
        data = json.loads(message)
        print(f"\n{data['sender']}: {data['text']}")
        print("Your message: ", end="", flush=True)


async def client():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        # Receive username
        username_message = await websocket.recv()
        username_data = json.loads(username_message)
        if username_data["type"] == "username":
            print(f"You are {username_data['value']}")
        else:
            print("Error: did not receive username")
            return

        # Start send and receive tasks
        send_task = asyncio.create_task(send_messages(websocket))
        receive_task = asyncio.create_task(receive_messages(websocket))

        # Wait for send_task to complete
        await send_task
        # Cancel receive_task
        receive_task.cancel()
        try:
            await receive_task
        except asyncio.CancelledError:
            pass


if __name__ == "__main__":
    asyncio.run(client())
