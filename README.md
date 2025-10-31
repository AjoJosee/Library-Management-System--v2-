Library Management system, UI by Swing

How to use?
1. Clone into the repo
2. Change the credentials in DatabaseHandler.java (Located inside src/main/java/com/library/database with correct MySQL credentials as per your configuration), setup assumes "root" user with
   password "root"

3. Run the 'testDB.sql' script in your MySQL client to set up the tables.
   (This script will also create some entries. You can update this as required)

   Example commands:
   - Using MySQL CLI (replace root/password as needed):
         "mysql -u root -p < src/testDB.sql"
   - Inside MySQL shell:
     - "SOURCE C:/full/path/to/testDB.sql;"

4. Build and run:
      "java -jar target/library-management-system-1.0.0.jar"
OR: open IntelliJ or any other ide's and run the library management system file
