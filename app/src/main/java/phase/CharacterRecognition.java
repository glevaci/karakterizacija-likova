package phase;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import model.CharacterName;
import model.StoryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static util.Constants.NER_MODEL;
import static util.Constants.NER_MODEL_NO_PUNCTUATION;

public class CharacterRecognition {

    private Logger logger = LoggerFactory.getLogger(CharacterRecognition.class);

    public StoryData setUpStoryAndCharacters(String path, boolean modelWithPunctuation) throws IOException {

        // load NER model
        NERClassifierCombiner classifier;
        if (modelWithPunctuation) {
            classifier = new NERClassifierCombiner(false, false, NER_MODEL);
        }
        else {
            classifier = new NERClassifierCombiner(false, false, NER_MODEL_NO_PUNCTUATION);
        }
        // read story text
        String storyText = IOUtils.slurpFile(path);

        // tag story with NER tags
        List<List<CoreLabel>> taggedText = classifier.classify(storyText);
        List<CharacterName> characters = new ArrayList<>();

        for (List<CoreLabel> sentence : taggedText) {
            for (int i = 0; i < sentence.size(); ++i) {
                CoreLabel firstLabel = sentence.get(i);
                String tag = firstLabel.ner();
                String name = firstLabel.value();
                if (tag.equals("O")) {
                    continue;
                }

                List<String> tokens = new ArrayList<>();
                tokens.add(name.toLowerCase());
                CoreLabel nextLabel = sentence.get(i + 1);

                // read all neighbouring character tokens
                // they all belong to single character
                while (nextLabel.ner().equals("C")) {
                    name += " " + nextLabel.value();
                    tokens.add(nextLabel.value().toLowerCase());
                    ++i;
                    if (i >= sentence.size() - 1) {
                        break;
                    }
                    nextLabel = sentence.get(i + 1);
                }
                updateCharacterList(name, tokens, characters);
            }
        }

        StoryData storyData = new StoryData();
        storyData.setCharacters(characters);
        storyData.setStory(storyText);
        logger.info("File '{}' successfully read.", path);
        logger.info("Found characters:\n{}", buildNiceCharactersString(characters));
        return storyData;
    }

    private String buildNiceCharactersString(List<CharacterName> characters) {
        return characters.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    private void updateCharacterList(String name, List<String> tokens, List<CharacterName> characters) {
        for (CharacterName character : characters) {
            // already in list: Red Riding Hood
            // now found: Little Red Riding Hood
            // update character name
            if (tokens.containsAll(character.getTokens())) {
                character.setFullName(name);
                character.setTokens(tokens);
                return;
            }
            // opposite:
            // already in list: Little Red Riding Hood
            // now found: Red Riding Hood
            // skip
            if (character.getTokens().containsAll(tokens)) {
                return;
            }
        }
        // other situation - completely new character
        CharacterName newCharacter = new CharacterName(name, tokens);
        characters.add(newCharacter);
    }
}
