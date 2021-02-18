
call mvn clean package
docker build -t martinnordberg/barlom .
docker run -it --rm -p:8080:8080 -p:8081:8081 martinnordberg/barlom
