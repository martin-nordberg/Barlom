
call mvn clean package
docker build -t martinnordberg/barlom .

docker push martinnordberg/barlom

kubectl delete -n default replicaset barlom
kubectl delete -n default service barlom

kubectl apply -f barlom-service.yaml
