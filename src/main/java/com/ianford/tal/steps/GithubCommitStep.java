package com.ianford.tal.steps;

import com.ianford.tal.Pipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class GithubCommitStep implements PipelineStep {

    private static final Logger logger= LogManager.getLogger();
    @Override
    public void run() throws IOException {

        try {
            File tempDir = Files.createTempDirectory("github-stager-java-test").toFile();

            Git git = Git.cloneRepository()
                    .setURI("https://github.com/ian4d/stager-test.git")
                    .setDirectory(tempDir)
                    .setProgressMonitor(new TextProgressMonitor())
                    .call();

            logger.info("Cloned stager test! {}", git);

            String newFilePrefix = "new-file";// + UUID.randomUUID().toString();
            String newFileSuffix = "txt";// + UUID.randomUUID().toString();

            Path newFilePath = Files.createTempFile(tempDir.toPath(), newFilePrefix, newFileSuffix);

            logger.info("Created new temp file at path: {}", newFilePath);

            DirCache addNewFileResult = git.add()
                    .addFilepattern(newFilePath.getFileName().toString())
                    .call();

            logger.info("Added new file to git repo: {}", addNewFileResult);

            RevCommit revCommit = git.commit().setMessage("Added test file and now committing it").call();

            logger.info("Commit successful? {}", revCommit);

            PushCommand pushCommand = git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("ian4d", "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"));

            for (PushResult pushResult : pushCommand.call()) {
                logger.info("-- push result: {}", pushResult);
            }

            tempDir.deleteOnExit();

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    void createAndCommitLocalRepo() throws IOException {
        Repository repo = createRepo();


        File tempFile = new File(repo.getDirectory().getParent(), "testfile");
        tempFile.createNewFile();

        logger.info("Temporary file created: {}", tempFile);

        Git git = new Git(repo);

        logger.info("Git repo created: {}", git);

        try {
            DirCache addResult = git.add().addFilepattern(tempFile.getName()).call();

            logger.info("Added file to local repo: {}", addResult);

            RevCommit revCommit = git.commit().setMessage("Added test file and now committing it").call();

            logger.info("Committed file: {}", revCommit);

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        tempFile.deleteOnExit();
    }

    Repository createRepo() throws IOException {
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }
        logger.info("FILE CREATED: {}", localPath);


        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();
        return repository;
    }
}
