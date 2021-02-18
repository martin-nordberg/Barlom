FROM adoptopenjdk:11-jre-hotspot
RUN mkdir /opt/app
COPY target/barlom-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/app
# EXPOSE 8080
# EXPOSE 8081
CMD ["java", "-jar", "/opt/app/barlom-1.0-SNAPSHOT-jar-with-dependencies.jar"]
