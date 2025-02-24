import grpc
from concurrent import futures
import client_streaming_pb2
import client_streaming_pb2_grpc


class ClientStreamingServiceServicer(client_streaming_pb2_grpc.ClientStreamingServiceServicer):
    def SendNumbers(self, request_iterator, context):
        total_sum = 0
        for request in request_iterator:
            print(f"Received number: {request.number}")
            total_sum += request.number
        return client_streaming_pb2.NumberResponse(sum=total_sum)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    client_streaming_pb2_grpc.add_ClientStreamingServiceServicer_to_server(ClientStreamingServiceServicer(), server)
    server.add_insecure_port("[::]:50051")
    server.start()
    print("Server started on port 50051...")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
