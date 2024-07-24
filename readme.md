# Setting Up the Spring Project Locally

## Prerequisites

1. **DB Setup:**
    - Ensure that port is free
    - Navigate to the `docker` folder inside the project directory.
    - Run the following command to start the Docker container and set up the PostgreSQL database:

      ```bash
      cd docker
      docker-compose up
      ```

   This will use the `docker-compose.yml` file located in the `docker` folder.

## Running the Application

- Once the Docker container is up and running, you can start the application as usual.

## For Running Tests

- Ensure Docker is running and available to connections from java app, as the application uses the Testcontainers library for integration tests.
