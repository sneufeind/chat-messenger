FROM openjdk:8-jre-alpine
ADD build/libs /opt
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/chat-messenger.jar"]
EXPOSE [8080 9000]