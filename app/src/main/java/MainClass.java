import trainer.CharacterRecogniserTrainer;
import solver.Solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static util.Constants.*;

public class MainClass {

    public static void main(String... args) {

        Solver solver = new Solver();
        Scanner scanner = new Scanner(System.in);
        boolean usePunctuation;

        // first phase - decide type of NER classifier to use
        System.out.println("Do you want to use NER classifiers trained with or without punctuation?\n" +
                "Please enter 'y' for model with punctuation or 'n' for model without punctuation.\n" +
                "If you want to exit, type 'q'.");
        while (true) {
            String input = scanner.next();
            String trimmedInput = input.trim();
            if (trimmedInput.equalsIgnoreCase("y")) {
                usePunctuation = true;
                break;
            } else if (trimmedInput.equalsIgnoreCase("n")) {
                usePunctuation = false;
                break;
            } else {
                System.out.println("Please enter 'y', 'n' or 'q'.");
            }
        }

        String modelLocation;
        if (usePunctuation) {
            modelLocation = NER_MODEL;
        }
        else {
            modelLocation = NER_MODEL_NO_PUNCTUATION;
        }

        // second phase - check if NER classifier exists
        if (!Files.exists(Paths.get(modelLocation))) {
            trainNewCharacterRecogniser(usePunctuation);
        } else {
            System.out.println("Existing NER model for character recognition found. \n" +
                    "Do you want to train new model or use old model? \n" +
                    "Please enter 'y' if you want to train new model or 'n' if you want to skip training.\n" +
                    "If you want to exit, type 'q'.");

            while (true) {
                String input = scanner.next();
                String trimmedInput = input.trim();
                if (trimmedInput.equalsIgnoreCase("q")) {
                    return;
                } else if (trimmedInput.equalsIgnoreCase("y")) {
                    trainNewCharacterRecogniser(usePunctuation);
                    break;
                } else if (trimmedInput.equalsIgnoreCase("n")) {
                    break;
                } else {
                    System.out.println("Please enter 'y', 'n' or 'q'.");
                }
            }
        }

        // third phase - solve for story
        System.out.println("Please enter file name without extension from '/src/main/resources/stories/', " +
                "e.g. 'snowwhite if file name is snowwhite.txt. \n" +
                "File should contain only story text, no titles, footnotes etc." +
                "\n" +
                "If you want to exit, type 'q'."
        );

        while (true) {
            String input = scanner.next();
            String trimmedInput = input.trim();
            if (trimmedInput.isEmpty()) {
                System.out.println("Filename must not be empty.");
                continue;
            }

            if (trimmedInput.equalsIgnoreCase("q"))
                return;

            String storyPath = STORIES + input + TXT;

            if (!Files.exists(Paths.get(storyPath))) {
                System.out.println("File does not exist! Please enter existing filename.");
                continue;
            }

            try {
                solver.solveForStory(storyPath, usePunctuation);
            } catch (IOException e) {
                System.out.format("Error while reading file: %s", e.getMessage());
            }
            System.out.println("\n\n");
            System.out.println("Enter next file name or 'q' to exit.");
        }
    }

    private static void trainNewCharacterRecogniser(boolean usePunctuation) {
        CharacterRecogniserTrainer trainer = new CharacterRecogniserTrainer();
        trainer.train(usePunctuation);
    }
}
