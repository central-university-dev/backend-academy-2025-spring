import grpc
from concurrent import futures

import service_pb2
import service_pb2_grpc


class GreeterServicer(service_pb2_grpc.GreeterServicer):
    def SayHello(self, request, context):
        return service_pb2.HelloReply(message=f"Hello, {request.name}!")


server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
service_pb2_grpc.add_GreeterServicer_to_server(GreeterServicer(), server)
server.add_insecure_port("[::]:50051")
server.start()
server.wait_for_termination()
