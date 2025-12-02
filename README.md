# POPd Review Service – Movie Review Microservice

The POPd Review Service is a Spring Boot REST API microservice for managing movie reviews. Users can write, update, delete, and retrieve reviews, optionally including a rating. The service integrates with the main POPd MVC application.

## Core Features

- **Create & Update Reviews** – Write or modify reviews with optional rating and title
- **Retrieve Reviews** – Get reviews by user and movie ID
- **Delete Reviews** – Remove reviews as needed
- **Movie Reviews** – Latest 5 reviews or paginated list per movie
- **Statistics** – Total review count per movie and per user
- **Latest Reviews** – Fetch the 20 most recent reviews by a user

## Tech Stack

- Java 17
- Spring Boot 3.4.0
- MongoDB (production & tests)
- Spring Data MongoDB
- Maven
- Jakarta Validation
- Lombok

## Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB running on localhost:27017
- IntelliJ IDEA recommended

## Setup Instructions

### 1. Configure MongoDB

In `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/reviewsdb
spring.data.mongodb.database=reviewsdb
```

The database `reviewsdb` will be created automatically on first write.

### 2. Run the service

- Open `PopdReviewSvcApplication.java` in IntelliJ
- Run the main method
- Service will start on **http://localhost:8085**

You can also run using Maven:
```bash
mvn spring-boot:run
```

## Testing

- Tests use a local MongoDB database (`reviewsdb_test`)
- Ensure MongoDB is running before executing tests
- Run all tests by right-clicking `src/test/java` → Run All Tests

Test types:
- **Unit tests** – service layer
- **Integration tests** – controller layer
- **End-to-end tests** – `UpsertReviewITest.java`

## Project Structure

```
src/
├── main/java/app/popdreviewsvc/
│   ├── model/          # Document models (Review)
│   ├── repository/     # MongoDB repositories
│   ├── service/        # Business logic services
│   ├── web/            # REST controllers, DTOs, mappers
│   │   ├── dto/
│   │   └── mapper/
│   ├── exception/      # Custom exceptions
│   └── PopdReviewSvcApplication.java
├── main/resources/
│   └── application.properties
└── test/
    ├── java/           # Test classes
    └── resources/      # MongoDB test configuration
```

## API Endpoints (prefix /api/v1)

### Reviews

- **POST** `/reviews` – Create or update a review
  - Body: `ReviewRequest` (userId, movieId, title optional, content, rating optional)
  - Response: `ReviewResponse` (201 Created)

- **GET** `/reviews/{userId}/{movieId}` – Get a review by user/movie
  - Response: `ReviewResponse` (200 OK)

- **DELETE** `/reviews/{userId}/{movieId}` – Delete a review
  - Response: 204 No Content

### Movie Reviews

- **GET** `/reviews/{movieId}` – Latest 5 reviews for a movie

- **GET** `/reviews/{movieId}/page` – Paginated reviews
  - Query params: `page` (default 0), `size` (default 5)

### Statistics

- **GET** `/reviews/{movieId}/stats` – Total reviews for a movie

- **GET** `/reviews/{userId}/user` – Total reviews by a user

- **GET** `/reviews/{userId}/latest-reviews` – 20 most recent reviews

### Error Handling

- **404 Not Found** – Review or resource not found
  - Response: `ErrorResponse` with message

## Notes

- Reviews include title (optional), content (required), and rating (optional integer)
- Each user can submit only one review per movie
- `createdOn` and `updatedOn` timestamps are automatically tracked
- MongoDB database and collections are auto-created on first write
- Service runs on port 8085
- Latest reviews endpoint returns up to 20 reviews (most recent first)
- Movie reviews endpoint returns latest 5 reviews or paginated results
