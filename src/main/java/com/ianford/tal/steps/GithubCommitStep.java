package com.ianford.tal.steps;

import com.ianford.podcasts.model.git.GitConfiguration;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.tal.model.PipelineConfig;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes new commits to Github based on progress of data pipeline.
 */
public class GithubCommitStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final GitConfiguration gitConfig;

    public GithubCommitStep(GitConfiguration gitConfig) {
        this.gitConfig = gitConfig;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {

        try {

            // Add posts and data to pending commit
            Git git = pipelineConfig.getLocalRepository();
            git.add()
                    .addFilepattern("_data/episodes")
                    .addFilepattern("_data/episodeList.json")
                    .addFilepattern("_data/contributors")
                    .addFilepattern("_data/contributorList.json")
                    .addFilepattern("_posts")
                    .call();

            // Build Commit

            String commitMessage = buildCommitMessage(pipelineConfig);
            git.commit()
                    .setMessage(commitMessage)
                    .call();

            CredentialsProvider gitCreds = new UsernamePasswordCredentialsProvider(this.gitConfig.getUsername(),
                    this.gitConfig.getPassword());
            PushCommand pushCommand = git.push()
                    .setCredentialsProvider(gitCreds);

            pushCommand.call()
                    .forEach(result -> logger.info(result.getRemoteUpdates()));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Builds a commit message for the git repo based on the provided template.
     *
     * @param pipelineConfig Config object for this data pipeline
     * @return String contents of the commit.
     */
    private String buildCommitMessage(PipelineConfig pipelineConfig) throws IOException, URISyntaxException {

        String commitTemplate = loadCommitTemplate();
        Map<String, String> tokenMap = new HashMap<>();

        List<BlogEpisode> blogEpisodeList = pipelineConfig.getParsedEpisodes()
                .stream()
                .flatMap(parsedEpisode -> parsedEpisode.getEpisodeMap()
                        .values()
                        .stream())
                .sorted((ep1, ep2) -> Integer.compare(ep1.getEpisodeNumber(),
                        ep2.getEpisodeNumber()))
                .collect(Collectors.toList());

        String commitTitle = String.format("Episodes: %s",
                blogEpisodeList.stream()
                        .map(blogEpisode -> blogEpisode.getEpisodeNumber())
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")));
        String commitMessage = blogEpisodeList.stream()
                .map(blogEpisode -> String.format("%s - %s",
                        blogEpisode.getEpisodeNumber(),
                        blogEpisode.getEpisodeTitle()))
                .collect(Collectors.joining("\n"));

        tokenMap.put("title",
                commitTitle);
        tokenMap.put("description",
                commitMessage);
        StringSubstitutor commitSubstitutor = new StringSubstitutor(tokenMap);
        return commitSubstitutor.replace(commitTemplate);
    }


    /**
     * Loads the template being used for new git commits.
     *
     * @return String body of the template
     *
     * @throws IOException        Thrown if template can't be loaded
     * @throws URISyntaxException Thrown if URI of template is invalid
     */
    private String loadCommitTemplate() throws IOException, URISyntaxException {
        return Files.readAllLines(Path.of(this.getClass()
                        .getClassLoader()
                        .getResource("github/commit_template.txt")
                        .toURI()))
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
