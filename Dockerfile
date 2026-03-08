# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom first to leverage docker cache
COPY pom.xml .
# if you use maven wrapper, copy it too (optional)
COPY mvnw .
COPY .mvn .mvn

# download deps (cacheable layer)
RUN mvn -q -DskipTests dependency:go-offline || true

# now copy sources and build
COPY src ./src
RUN mvn -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy the built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]