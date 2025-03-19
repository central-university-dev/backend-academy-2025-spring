from sqlalchemy import Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    family_name = Column(String, nullable=False)

class Customer(Base):
    __tablename__ = "cust"
    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)

