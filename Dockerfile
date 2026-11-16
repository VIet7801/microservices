FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
ARG MODULE
RUN mvn -B -pl ${MODULE} -am -Dmaven.test.skip=true package
RUN cp ${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
