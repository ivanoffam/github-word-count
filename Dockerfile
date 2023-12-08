FROM openjdk:11-jre-slim
WORKDIR /github-word-count
COPY out/artifacts/github_word_count_jar/github-word-count.jar .
CMD ["java", "-jar", "github-word-count.jar"]