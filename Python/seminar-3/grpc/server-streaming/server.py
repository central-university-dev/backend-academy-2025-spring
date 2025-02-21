import grpc
import time
from concurrent import futures
import streaming_pb2
import streaming_pb2_grpc


class StreamingServiceServicer(streaming_pb2_grpc.StreamingServiceServicer):
    def GetNumbers(self, request, context):
        for i in range(1, request.count + 1):
            yield streaming_pb2.NumberResponse(number=i)
            time.sleep(0.5)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    streaming_pb2_grpc.add_StreamingServiceServicer_to_server(StreamingServiceServicer(), server)
    server.add_insecure_port("[::]:50051")
    server.start()
    print("Server started on port 50051...")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
