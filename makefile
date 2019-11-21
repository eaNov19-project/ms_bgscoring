img=islamahmad/eaproj-bgscoringms:1.0.2

# ===== Maven =====
maven-rebuild:
	mvn clean && mvn install

# ===== Docker =====
docker-build: maven-rebuild
	docker build -t ${img} .

docker-run:
	docker run -p 8080:8092 ${img}

docker-login:
	docker login

docker-push: docker-login docker-build
	docker push ${img}
k8-install:
	kubectl apply -f k8s-deploy.yaml

k8-delete:
	kubectl delete -f k8s-deploy.yaml

k8-restart: k8-delete k8-install



build:
	 mvn clean && mvn install && docker build -t ${img} . && docker push $(img)

config:
	kubectl apply -f k8s-config.yaml

deploy:
	kubectl apply -f k8s-deploy.yaml
