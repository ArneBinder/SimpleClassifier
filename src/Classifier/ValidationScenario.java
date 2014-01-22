package Classifier;

import Classifier.bean.Exceptions;
import Classifier.ExtractionValidator;
import Classifier.bean.FeatureTypes;
import Classifier.bean.ValidateResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Arne on 22.01.14.
 */
public class ValidationScenario {
	public static void main(String[] args) throws Exceptions.SRLException, IOException {
		if (args.length < 4) {
			System.out.println("not enough arguments: <corpusFileName> <validateOutFolder> <crossFoldCount> <validationStatisticOutFileName> [<featureTypeFileName>]*");
		} else {
			Corpus corpus = ClassifierNB.readCorpusData(new File(args[0]));
			File valOut = new File(args[1]);
			int crossFoldCount = Integer.parseInt(args[2]);
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(args[3])));

			ExtractionValidator extractionValidator = new ExtractionValidator();
			ValidateResult validateResult;
			out.write(ValidateResult.getCaption() + "\n");
			if (args.length < 5) {
				validateResult = extractionValidator.performCrossValidation(corpus, crossFoldCount, valOut);
				validateResult.normalize(crossFoldCount);
				out.write(validateResult + "\n");
			} else {
				for (int i = 4; i < args.length; i++) {
					FeatureTypes.readFeatureTypesFromFile(args[i]);
					validateResult = extractionValidator.performCrossValidation(corpus, crossFoldCount, valOut);
					validateResult.normalize(crossFoldCount);
					out.write(validateResult + "\n");
				}
			}
			out.close();
		}

	}

}
