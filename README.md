# NotificationService

docker-compose up -d  

mvn clean package
mvn spring-boot:run


java -jar target/notificationservice-0.0.1-SNAPSHOT.jar
http://localhost:8080/swagger-ui/index.html



# from local machine
scp -r ./notification_service ec2-user@EC2_PUBLIC_IP:~/notification_service


scp -i  -r ./notificationservice ubuntu@<EC2_IP>:/home/ubuntu/
