FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY RadarBackend.java index.html ./
RUN javac RadarBackend.java
ENV PORT=8080
EXPOSE 8080
CMD ["java", "RadarBackend"]
