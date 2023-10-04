package com.ianford.tal.model;

import com.ianford.podcasts.model.ParsedEpisode;
import org.eclipse.jgit.api.Git;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PipelineConfig {

    private Git localRepository;
    private Path workingDirectory;
    private Path localDownloadDirectory;
    private Path localParsedEpisodeDirectory;
    private Path localContributorDirectory;
    private Path localPostsDirectory;
    private Path contributorListFilepath;
    private Path episodeListFilepath;

    private List<Path> downloadedEpisodes = new ArrayList<>();

    private List<ParsedEpisode> parsedEpisodes = new ArrayList<>();

    private Set<String> contributors = new HashSet<>();

    private Optional<Integer> optionalTargetEpisode = Optional.empty();

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

    public List<Path> getDownloadedEpisodes() {
        return downloadedEpisodes;
    }

    public void setDownloadedEpisodes(List<Path> downloadedEpisodes) {
        this.downloadedEpisodes = downloadedEpisodes;
    }

    public List<ParsedEpisode> getParsedEpisodes() {
        return parsedEpisodes;
    }

    public void setParsedEpisodes(List<ParsedEpisode> parsedEpisodes) {
        this.parsedEpisodes = parsedEpisodes;
    }

    public Set<String> getContributors() {
        return contributors;
    }

    public void setContributors(Set<String> contributors) {
        this.contributors = contributors;
    }

    /**
     * Builds a path appropriate for episode storage for this episode
     *
     * @param episodeNumber
     * @return
     */
    public Path buildPathForEpisode(int episodeNumber) {
        return this.workingDirectory
                .resolve(this.localParsedEpisodeDirectory)
                .resolve(String.format("episode-%s.json",
                        episodeNumber));
    }

    public Optional<Integer> getOptionalTargetEpisode() {
        return optionalTargetEpisode;
    }

    public void setOptionalTargetEpisode(Optional<Integer> optionalTargetEpisode) {
        this.optionalTargetEpisode = optionalTargetEpisode;
    }
}
