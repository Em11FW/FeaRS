FROM openjdk:8-jre-alpine

# Copy war file
ADD build/libs/gs-rest-service-0.1.0.jar /fears/gs-rest-service-0.1.0.jar
ADD data /fears/data

# run the app
CMD ["java", "-jar", "/fears/gs-rest-service-0.1.0.jar"]
