FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/*.jar gestionepagamenti.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","gestionepagamenti.jar"]
