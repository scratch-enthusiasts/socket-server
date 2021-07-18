# base image
FROM openjdk:16-jdk-alpine

# set working directory
WORKDIR /root/

# update os, install unzip
RUN apk update && \
    apk add unzip

# download jar from github
RUN wget https://github.com/scratch-enthusiasts/socket_server/raw/master/out/artifacts/musicparty/MusicParty.jar

# expose ports for various reasons such as database access
EXPOSE 5002
EXPOSE 3333
EXPOSE 3306

# run the jar
ENTRYPOINT ["java", "-Xmx1024m", "-jar", "MusicParty.jar"]
