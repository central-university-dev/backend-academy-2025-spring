import psycopg2

if __name__ == '__main__':
    conn = psycopg2.connect("dbname=postgres user=postgres password=postgres host=localhost")
    cur = conn.cursor()

    cur.execute("SELECT * FROM users;")
    print(cur.fetchall())

    cur.close()
    conn.close()
