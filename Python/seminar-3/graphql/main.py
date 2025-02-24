import strawberry
from fastapi import FastAPI
from strawberry.fastapi import GraphQLRouter


@strawberry.type
class User:
    id: int
    name: str
    age: int


users = [User(id=1, name="Alice", age=25), User(id=2, name="Bob", age=30)]


@strawberry.type
class Query:
    @strawberry.field
    def get_users(self) -> list[User]:
        return users


schema = strawberry.Schema(query=Query)

####################

app = FastAPI()
graphql_app = GraphQLRouter(schema)
app.include_router(graphql_app, prefix="/graphql")

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
