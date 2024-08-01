# Start with a base image containing Java runtime (Java 21 is not officially available, using Java 17)
FROM amazoncorretto:22-alpine3.19-jdk

# The application's jar file
ARG JAR_FILE=application/target/*.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

# Copy positions.json file to /app directory in the container
COPY positions.json positions.json

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file 
ENTRYPOINT ["java","-jar","/app.jar"]