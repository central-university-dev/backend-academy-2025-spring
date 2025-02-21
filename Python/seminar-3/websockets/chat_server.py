import asyncio
import websockets
import json

connected = set()
username_map = {}
counter = 0


async def chat_handler(websocket):
    global counter
    counter += 1
    username = f"User{counter}"
    username_map[websocket] = username

    # Send username to client
    await websocket.send(json.dumps({"type": "username", "value": username}))

    # Broadcast join message
    join_message = json.dumps({"sender": "Server", "text": f"{username} has joined the chat"})
    for client in connected:
        await client.send(join_message)

    # Add to connected
    connected.add(websocket)

    try:
        async for message in websocket:
            sender = username_map[websocket]
            broadcast_message = json.dumps({"sender": sender, "text": message})
            for client in connected:
                await client.send(broadcast_message)
    except websockets.exceptions.ConnectionClosed:
        pass
    finally:
        # Broadcast leave message
        leave_message = json.dumps({"sender": "Server", "text": f"{username} has left the chat"})
        for client in connected:
            if client != websocket:
                await client.send(leave_message)
        # Remove from connected and username_map
        connected.remove(websocket)
        del username_map[websocket]


async def main():
    async with websockets.serve(chat_handler, "localhost", 8765):
        await asyncio.Future()  # run forever


if __name__ == "__main__":
    asyncio.run(main())
