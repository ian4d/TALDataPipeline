package com.ianford.tal.steps;

import com.ianford.podcasts.model.GitConfiguration;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GithubCommitStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final GitConfiguration gitConfig;

    public GithubCommitStep(GitConfiguration gitConfig) {
        this.gitConfig = gitConfig;
    }


    @Override
    public void run() throws IOException {

        try {
            File tempDir = Files.createTempDirectory("github-stager")
                    .toFile();
            tempDir.deleteOnExit();

            try (Git git = cloneGitRepo(tempDir,
                    this.gitConfig.getUrl())) {

                // Update episode list
                updateEpisodeListInLocalRepository(tempDir);

                // Make sure blog post directory exists
                configureBlogPostPath();

                // Load post template
                String postTemplate = loadPostTemplate();
                String postContent = buildPostContents(postTemplate);

                // Write file to _posts directory
                String newPostFilename = writePostToLocalRepository(tempDir,
                        postContent);

                // Add posts and data to pending commit
                git.add()
                        .addFilepattern("_data")
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
            }


        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a new Jekyll post file to the local git repo directory.
     *
     * @param tempDir     Temporary directory where changes are being made.
     * @param postContent Text of the post to generate.
     * @return String Filename of the new post.
     *
     * @throws IOException Thrown if writing the post fails.
     */
    private String writePostToLocalRepository(File tempDir, String postContent) throws IOException {
        String postFilenameDatePrefix = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        // TODO: Generate post filename from details about actual episode being committed
        String postFilenameTitleInfix = "this-is-a-title";
        String newPostFilename = String.format("_posts/%s-%s.md",
                postFilenameDatePrefix,
                postFilenameTitleInfix);
        Path newPostPath = Path.of(tempDir.getPath() + File.separator + newPostFilename);
        Files.write(newPostPath,
                postContent.getBytes(StandardCharsets.UTF_8));
        return newPostFilename;
    }

    /**
     * Builds the body of a post to be committed to the blog.
     *
     * @param template Template to load for this post
     * @return String contents of the post
     */
    private String buildPostContents(String template) {
        Map<String, String> tokenMap = new HashMap<>();
        // TODO: Parameterize post layout using environment variables
        tokenMap.put("layout",
                "post");

        // TODO: Set post title based on episode being parsed
        tokenMap.put("title",
                "New Episode!");

        // TODO: Set post body based on episode being parsed.
        tokenMap.put("content",
                "Here is a description of the episode");
        StringSubstitutor postSubstitutor = new StringSubstitutor(tokenMap);
        return postSubstitutor.replace(template);
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
     * Makes sure that the _posts directory in our local git repo exists.
     *
     * @return Path to the _posts directory.
     */
    private Path configureBlogPostPath() {
        Path blogPostPath = Paths.get("_posts");
        blogPostPath.toFile()
                .mkdir();
        return blogPostPath;
    }

    /**
     * Loads the template being used for generating new Jekyll posts.
     *
     * @return String body of the template
     *
     * @throws IOException        Thrown if template can't be loaded
     * @throws URISyntaxException Thrown if URI of template is invalid
     */
    private String loadPostTemplate() throws IOException, URISyntaxException {
        return Files.readAllLines(Path.of(this.getClass()
                        .getClassLoader()
                        .getResource("jekyll/post_template.md")
                        .toURI()))
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
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

    /**
     * Updates the list of parsed episodes in our local repository.
     *
     * @param tempDir The directory where our local repository is held.
     * @return Path to current list of parsed episodes.
     *
     * @throws IOException Thrown if list can't be updated.
     */
    private Path updateEpisodeListInLocalRepository(File tempDir) throws IOException {
        // Get path to updated episode data
        Path updatedEpisodeListPath = Paths.get("_data/blog/episodeList.json");
        updatedEpisodeListPath.getParent()
                .toFile()
                .mkdirs();

        // get path to existing episode data in cloned git repo
        Path currentEpisodeListPath = Paths.get(tempDir.getPath() + File.separator + "_data/episodeList.json");
        currentEpisodeListPath.getParent()
                .toFile()
                .mkdirs();

        // Copy updated data into git repo at location of current data
        Files.copy(updatedEpisodeListPath,
                currentEpisodeListPath,
                StandardCopyOption.REPLACE_EXISTING);
        return currentEpisodeListPath;
    }

    /**
     * Clones the repo for our blog into `tempDir` from the repo at `endpoint`.
     *
     * @param tempDir  Directory to clone into.
     * @param endpoint Endpoint of the repo to clone.
     * @return Git
     *
     * @throws GitAPIException Thrown if repo can't be cloned.
     */
    private Git cloneGitRepo(File tempDir, String endpoint) throws GitAPIException {
        return Git.cloneRepository()
                .setURI(endpoint)
                .setDirectory(tempDir)
                .setProgressMonitor(new TextProgressMonitor())
                .call();
    }

}
