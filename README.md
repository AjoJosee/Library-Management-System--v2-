
Remove folder resources which contains styles.css and pom.xml

Library Management system, UI by Swing

How to use?
1. Clone into the repo
2. Change the credentials in DatabaseHandler.java (Located inside src/main/java/com/library/database with correct MySQL credentials as per your configuration), setup assumes root user with
   password "root"

3. Run the 'database.sql' script in your MySQL client to set up the tables.
   (This script will also create default users: admin@library.com [pass: admin] and student@library.com [pass: student])

   Example commands:
   - Using MySQL CLI (replace root/password as needed):
     - Windows PowerShell/CMD: "mysql -u root -p < database.sql"
     - Optional sample data:    "mysql -u root -p < src/testDB.sql"
   - Inside MySQL shell:
     - "SOURCE C:/full/path/to/database.sql;" (try this if the command fails)
     - Optional: "SOURCE C:/full/path/to/src/testDB.sql;"

4. Build and run:
   - Build: "mvn clean package"
   - Run:   "java -jar target/library-management-system-1.0.0.jar"
OR: open IntelliJ or other ide's and run the librarry management system file

NB (Update as of Oct 3 10:51 PM IST) --> added another sql script that creates entries with dummy data, feel free to use that while testing my application
