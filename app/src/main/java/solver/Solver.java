package solver;

import model.StoryData;
import phase.CharacterRecognition;
import phase.CoreferenceResolution;
import phase.SentimentAnalysis;

import java.io.IOException;
import java.util.List;

public class Solver {

    private CharacterRecognition characterRecognition = new CharacterRecognition();
    private CoreferenceResolution coreferenceResolution = new CoreferenceResolution();
    private SentimentAnalysis sentimentAnalysis = new SentimentAnalysis();

    public void solveForStory(String path, boolean usePunctuation) throws IOException {

        StoryData storyData = characterRecognition.setUpStoryAndCharacters(path, usePunctuation);
        List<String> characterTextList = coreferenceResolution.recogniseCharacterSentences(storyData);
        List<String> sentiments = sentimentAnalysis.calculateCharacterSentiments(characterTextList);
        System.out.println("-------------------------------------------------------------------");
        System.out.println("Results:");
        for (int i = 0; i < sentiments.size(); ++i) {
            System.out.format("%s: %s\n", storyData.getCharacters().get(i).getFullName(), sentiments.get(i));
        }
        System.out.println("-------------------------------------------------------------------");
    }
}