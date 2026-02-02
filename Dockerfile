FROM maven:3.9-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /app/target/someone-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
# Мы запускаем приложение в режиме Web Server по умолчанию в докере, 
# так как GUI требует X11/дисплей.
CMD ["java", "-Djava.awt.headless=true", "-cp", "app.jar", "org.example.Main"]
