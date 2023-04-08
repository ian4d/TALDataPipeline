package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Act {

    private final int actNumber;
    private String actName;
    private Map<String, Contributor> contributorMap;
    private List<Statement> statementList;

    public Act(int actNumber) {
        this.actNumber = actNumber;
        this.contributorMap = new HashMap<>();
        this.statementList = new ArrayList<>();
    }

    public int getActNumber() {
        return actNumber;
    }

    public String getActName() {
        return actName;
    }

    public Map<String, Contributor> getContributorMap() {
        return contributorMap;
    }

    public List<Statement> getStatementList() {
        return statementList;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public void setContributorMap(Map<String, Contributor> contributorMap) {
        this.contributorMap = contributorMap;
    }

    public void setStatementList(List<Statement> statementList) {
        this.statementList = statementList;
    }
}
