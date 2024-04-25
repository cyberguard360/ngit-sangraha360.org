import psycopg2
import csv
import dotenv
import os


dotenv.load_dotenv()

# Establish a connection to the PostgreSQL database
conn = psycopg2.connect(
    dbname=os.getenv("DB_NAME"),
    user= os.getenv("DB_USER"),
    password= os.getenv("db_password"),
    host="localhost",
    port="5432"
)

# Create a cursor object using the cursor() method
cursor = conn.cursor()

# SQL query to fetch the data
sql_query = "SELECT * FROM account_dataentry;"

# Execute the SQL query
cursor.execute(sql_query)

# Fetch all the rows
rows = cursor.fetchall()

# Define the filename for the CSV file
csv_file = "output.csv"

# Write the fetched data into a CSV file
with open(csv_file, 'w', newline='') as f:
    writer = csv.writer(f)
    # Write the column headers
    writer.writerow([desc[0] for desc in cursor.description])
    # Write the rows
    writer.writerows(rows)

# Close the cursor and the connection
cursor.close()
conn.close()

print("Data exported successfully to", csv_file)
