from faker import Faker
import sqlite3

# Connect to the database
conn = sqlite3.connect("example.db")

# Create a cursor object
cur = conn.cursor()

# Create a Faker instance
fake = Faker()

# Generate fake data and insert it into the database
for i in range(1000):
    name = fake.name()
    email = fake.email()
    sql = """INSERT INTO users(id,name,email)
             VALUES(?,?,?) """
    cur.execute(sql, (i + 1, name, email))

# Commit the changes
conn.commit()

# Close the cursor and connection
cur.close()
conn.close()

