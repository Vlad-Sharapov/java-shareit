FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar home/root/applications/shareit/shareit-gateway-app.jar
ENTRYPOINT ["java","-jar","/home/root/applications/shareit/shareit-gateway-app.jar"]

