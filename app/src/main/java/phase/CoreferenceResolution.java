package phase;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.CorefChain.CorefMention;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import model.StoryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static util.Constants.ANIMATE_LIST;

public class CoreferenceResolution {

    private Logger logger = LoggerFactory.getLogger(CoreferenceResolution.class);

    public List<String> recogniseCharacterSentences(StoryData storyData) {

        // set up annotator
        StanfordCoreNLP pipeline = setUpCoreferencePipeline();
        Annotation document = pipeline.process(storyData.getStory());

        int numberOfCharacters = storyData.getCharacters().size();
        List<String> characterTextList = initializeAllCharacterTexts(numberOfCharacters);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CorefChain cc : document.get(CorefChainAnnotation.class).values()) {
            List<CorefMention> mentions = cc.getMentionsInTextualOrder();
            int characterIndex = checkMentionChainForAnyCharacter(storyData, mentions);
            if (characterIndex == -1) {
                // character not found in current mention chain
                continue;
            }
            updateCharacterTextList(characterIndex, characterTextList, sentences, mentions);
        }
        return characterTextList;
    }

    private void updateCharacterTextList(int characterIndex, List<String> characterTextList,
                                         List<CoreMap> sentences, List<CorefMention> mentions) {

        for (CorefMention mention : mentions) {
            // check if character is subject in that sentence
            CoreMap coreMap = sentences.get(mention.sentNum - 1);

            String characterText = characterTextList.get(characterIndex);

            // there can be multiple mentions in same sentence
            // if that case, such sentences would be added multiple times, once for every mention
            if (characterText.contains(coreMap.toString())) {
                return;
            }

            SemanticGraph semanticGraph = coreMap.get(CollapsedCCProcessedDependenciesAnnotation.class);
            IndexedWord verb = semanticGraph.getFirstRoot();
            IndexedWord subject = semanticGraph.getChildWithReln(verb,
                    GrammaticalRelation.valueOf(Language.UniversalEnglish, "nsubj"));

            // if subject is not found or if character is subject, add that sentence to text
            // very often, characters are both subjects and objects
            // we want only subjects - bad characters often do something bad to another character
            // both bad and good characters would get that same bad sentence
            if (subject == null || containsIgnoreCase(mention.mentionSpan, subject.word())) {
                String updatedText = characterText + " " + coreMap;
                characterTextList.set(characterIndex, updatedText);
            }
        }
    }

    private int checkMentionChainForAnyCharacter(StoryData storyData, List<CorefMention> mentions) {
        int numberOfCharacters = storyData.getCharacters().size();
        for (CorefMention mention : mentions) {
            for (int i = 0; i < numberOfCharacters; ++i) {
                for (String characterToken : storyData.getCharacters().get(i).getTokens()) {
                    String[] mentionWords = mention.mentionSpan.split("[\\.\\'\\,\\ ]+");
                    for (String mentionWord : mentionWords) {
                        if (equalsIgnoreCase(mentionWord, characterToken)) {
                            logger.info("Found character {} in mention '{}', mention chain is '{}'",
                                    storyData.getCharacters().get(i).getFullName(),
                                    mention.mentionSpan,
                                    mentions);
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private StanfordCoreNLP setUpCoreferencePipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, dcoref");
        props.setProperty("dcoref.use.big.gender.number", "false");
        props.setProperty("dcoref.animate", ANIMATE_LIST);
        return new StanfordCoreNLP(props);
    }

    // prepare character sentences list (character text) which will for each character hold it's story sentences
    private List<String> initializeAllCharacterTexts(int numberOfCharacters) {
        List<String> characterTextList = new ArrayList<>(numberOfCharacters);
        for (int i = 0; i < numberOfCharacters; ++i) {
            characterTextList.add("");
        }
        return characterTextList;
    }
}
