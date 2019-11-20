# Parking-Java9
A small desktop app. DEBUG VERSION! -> There's no .jar file in here, the project is in process. \n
Main functionality: interaction with MySQL database using Hibernate. \n
To try this app you first have to configure src/hibernate.cfg.xml file: \n
  -change connection.driver_class, connection.url, connection.username and dialect properties if needed; \n
  -add connection.password property to set password (my local server doesn't have one). \n
Then compile all the .java files (from src directory) and run the main launching file: src/logic/ParkingMain.java. \n
The database structure is the following: \n
 -table "cars": fields: car_id, car_number, car_model, registered, parked, owner_id, car_parked_time; \n
 -table "owners": fields: owner_id, owner_name, owner_tel. \n
 The app uses several ways to make Hibernate queries to try them out and learn :) 
