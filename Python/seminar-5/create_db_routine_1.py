from sqlalchemy import create_engine, Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from faker import Faker
import psycopg2

fake = Faker()

DB_USER = "postgres"
DB_PASSWORD = "pwd"
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "users_db"


def create_database():
    try:
        conn = psycopg2.connect(dbname="postgres", user=DB_USER, password=DB_PASSWORD, host=DB_HOST, port=DB_PORT)
        conn.autocommit = True
        cursor = conn.cursor()

        cursor.execute(f"CREATE DATABASE {DB_NAME}")
        print(f"Database {DB_NAME} created successfully")

        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error creating database: {e}")


engine = create_engine(f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")
Base = declarative_base()


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(255), nullable=False, unique=True)
    name = Column(String(100), nullable=False, index=True)
    age = Column(Integer)


def setup_database():
    Base.metadata.create_all(engine)
    print("Users table created successfully")

    Session = sessionmaker(bind=engine)
    session = Session()

    try:
        for _ in range(1900):
            user = User(email=fake.unique.email(), name=fake.name(), age=fake.random_int(min=18, max=80))
            session.add(user)

        session.commit()
        print("fake users inserted successfully")
    except Exception as e:
        session.rollback()
        print(f"Error inserting data: {e}")
    finally:
        session.close()


def verify_data():
    Session = sessionmaker(bind=engine)
    session = Session()

    users = session.query(User).all()
    print("\nUsers in database:")
    for user in users:
        print(f"ID: {user.id}, Name: {user.name}, Email: {user.email}, Age: {user.age}")

    session.close()


if __name__ == "__main__":
    create_database()
    setup_database()
    verify_data()
