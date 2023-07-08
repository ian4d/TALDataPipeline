package com.ianford.podcasts.model.git;

public class GitConfiguration {
    private final String username;
    private final String password;
    private final String remote;

    private final String branch;
    private final String url;

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
