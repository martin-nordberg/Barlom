
mvn clean package
docker build -t microk8s.mshome.net:32000/barlom .
docker push microk8s.mshome.net:32000/barlom
docker run -it --rm -p:8080:8080 -p:8081:8081 microk8s.mshome.net:32000/barlom
