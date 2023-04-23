package com.ianford.tal.steps;

import com.ianford.podcasts.model.git.GitConfiguration;
import com.ianford.tal.model.PipelineConfig;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
                    .addFilepattern("_posts")
                    .call();

            // Build Commit
            String commitTemplate = loadCommitTemplate();
            String commitMessage = buildCommitMessage(commitTemplate);
            git.commit()
                    .setMessage(commitMessage)
                    .call();

            PushCommand pushCommand = git.push()
                    .setRemote(this.gitConfig.getRemote())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitConfig.getUsername(),
                            this.gitConfig.getPassword()));
            for (PushResult pushResult : pushCommand.call()) {
                logger.info("-- push result: {}",
                        pushResult);
            }
        } catch (GitAPIException ex) {
            throw new RuntimeException(ex);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a commit message for the git repo based on the provided template.
     *
     * @param template Template to use for the message
     * @return String contents of the commit.
     */
    private String buildCommitMessage(String template) {
        Map<String, String> tokenMap = new HashMap<>();
        // TODO: Parameterize commit title based on episode being committed
        tokenMap.put("title",
                "TITLE -- TODO");

        // TODO: Parameterize commit body based on episode being committed
        tokenMap.put("description",
                "DESCRIPTION -- TODO");
        StringSubstitutor commitSubstitutor = new StringSubstitutor(tokenMap);
        return commitSubstitutor.replace(template);
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
