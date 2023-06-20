FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar home/root/applications/shareit/shareit-server-app.jar
ENTRYPOINT ["java","-jar","/home/root/applications/shareit/shareit-server-app.jar"]
