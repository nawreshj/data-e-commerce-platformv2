FROM openjdk:21

WORKDIR /app

# Le jar doit déjà être compilé AVANT le docker build
COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]