package Classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import Classifier.bean.*;
import Classifier.bean.Exceptions.*;
import com.rits.cloning.Cloner;


/**
 * Takes two annotated corpora and writes the result into a result file
 *
 * @author Robert
 */
public class ExtractionValidator {
	public static void main(String[] args) throws SRLException, IOException {
		if (args.length < 3) {
			throw new IllegalArgumentException("3 arguments needed:\n\t\t\t-cross <originalCorpus> <resultFolder> <type:[single,(number)]>\n\t\t\t-single <orignalCorpus> <annotatedCorpus>\n\t\t\t-featureTypes <corpusFileName> <validateOutFolder> <crossFoldCount> <validationStatisticOutFileName> [<featureTypeFileName>]*");

		}
		if (args[0].equals("-cross")) {
			if (args.length < 4) {
				throw new IllegalArgumentException("3 arguments needed: -cross <originalCorpus> <resultFolder> <type:[single,(number)]>");

			}
			File originalCorpusFile = new File(args[1]);
			File resultFolder = new File(args[2]);
			if (!resultFolder.isDirectory()) {
				throw new IllegalArgumentException("The <resultFolder> has to be a folder!");
			}
			long startTime = System.currentTimeMillis();
			System.out.println("--- Start CrossValidation ---");
			System.out.println("Original Corpus: " + originalCorpusFile.getAbsolutePath());
			System.out.println("Result Folder: " + resultFolder.getAbsolutePath());

			System.out.println("--- Read Corpora---");
			ExtractionValidator extractionValidator = new ExtractionValidator();

			int crossValidationCount = 1;
			if (!args[3].equals("single")) {
				crossValidationCount = Integer.parseInt(args[3]);
			}


			extractionValidator.performCrossValidation(ClassifierNB.readCorpusData(originalCorpusFile), crossValidationCount, resultFolder);


			System.out.println("--- End ---");
			System.out.println((((System.currentTimeMillis() - startTime) / 1000) / 60) + "min" + (((System.currentTimeMillis() - startTime) / 1000) % 60) + "sec");
		} else if (args[0].equals("-single")) {
			//File originalCorpusFile = new File(args[1]);
			ExtractionValidator extractionValidator = new ExtractionValidator();
			Corpus originalCorpus = ClassifierNB.readCorpusData(new File(args[1]));
			Corpus annotatedCorpus = ClassifierNB.readCorpusData(new File(args[2]));
			long[] result = extractionValidator.validate(originalCorpus, annotatedCorpus);
			ValidateResult validateResult = new ValidateResult(result);
			validateResult.printResult();

			annotatedCorpus.writeCorpusToFile(args[2]);

		} else if (args[0].equals("-featureTypes")) {
			if (args.length < 5) {
				System.out.println("not enough arguments: -featureTypes <corpusFileName> <validateOutFolder> <crossFoldCount> <validationStatisticOutFileName> [<featureTypeFileName>]*");
			} else {
				Corpus corpus = ClassifierNB.readCorpusData(new File(args[1]));
				File valOut = new File(args[2]);
				int crossFoldCount = Integer.parseInt(args[3]);
				File fileOut = new File(args[4]);

				BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));

				ExtractionValidator extractionValidator = new ExtractionValidator();
				ValidateResult validateResult;

				if (args.length < 6) {
					out.write(ValidateResult.getCaption() + "\n");
					out.close();
					validateResult = extractionValidator.performCrossValidation(corpus, crossFoldCount, valOut);
					out = new BufferedWriter(new FileWriter(fileOut, true));
					//validateResult.normalize(crossFoldCount);
					out.write(validateResult + "\n");
				} else {
					File featureFile = new File(args[5]);
					File[] featureFiles;
					if (featureFile.isDirectory()) {
						featureFiles = featureFile.listFiles();
					} else {
						featureFiles = new File[args.length - 5];
						for (int i = 5; i < args.length; i++) {
							featureFiles[i - 5] = new File(args[i]);
						}
					}
					out.write("featureFile" + Const.splitOutValStats + ValidateResult.getCaption() + "\n");
					out.close();
					for (int i = 0; i < featureFiles.length; i++) {
						System.out.println();
						System.out.println("Read featureTypes from File: "+featureFiles[i].getName());
						FeatureTypes.readFeatureTypesFromFile(featureFiles[i]);
						validateResult = extractionValidator.performCrossValidation(corpus, crossFoldCount, valOut);
						//validateResult.normalize(crossFoldCount);
						out = new BufferedWriter(new FileWriter(fileOut, true));
						out.write(featureFiles[i].getName() + Const.splitOutValStats + validateResult + "\n");
						out.close();
					}
				}
			}
		} else
			System.out.println("validation: " + args[0] + " unknown");
	}

	public ValidateResult performCrossValidation(Corpus originalCorpus, int crossValidationCount, File resultFolder) throws Exceptions.SRLException, IOException {
		System.out.println("--- Start cross validation ---");
		System.out.println("--- - FoldCount: " + crossValidationCount);
		ValidateResult result = new ValidateResult();
		long startTime = System.currentTimeMillis();
		Corpus[] splittedCorpora = Corpus.splitCorpus(originalCorpus, crossValidationCount);

		String folderName = new Date().toString().replace(":", "-");
		File currentCrossValidationFolder = new File(resultFolder.getAbsolutePath() + File.separatorChar + folderName);
		currentCrossValidationFolder.mkdir();

		//Gson gson = new Gson();
		//String annotateCorpusJson = "";
		Corpus trainCorpus;
		Corpus annotateCorpus;

		//Corpus[] sourceCorpora = new Corpus[0];
		File foldFolder;
		for (int i = 0; i < crossValidationCount; i++) {
			foldFolder = new File(currentCrossValidationFolder.getAbsolutePath() + File.separatorChar + "Fold " + (i + 1));
			foldFolder.mkdir();

			//annotateCorpusJson = gson.toJson(splittedCorpora[i], Corpus.class);
			startTime = System.currentTimeMillis();
			System.out.print("--- -- generate training corpus " + (i + 1) + "... ");
			trainCorpus = new Corpus();

			for (int j = 0; j < crossValidationCount; j++) {
				if (j != i || crossValidationCount == 1) {
					trainCorpus.addSentences(splittedCorpora[j].getSentences());
					//sourceCorpora = ArrayUtils.addAll(sourceCorpora, splittedCorpora[j]);
				}
			}
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			/*startTime = System.currentTimeMillis();
			System.out.print("--- -- write training corpus to file " + (i + 1) + "... ");
			trainCorpus.writeCorpusToFile(foldFolder.getAbsolutePath() + File.separatorChar + "trainCorpus" + (i + 1) + ".xml");
			System.out.println((System.currentTimeMillis()-startTime)+"ms");
            */
			startTime = System.currentTimeMillis();
			System.out.print("--- -- generate annotation corpus " + (i + 1) + "... ");
			Cloner cloner = new Cloner();
			//annotateCorpusJson = gson.toJson(splittedCorpora[i], Corpus.class);
			annotateCorpus = cloner.deepClone(splittedCorpora[i]);//gson.fromJson(annotateCorpusJson, Corpus.class);
			annotateCorpus.deleteAnnotation();
			System.out.println((System.currentTimeMillis() - startTime) + "ms");
			//try {

			startTime = System.currentTimeMillis();
			System.out.print("--- -- train model " + (i + 1) + "... ");
			Model model = trainCorpus.trainModel();
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			startTime = System.currentTimeMillis();
			System.out.print("--- -- write model to file " + (i + 1) + "... ");
			model.writeModelToFile(foldFolder.getAbsolutePath() + File.separatorChar + "foldModel" + (i + 1) + ".txt");
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			startTime = System.currentTimeMillis();
			System.out.print("--- -- annotate corpus " + (i + 1) + "... ");
			annotateCorpus.annotateCorpus(model);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			startTime = System.currentTimeMillis();
			System.out.print("--- -- validate annotated corpus " + (i + 1) + "... ");
			long[] valResult = validate(splittedCorpora[i], annotateCorpus);
			ValidateResult validateResult = new ValidateResult(valResult);
			result.addResult(validateResult);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			startTime = System.currentTimeMillis();
			System.out.print("--- -- write annotated corpus to file " + (i + 1) + "... ");
			annotateCorpus.writeCorpusToFile(foldFolder.getAbsolutePath() + File.separatorChar + "foldCorpus" + (i + 1) + ".xml");
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			//System.out.println("--- -- Finished validating annotated corpus " + (i + 1) + " ---");

			System.out.println("--- -- Result for fold " + (i + 1) + " ---");

			validateResult.printResult();
			//double[] stats;
			//stats = getStats(result[idxTruePositiveFrameElementIDrefCount], result[idxClassyFrameElementIDrefCount], result[idxGoldFrameElementIDrefCount]);
			//fmeasureIDrefsSum += validateResult.getFMeasure(0);
			//fmeasureFEsSum += validateResult.getFMeasure(1);
			//fmeasureIDrefsNISum += validateResult.getFMeasure(2);

			System.out.println("--- -- finished fold " + (i + 1) + " ---");

			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
		}

		System.out.println();
		System.out.println("AVG F-Measure (correct IDref): \t" + (result.getFMeasure(0) / crossValidationCount));
		System.out.println("AVG F-Measure (FE identified in sentence): \t" + (result.getFMeasure(1) / crossValidationCount));
		System.out.println("AVG F-Measure (IDrefs FE-name independent): \t" + (result.getFMeasure(2) / crossValidationCount));
		result.normalize(crossValidationCount);
		System.out.println("--- Finished cross validation ---");
		return result;
	}

	private long[] validate(Corpus originalCorpus, Corpus annotatedCorpus) {
		long[] resultValues = new long[10];
		List<Sentence> annotatedSentenceList = annotatedCorpus.getSentences();

		for (Sentence originalSentence : originalCorpus.getSentences()) {

			int index = annotatedSentenceList.indexOf(originalSentence);

			if (index > -1) {
				Sentence annotatedSentence = annotatedSentenceList.get(index);

				List<Frame> originalFrames = originalSentence.getFrames();
				List<Frame> annotatedFrames = annotatedSentence.getFrames();

				// check only sentences with found target...
				if (annotatedSentence.getFrames().size() > 0) {
					//List<String> checkedIDrefs = new LinkedList<String>();
					//List<String> correctIDrefs1 = new LinkedList<String>();

					// check if all FEs are found...
					for (Frame origFrame : originalFrames) {
						Frame annotatedFrame = annotatedSentence.getFrameForTargetLemma(origFrame.getTargetLemma());
						if (annotatedFrame != null) {
							for (FrameElement origFrameElement : origFrame.getFrameElements()) {
								resultValues[ValidateResult.idxGoldFrameElementCount]++;
								resultValues[ValidateResult.idxGoldFrameElementIDrefCount] += origFrameElement.getIdrefs().size();

								for (FrameElement annotatedFrameElement : annotatedFrame.getFrameElements()) {
									if (!annotatedFrameElement.getName().equals(Const.dummyRole)) {
										for (String origIDref : origFrameElement.getIdrefs()) {
											if (annotatedFrameElement.getIdrefs().contains(origIDref)) {
												resultValues[ValidateResult.idxTruePositiveFrameElementIDrefCountNameIndependent]++;
											}
										}
									}
								}

								// check only for FE-Name...
								FrameElement annotatedFrameElement = annotatedFrame.getFrameElement(origFrameElement.getName());
								if (annotatedFrameElement != null) {
									resultValues[ValidateResult.idxTruePositiveFrameElementCount]++;
									for (String origIDref : origFrameElement.getIdrefs()) {
										if (annotatedFrameElement.getIdrefs().contains(origIDref)) {
											resultValues[ValidateResult.idxTruePositiveFrameElementIDrefCount]++;
											annotatedFrameElement.setCorrect(origIDref);
										}
									}
								}
							}


						} else {
							// skipped frames where it is impossible to find a target
						}
					}

					for (Frame annotatedFrame : annotatedFrames)
						for (FrameElement annotatedFrameElement : annotatedFrame.getFrameElements()) {
							if (!annotatedFrameElement.getName().equals(Const.dummyRole)) {
								resultValues[ValidateResult.idxClassyFrameElementCount]++;
								resultValues[ValidateResult.idxClassyFrameElementIDrefCount] += annotatedFrameElement.getIdrefs().size();
							}
						}
				} else {
					resultValues[ValidateResult.idxUnclassifiedSentenceCount]++;
				}
			}


			resultValues[ValidateResult.idxSentenceCount]++;
		}

		return resultValues;
	}



	/*private static void printStats(double[] stats, String label) {
		System.out.println("Precision (" + label + "): \t" + stats[0]);
		System.out.println("Recall (" + label + "): \t" + stats[1]);
		System.out.println("F-Measure (" + label + "): \t" + stats[2]);
		System.out.println();
	} */


}
