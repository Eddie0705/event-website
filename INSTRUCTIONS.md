# RMIT COSC2299 SEPT Major Project Instructions

**DO NOT MODIFY THIS FILE**

## IMPORTANT

- You are not allowed to make this repository public or share its content out side of the github organization created for this course.

## Setup your environment

You will need to have in your system

- Java 17.0 or higher
- Apache Maven
- IDE or Editor

Other tools will be required to complete the project (e.g., Docker)

## Run Instructions

- Confirm you can run your application

```shell
./mvnw spring-boot:run
```

- Open in your browser [http://localhost:8080](http://xlxocalhost:8080)

- You will see a page with the message "Whitelabel Error Page"

- You can use the lectorials material to start developing your web application.



---

## Database Setup (Quick Guide)

1. Ensure **MySQL 8.0** is installed and running.
2. Save the provided SQL script as `eventhub_setup.sql`.
3. Run the script from your terminal:

```bash
mysql -u root -p < docs/Database-Setup/eventhub_setup.sql
```

Now the database should be ready to go

