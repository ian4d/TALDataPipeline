package com.ianford.tal.model;

import org.eclipse.jgit.api.Git;

import java.nio.file.Path;

public class PipelineConfig {

    private Git localRepository;
    private Path workingDirectory;
    private Path localDownloadDirectory;
    private Path localParsedEpisodeDirectory;
    private Path localContributorDirectory;
    private Path localPostsDirectory;
    private Path contributorListFilepath;
    private Path episodeListFilepath;

    // List of modified episodes
    // Title
    // Description (full episode records?)
    // List of modified contributors
    // Name
    // Episodes?


    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Git getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(Git localRepository) {
        this.localRepository = localRepository;
    }

    public Path getLocalDownloadDirectory() {
        return localDownloadDirectory;
    }

    public void setLocalDownloadDirectory(Path localDownloadDirectory) {
        this.localDownloadDirectory = localDownloadDirectory;
    }

    public Path getLocalParsedEpisodeDirectory() {
        return localParsedEpisodeDirectory;
    }

    public void setLocalParsedEpisodeDirectory(Path localParsedEpisodeDirectory) {
        this.localParsedEpisodeDirectory = localParsedEpisodeDirectory;
    }

    public Path getLocalContributorDirectory() {
        return localContributorDirectory;
    }

    public void setLocalContributorDirectory(Path localContributorDirectory) {
        this.localContributorDirectory = localContributorDirectory;
    }

    public Path getContributorListFilepath() {
        return contributorListFilepath;
    }

    public void setContributorListFilepath(Path contributorListFilepath) {
        this.contributorListFilepath = contributorListFilepath;
    }

    public Path getEpisodeListFilepath() {
        return episodeListFilepath;
    }

    public void setEpisodeListFilepath(Path episodeListFilepath) {
        this.episodeListFilepath = episodeListFilepath;
    }

    public Path getLocalPostsDirectory() {
        return localPostsDirectory;
    }

    public void setLocalPostsDirectory(Path localPostsDirectory) {
        this.localPostsDirectory = localPostsDirectory;
    }
}
