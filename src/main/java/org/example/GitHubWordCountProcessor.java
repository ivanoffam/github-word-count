package org.example;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class GitHubWordCountProcessor {
    private static String GITHUB_USERNAME;
    private static final String GITHUB_TOKEN = "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";
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\w-]+");
    private static final int MIN_WORD_LENGTH = 5;
    private static final int COUNT_POPULAR_WORDS = 3;
    private Map<String, Integer> wordCount = new HashMap<>();

    public GitHubWordCountProcessor(String username) {
        GITHUB_USERNAME = username;
    }

    public void processRepositories() {
        try {
            GitHub github = GitHub.connectUsingOAuth(GITHUB_TOKEN);
            GHUser user = github.getUser(GITHUB_USERNAME);
            PagedIterable<GHRepository> repositories = user.listRepositories();

            for (GHRepository repository : repositories) {
                processRepository(repository);
            }
            printResults();
        } catch (IOException e) {
            log.error("An error occurred during connecting to '{}' GitHub account: {}", GITHUB_USERNAME, e.getMessage());
        }
    }

    private void processRepository(GHRepository repository) {
        try {
            String content = getReadMeContent(repository);
            countWords(content);
            log.info("Successfully processed the README.md file from the repository '{}'.", repository.getName());
        } catch (IOException e) {
            String errorMessage = e.getMessage().contains("Not Found") ?
                    "The README.md file was not found in the repository '{}'." :
                    "An error occurred while processing the repository '{}': {}.";
            log.error(errorMessage, repository.getName(), e.getMessage());
        }
    }

    private void printResults() {
        System.out.println();

        if (wordCount.isEmpty()) {
            log.info("There are no README.md files for '{}'.", GITHUB_USERNAME);
            return;
        }
        log.info("Most popular words:");
        wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(COUNT_POPULAR_WORDS)
                .forEach(entry -> log.info("'{}' ({} occurrences)", entry.getKey(), entry.getValue()));
    }

    private String getReadMeContent(GHRepository repository) throws IOException {
        return repository.getReadme().getContent();
    }

    private void countWords(String content) {
        getMatcherResults(content)
                .map(matchResult -> matchResult.group().toLowerCase())
                .filter(word -> word.length() >= MIN_WORD_LENGTH)
                .forEach(word -> wordCount.put(word, wordCount.getOrDefault(word, 0) + 1));
    }

    private Stream<MatchResult> getMatcherResults(String content) {
        return WORD_PATTERN.matcher(content).results();
    }
}
