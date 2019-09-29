package com.ianford.tal.steps;

import com.ianford.podcasts.model.git.GitConfiguration;
import com.ianford.tal.model.PipelineConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This step checks out the website from its Git repository and prepares the file system for what we'll be doing later.
 */
public class PrepareLocalRepoStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final GitConfiguration gitConfig;

    /**
     * Constructor.
     *
     * @param gitConfig
     */
    public PrepareLocalRepoStep(GitConfiguration gitConfig) {
        this.gitConfig = gitConfig;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {
        // TODO: Parameterize temp directory prefix

        logger.info("Preparing local repository with configuration: {}", gitConfig.toString());

        File tempDir = Files.createTempDirectory("github-stager")
                .toFile();
        tempDir.deleteOnExit();

        try (Git git = cloneGitRepo(tempDir,
                this.gitConfig.getUrl())) {
            pipelineConfig.setLocalRepository(git);
            pipelineConfig.setWorkingDirectory(tempDir.toPath());

            // Prepare actual directories for output
            Path tempPath = tempDir.toPath();
            prepareSubdirectory(tempPath,
                    pipelineConfig.getLocalContributorDirectory());
            prepareSubdirectory(tempPath,
                    pipelineConfig.getLocalDownloadDirectory());
            prepareSubdirectory(tempPath,
                    pipelineConfig.getLocalParsedEpisodeDirectory());
            prepareSubdirectory(tempPath,
                    pipelineConfig.getLocalPostsDirectory());

            prepareFilePermissions(tempPath,
                    pipelineConfig.getEpisodeListFilepath());
            prepareFilePermissions(tempPath,
                    pipelineConfig.getContributorListFilepath());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes sure that the requested subdirectory exists, and that it is writeable.
     *
     * @param rootPath Path to resolve subdirectory from
     * @param subPath  Path to subdirectory
     * @throws IOException Thrown if directory cannot have permissions changed
     */
    private void prepareSubdirectory(Path rootPath, Path subPath) throws IOException {
        File subDirectory = Files.createDirectories(rootPath.resolve(subPath))
                .toFile();
        subDirectory.setReadable(true);
        subDirectory.setWritable(true);
    }

    /**
     * Makes sure the provided file is writeable and readable.
     * @param rootPath Path to resolve subdirectory from
     * @param filePath  Path to file
     */
    private void prepareFilePermissions(Path rootPath, Path filePath) {
        File file = rootPath.resolve(filePath)
                .toFile();
        file.setWritable(true);
        file.setReadable(true);
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
