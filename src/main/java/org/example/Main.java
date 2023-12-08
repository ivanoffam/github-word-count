package org.example;

public class Main {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "Spotify";
        GitHubWordCountProcessor processor = new GitHubWordCountProcessor(username);
        processor.processRepositories();
    }
}