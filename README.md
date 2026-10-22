# EventHub

An event discovery and RSVP web app built as a 6-person team project for **RMIT COSC2299 Software Engineering: Process and Tools** (Semester 2, 2025, Group P03-03).

Users can browse and search upcoming events, RSVP and cancel, and track events on a calendar. Organisers create and manage their own events, and admins moderate the platform.

> This is my personal copy of the team repository, kept for my portfolio. The full commit history from the original repo is preserved, including all branches and the milestone tags.

## Tech stack

- **Backend:** Java 17, Spring Boot (Web, Security, Data JPA, Validation, Mail)
- **Database:** MySQL 8
- **Frontend:** Thymeleaf templates (Layout Dialect), HTML/CSS
- **Testing:** JUnit and Spring Boot Test (controller and service tests)
- **DevOps:** Docker, Docker Compose, GitHub Actions CI
- **Process:** Agile/Scrum across 3 milestones, feature branches with pull requests, `dev` → `test` → `prod` branch flow

## My contributions (Yong Tian Goh)

I worked mainly on the user-facing pages and the event search feature:

- **Event search and filtering:** built keyword search across event titles and descriptions, plus filtering by date and location. The filters are implemented as Spring Data JPA queries in `EventRepository`, wired through `EventService` and the controllers to the Explore page. I also fixed search being unavailable to logged-in users. (`search_filter` branch)
- **Explore page:** designed the page and its filter dropdown, then connected it to the backend so it lists live events from the database.
- **Landing page:** built the initial routing, the page template and the CSS design.
- **Login and logout:** built the login page UI and implemented the logout controller.
- **Session-aware navigation:** the nav bar shows or hides the logout button and profile dropdown based on whether the user is logged in.
- **Event cards:** wrote the reusable Thymeleaf fragment that displays events in a grid.

Main files I worked in:
- `controller/PageController.java`
- `controller/EventController.java`
- `services/EventService.java`
- `repository/EventRepository.java`
- `templates/Index.html`
- `templates/fragments/eventCards.html`
- `static/css/style.css`

To see my commits:

```bash
git log --all --author="Eddie-Goh"
```

## Team

| Member | Student ID |
|---|---|
| Harry Marshall | s4015845 |
| Luca Grosso | s4093817 |
| Khanh Nguyen | s4090097 |
| Athrinandana Vase | s4007366 |
| Halbert Setiawan | s3962208 |
| Yong Tian Goh | s4025884 |

## Project structure

```
src/main/java/au/edu/rmit/sept/webapp/
  controller/   Admin, Auth, Calendar, Event, Organiser, Page, RSVP, User
  services/     Business logic
  repository/   Spring Data JPA repositories
  models/       JPA entities
  config/       Spring Security config
src/main/resources/templates/   Thymeleaf views (admin, auth, events, organiser, rsvp, user, calendar)
docs/                           SRS, architecture, UI/UX wireframes, milestone docs, meeting minutes
```

## Running locally

### With Docker Compose (app + MySQL + Adminer)

```bash
export DB_PASSWORD=yourpassword
docker compose -f dockercompose.yml up --build
```

- App: http://localhost:8080
- Adminer (DB viewer): http://localhost:9000

### App container only (MySQL running on your machine)

```bash
docker build --platform linux/amd64 -t eventhub-webapp .
docker run -d \
  --platform linux/amd64 \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  --name eventhub-app \
  eventhub-webapp
```

### Without Docker

Needs Java 17 and a local MySQL database called `eventhub_db`. The connection settings are in `src/main/resources/application.properties`.

```bash
./mvnw spring-boot:run
```

Run the tests with:

```bash
./mvnw test
```

## Docs

The `docs/` folder holds the course deliverables: the software requirements spec, architecture diagrams, UI/UX wireframes, database setup, Definition of Done, meeting minutes and the per-milestone documentation.
