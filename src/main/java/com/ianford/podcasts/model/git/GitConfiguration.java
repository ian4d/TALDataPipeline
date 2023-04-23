package com.ianford.podcasts.model.git;

public class GitConfiguration {
    private final String username;
    private final String password;
    private final String remote;
    private final String url;

    public GitConfiguration(String username, String password, String remote, String url) {
        this.username = username;
        this.password = password;
        this.remote = remote;
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

    public String getUrl() {
        return url;
    }
}
