import grpc
import client_streaming_pb2
import client_streaming_pb2_grpc


def generate_numbers():
    numbers = [1, 2, 3, 4, 5]
    for num in numbers:
        print(f"Sending number: {num}")
        yield client_streaming_pb2.NumberRequest(number=num)


def run():
    channel = grpc.insecure_channel("localhost:50051")
    stub = client_streaming_pb2_grpc.ClientStreamingServiceStub(channel)

    response = stub.SendNumbers(generate_numbers())
    print(f"Server returned sum: {response.sum}")


if __name__ == "__main__":
    run()
