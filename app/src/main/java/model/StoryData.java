package model;

import java.util.List;

public class StoryData {

    private List<CharacterName> characters;
    private String story;

    public List<CharacterName> getCharacters() {
        return characters;
    }

    public void setCharacters(List<CharacterName> characters) {
        this.characters = characters;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }
}
