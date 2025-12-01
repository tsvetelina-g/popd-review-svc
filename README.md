# POPd Review Service

A Spring Boot-based REST API microservice for managing movie reviews. This service is part of the POPd movie review and rating platform, allowing users to write and manage movie reviews with optional ratings, retrieve reviews, and get comprehensive review statistics.

## Features

POPd Review Service provides:

- **Write Reviews** - Create or update movie reviews with title, content, and optional rating for any user and movie combination
- **Retrieve Reviews** - Get specific reviews by user and movie ID
- **Delete Reviews** - Remove reviews when needed
- **Movie Reviews** - Get the latest 5 reviews for a movie or paginated reviews
- **Movie Statistics** - Get total review count for movies
- **User Statistics** - Track how many movies a user has reviewed
- **Latest Reviews** - Retrieve a user's most recently created reviews (limited to 20)

## Tech Stack

- **Framework**: Spring Boot 3.4.0
- **Java Version**: 17
- **Database**: MySQL 8 (production), H2 (testing)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Validation**: Jakarta Validation
- **Utilities**: Lombok

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+** (included via Maven Wrapper)
- **MySQL 8.0+** (for production)
- **MySQL server** running on localhost:3306
- **IntelliJ IDEA** (recommended IDE)

## Installation & Setup

### Open the project in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select **File → Open**
3. Navigate to the project directory and select it
4. IntelliJ will automatically detect it as a Maven project and import dependencies

### Configure the database

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/popd_review_svc?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

The application will automatically create the database `popd_review_svc` if it doesn't exist.

### Wait for Maven to sync

- IntelliJ will automatically download dependencies
- Wait for the Maven sync to complete (check the bottom-right status bar)

## Running the Application

### Using IntelliJ IDEA

#### Locate the main class

Navigate to `src/main/java/app/popdreviewsvc/PopdReviewSvcApplication.java`

#### Run the application

1. Right-click on `PopdReviewSvcApplication.java`
2. Select **Run 'PopdReviewSvcApplication'**
3. Or use the green play button next to the main method
4. Or press `Shift + F10`

#### Access the application

- The application will be available at **http://localhost:8085**
- Check the IntelliJ console for startup logs and any errors

## Configuration

### Application Properties

Key configuration options in `application.properties`:

- **Database**: MySQL connection settings with auto-create database
- **JPA**: Hibernate DDL auto-update enabled
- **Server**: Runs on port 8085
- **Logging**: Configured for Hibernate entity persister errors

### Integration with POPd Application

This microservice is designed to be consumed by the main POPd MVC application via REST API calls. Ensure this service is running and accessible when using the full POPd platform.

## Testing

### Running Tests in IntelliJ IDEA

#### Run all tests

1. Right-click on the `src/test/java` folder
2. Select **Run 'All Tests'**
3. Or use `Ctrl + Shift + F10` (Windows/Linux) or `Cmd + Shift + R` (Mac)

#### Run a specific test class

1. Open the test file (e.g., `ReviewServiceUTest.java`, `ReviewControllerApiTest.java`)
2. Right-click on the class name or the file
3. Select **Run 'ClassName'**
4. Or click the green play button next to the class declaration

#### Run a specific test method

1. Click the green play button next to the test method
2. Or right-click on the method and select **Run 'methodName()'**

#### View test results

- Test results appear in the **Run** tool window at the bottom
- Green checkmarks indicate passed tests
- Red X marks indicate failed tests with error details

The test suite includes:

- **Unit tests** for services (`ReviewServiceUTest.java`)
- **Integration tests** for controllers (`ReviewControllerApiTest.java`)
- **End-to-end integration tests** (`UpsertReviewITest.java`)
- **Application context test** (`PopdReviewSvcApplicationTests.java`)

Test database uses H2 in-memory database (configured in `src/test/resources/application.properties`), so no database setup is required for running tests.

## Project Structure

```
src/
├── main/
│   ├── java/app/popdreviewsvc/
│   │   ├── model/          # Entity models (Review)
│   │   ├── repository/     # JPA repositories
│   │   ├── service/        # Business logic services
│   │   ├── web/            # REST controllers, DTOs, and mappers
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   └── mapper/     # DTO mappers
│   │   ├── exception/      # Custom exceptions
│   │   └── PopdReviewSvcApplication.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/               # Test classes
    └── resources/
        └── application.properties  # Test configuration (uses H2 in-memory DB)
```

## API Endpoints

All endpoints are prefixed with `/api/v1`.

### Review Management

- **POST** `/api/v1/reviews` - Create or update a review
  - Request body: `ReviewRequest` (userId, movieId, rating (optional), title (optional), content)
  - Response: `ReviewResponse` (201 Created)

- **GET** `/api/v1/reviews/{userId}/{movieId}` - Get a specific review by user and movie
  - Response: `ReviewResponse` (200 OK)

- **DELETE** `/api/v1/reviews/{userId}/{movieId}` - Delete a review
  - Response: 204 No Content

### Movie Reviews

- **GET** `/api/v1/reviews/{movieId}` - Get the latest 5 reviews for a movie
  - Response: `List<ReviewResponse>` (200 OK)

- **GET** `/api/v1/reviews/{movieId}/page` - Get paginated reviews for a movie
  - Query parameters: `page` (default: 0), `size` (default: 5)
  - Response: `Page<ReviewResponse>` (200 OK)

### Statistics

- **GET** `/api/v1/reviews/{movieId}/stats` - Get movie review statistics
  - Response: `MovieReviewStatsResponse` (allReviewsCount)

- **GET** `/api/v1/reviews/{userId}/user` - Get user review statistics
  - Response: `UserReviewsStatsResponse` (moviesReviewedCount)

- **GET** `/api/v1/reviews/{userId}/latest-reviews` - Get latest reviews by user (limited to 20)
  - Response: `List<ReviewResponse>` (200 OK)

### Error Handling

- **404 Not Found** - Returned when a review or resource is not found
  - Response: `ErrorResponse` with error message

## Notes

- Reviews include **title** (optional), **content** (required), and **rating** (optional integer)
- Each user can have only **one review per movie** (enforced by unique constraint)
- The service automatically tracks `createdOn` and `updatedOn` timestamps
- Database schema is automatically created/updated via Hibernate DDL auto mode
- The application runs on port **8085** by default
- Tests use an in-memory H2 database, so no database setup is required for running tests
- Latest reviews endpoint returns up to 20 reviews ordered by creation date (most recent first)
- Movie reviews endpoint returns the latest 5 reviews by default, or paginated results when using the `/page` endpoint

