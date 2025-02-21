import grpc
import streaming_pb2
import streaming_pb2_grpc


def run():
    channel = grpc.insecure_channel("localhost:50051")
    stub = streaming_pb2_grpc.StreamingServiceStub(channel)

    request = streaming_pb2.NumberRequest(count=5)
    response_stream = stub.GetNumbers(request)

    print("Receiving streamed numbers:")
    for response in response_stream:
        print(response.number)


if __name__ == "__main__":
    run()
