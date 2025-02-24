import grpc

import service_pb2
import service_pb2_grpc

channel = grpc.insecure_channel("localhost:50051")
stub = service_pb2_grpc.GreeterStub(channel)
response = stub.SayHello(service_pb2.HelloRequest(name="Alice"))
print(response.message)
