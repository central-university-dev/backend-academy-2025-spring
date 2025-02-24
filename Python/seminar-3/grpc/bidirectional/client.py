import grpc
import bidirectional_pb2
import bidirectional_pb2_grpc


def chat_stream():
    while True:
        message = input()
        if message == "q":
            break
        yield bidirectional_pb2.ChatMessage(username="Client", message=message)


def run():
    channel = grpc.insecure_channel("localhost:50051")
    stub = bidirectional_pb2_grpc.ChatServiceStub(channel)

    responses = stub.Chat(chat_stream())

    for response in responses:
        print(f"Server: {response.message}")


if __name__ == "__main__":
    run()
