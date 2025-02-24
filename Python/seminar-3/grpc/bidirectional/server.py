import grpc
import time
from concurrent import futures
import bidirectional_pb2
import bidirectional_pb2_grpc


class ChatServiceServicer(bidirectional_pb2_grpc.ChatServiceServicer):
    def Chat(self, request_iterator, context):
        for request in request_iterator:
            print(f"Received from {request.username}: {request.message}")
            response = bidirectional_pb2.ChatMessage(username="Server", message=f"Echo: {request.message}")
            yield response
            time.sleep(1)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    bidirectional_pb2_grpc.add_ChatServiceServicer_to_server(ChatServiceServicer(), server)
    server.add_insecure_port("[::]:50051")
    server.start()
    print("Chat server started on port 50051...")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
