FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY VideoMiner/pom.xml VideoMiner/pom.xml
COPY VimeoMiner/pom.xml VimeoMiner/pom.xml
COPY YoutubeMiner/pom.xml YoutubeMiner/pom.xml

RUN mvn -B -ntp -pl VideoMiner -am dependency:go-offline

COPY VideoMiner VideoMiner
COPY VimeoMiner VimeoMiner
COPY YoutubeMiner YoutubeMiner

RUN mvn -B -ntp -pl VideoMiner -am -DskipTests clean package && \
    find VideoMiner/target -maxdepth 1 -name "*.jar" ! -name "*original*" -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
