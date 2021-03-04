
mvn clean package
echo http://localhost:8081/liveness
echo http://localhost:8081/readiness
echo http://localhost:8081/config
echo http://localhost:8081/shutdown
java -jar ./target/barlom-1.0-SNAPSHOT-jar-with-dependencies.jar
