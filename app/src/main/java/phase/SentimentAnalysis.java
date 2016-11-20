package phase;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SentimentAnalysis {

    private Logger logger = LoggerFactory.getLogger(SentimentAnalysis.class);

    public List<String> calculateCharacterSentiments(List<String> characterTextList) {

        StanfordCoreNLP pipeline = setUpSentimentPipeline();
        logger.info("Calculating sentiment for each character...");
        List<String> sentiments = new ArrayList<>();
        for (String text : characterTextList) {
            String sentiment = calculateSentiment(pipeline, text);
            sentiments.add(sentiment);
        }
        return sentiments;
    }

    private StanfordCoreNLP setUpSentimentPipeline() {
        Properties sentProperties = new Properties();
        sentProperties.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        return new StanfordCoreNLP(sentProperties);
    }

    private String calculateSentiment(StanfordCoreNLP pipeline, String text) {

        if (text.length() == 0) {
            logger.info("Text is empty - skipping sentiment analysis.");
            return "insufficient data for sentiment analysis";
        }

        logger.info("\nReceived text for sentiment analysis:\n{}", text);

        Annotation document = pipeline.process(text);

        double sum = 0;
        int count = 0;

        // predicted class can be one of:
        // O = very negative
        // 1 = negative
        // 2 = neutral
        // 3 = positive
        // 4 = very positive

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            if (sentiment >= 0) {
                sum += sentiment;
                ++count;
            }
            logger.info("\nSentence: {} \nSentiment: {}\n", sentence.toString(), sentiment);
        }

        // return average of sentence sentiments
        double value = sum / count;
        logger.info("Final sentiment numeric value: {}\n", value);

        // usually, neutral characters are considered good
        if (value <= 1.5)
            return "bad";
        else
            return "good";
    }
}
