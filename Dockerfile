# Use the official OpenJDK 17 base image
FROM openjdk:21-jdk

# Set the working directory inside the container
WORKDIR /

# Copy the JAR file into the container at /app
COPY target/rbac-iam.jar /rbac-iam.jar

# Expose the port the application runs on
EXPOSE 8051

# Specify the default command to run when the container starts
ENTRYPOINT ["java", "-jar", "/rbac-iam.jar"]