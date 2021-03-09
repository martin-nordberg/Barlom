
mvn clean package
docker build -t microk8s.mshome.net:32000/barlom .

docker push microk8s.mshome.net:32000/barlom

kubectl delete -n default deployment barlom-app
kubectl delete -n default service barlom-app

kubectl apply -f barlom-service.yaml
