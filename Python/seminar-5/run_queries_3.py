from sqlalchemy import create_engine, Table, Column, Integer, String, Index, text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.postgresql import JSONB, TSVECTOR


DB_USER = "postgres"
DB_PASSWORD = "pwd"
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "testdb"

Base = declarative_base()
engine = create_engine(f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")
Session = sessionmaker(bind=engine)
session = Session()

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


def run_queries():
    def explain_query(query):
        result = session.execute(text(f"EXPLAIN ANALYZE {query}"))
        print("\nQuery Plan:")
        for row in result:
            print(row[0])

    print("\n=== Users Table Query (Hash Index) ===")
    user_query = "SELECT * FROM users WHERE name = 'John Doe'"
    print(f"Executing: {user_query}")
    result = session.execute(text(user_query)).fetchall()
    print(f"Found {len(result)} matching users")
    explain_query(user_query)

    print("\n=== Articles Table Query (GIN Index) ===")
    article_query = """
        SELECT * FROM articles 
        WHERE tags->>'category' = 'tech' 
        AND (tags->>'priority')::integer > 3
    """
    print(f"Executing: {article_query}")
    result = session.execute(text(article_query)).fetchall()
    print(f"Found {len(result)} matching articles")
    explain_query(article_query)

    print("\n=== Locations Table Query (GiST Index) ===")
    location_query = """
        SELECT * FROM locations 
        WHERE coordinates @@ to_tsquery('40 & -73')
    """
    print(f"Executing: {location_query}")
    result = session.execute(text(location_query)).fetchall()
    print(f"Found {len(result)} matching locations")
    explain_query(location_query)

    print("\n=== Products Table Query (Composite B-tree Index) ===")
    product_query = """
        SELECT * FROM products 
        WHERE name LIKE '%Solution%' 
        AND category = 'Electronics'
    """
    print(f"Executing: {product_query}")
    result = session.execute(text(product_query)).fetchall()
    print(f"Found {len(result)} matching products")
    explain_query(product_query)


if __name__ == "__main__":
    try:
        run_queries()
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        session.close()
