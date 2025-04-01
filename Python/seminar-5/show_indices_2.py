# read about indices types: https://habr.com/ru/companies/ruvds/articles/724066/
from sqlalchemy import create_engine, Table, Column, Integer, String, Index
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.postgresql import JSONB, TSVECTOR
from faker import Faker
import random

DB_USER = "postgres"
DB_PASSWORD = "pwd"
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "testdb"

Base = declarative_base()
engine = create_engine(f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")
Session = sessionmaker(bind=engine)
session = Session()
fake = Faker()

# Define the models
users = Table("users", Base.metadata, Column("name", String), Index("hash_name_idx", "name", postgresql_using="hash"))


class Article(Base):
    __tablename__ = "articles"
    id = Column(Integer, primary_key=True)
    tags = Column(JSONB)
    __table_args__ = (Index("gin_tags_idx", "tags", postgresql_using="gin"),)


class Location(Base):
    __tablename__ = "locations"
    id = Column(Integer, primary_key=True)
    coordinates = Column(TSVECTOR)
    __table_args__ = (Index("gist_coordinates_idx", "coordinates", postgresql_using="gist"),)


class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True)
    name = Column(String, index=True)
    category = Column(String)
    __table_args__ = (Index("idx_name_category", "name", "category"),)


Base.metadata.create_all(engine)


def generate_fake_data(num_records=1000):
    for _ in range(num_records):
        user_data = {"name": fake.name()}
        session.execute(users.insert().values(**user_data))

    for _ in range(num_records):
        tags = {
            "keywords": fake.words(nb=3),
            "category": fake.random_element(elements=("tech", "news", "sports", "food")),
            "priority": random.randint(1, 5),
        }
        article = Article(tags=tags)
        session.add(article)

    for _ in range(num_records):
        lat = fake.latitude()
        lon = fake.longitude()
        coord_str = f"'{lat} {lon}'"  # Format for TSVECTOR
        location = Location(coordinates=coord_str)
        session.add(location)

    categories = ["Electronics", "Clothing", "Books", "Home", "Toys"]
    for _ in range(num_records):
        product = Product(name=fake.name(), category=random.choice(categories))
        session.add(product)

    session.commit()
    print(f"Successfully added {num_records} fake records to each table")


if __name__ == "__main__":
    try:
        generate_fake_data()
    except Exception as e:
        print(f"An error occurred: {e}")
        session.rollback()
    finally:
        session.close()
