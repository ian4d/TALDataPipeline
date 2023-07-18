package com.ianford.podcasts.model.git;

public class GitConfiguration {
    private final String username;
    private final String password;
    private final String remote;

    private final String branch;
    private final String url;

    /**
     * Holds information about the Git repo we'll be committing to.
     *
     * @param username Username of the user who will push the changes.
     * @param password Password to authenticate the user
     * @param remote   Remote to get the repo from
     * @param branch   Name of the branch to use in the repo
     * @param url      URL of the remote repo
     */
    public GitConfiguration(String username, String password, String remote, String branch, String url) {
        this.username = username;
        this.password = password;
        this.remote = remote;
        this.branch = branch;
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRemote() {
        return remote;
    }

    public String getBranch() {
        return branch;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "GitConfiguration{" +
                "username='" + username + '\'' +
                ", remote='" + remote + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
