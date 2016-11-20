package trainer;


import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static util.Constants.*;

public class CharacterRecogniserTrainer {

    private Logger logger = LoggerFactory.getLogger(CharacterRecogniserTrainer.class);

    public void train(Boolean usePunctuation) {

        logger.info("Started training of new NER model. Model includes punctuation: {}", usePunctuation);

        String location;
        Properties properties = StringUtils.propFileToProperties(TRAINING_PROPERTIES);
        if (usePunctuation) {
            properties.setProperty(TRAIN_FILE, TRAINING_DATA);
            location = NER_MODEL;
        }
        else {
            properties.setProperty(TRAIN_FILE, TRAINING_DATA_NO_PUNCTUATION);
            location = NER_MODEL_NO_PUNCTUATION;
        }
        SeqClassifierFlags flags = new SeqClassifierFlags(properties);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        crf.train();
        crf.serializeClassifier(location);
        logger.info("Training finished. Model saved to {}.", NER_MODEL);
    }
}
