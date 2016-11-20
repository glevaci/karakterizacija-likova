package model;

import java.util.List;

public class CharacterName {

    private String fullName;

    private List<String> tokens;

    public CharacterName(String fullName, List<String> tokens) {
        this.fullName = fullName;
        this.tokens = tokens;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
