package Classifier;

import java.io.File;
import java.util.*;

import Classifier.bean.FrameElement;
import com.rits.cloning.Cloner;

import Classifier.bean.Frame;
import Classifier.bean.Sentence;

//import com.google.gson.Gson;

/**
 * Takes two annotated corpora and writes the result into a result file
 *
 * @author Robert
 */
public class ExtractionValidator {

	private final static int idxSentenceCount = 0;
	//private final static int idxFrameCount = 1;


	//private final static int idxCorrectSentenceCount = 3;
	//private final static int idxCorrectFrameCount = 4;
	private final static int idxTruePositiveFrameElementIDrefCount = 1;
	private final static int idxTruePositiveFrameElementIDrefCount_check = 2;
	private final static int idxClassyFrameElementIDrefCount = 3;
	private final static int idxGoldFrameElementIDrefCount = 4;

	private final static int idxTruePositiveFrameElementCount = 5;
	//private final static int idxTruePositiveFrameElementCount_check = 6;
	private final static int idxClassyFrameElementCount = 7;
	private final static int idxGoldFrameElementCount = 8;

	//private final Corpus originalCorpus;
	//private final File resultFolder;

	// basic structure for a result array
	// private final long[] resultValues = { // see next line
	//
	// 0, // sentenceCount
	// 0, // frameCount
	// 0, // frameElementCount
	//
	// 0, // correctSentenceCount
	// 0, // correctFrameCount
	// 0 // correctFrameElementCount
	// };

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			throw new IllegalArgumentException("3 arguments needed: -cross <originalCorpus> <resultFolder> <type:[single,(number)]>\n\t\t\t-single <orignalCorpus> <annotatedCorpus>");

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

			System.out.println("--- Start cross validation ---");
			System.out.println("--- - FoldCount: " + crossValidationCount);
			extractionValidator.performCrossValidation(ClassifierNB.readCorpusData(originalCorpusFile), crossValidationCount, resultFolder);
			System.out.println("--- Finished cross validation ---");

			System.out.println("--- End ---");
			System.out.println((((System.currentTimeMillis() - startTime) / 1000) / 60) + "min" + (((System.currentTimeMillis() - startTime) / 1000) % 60) + "sec");
		} else if (args[0].equals("-single")) {
			//File originalCorpusFile = new File(args[1]);
			ExtractionValidator extractionValidator = new ExtractionValidator();
			Corpus originalCorpus = ClassifierNB.readCorpusData(new File(args[1]));
			Corpus annotatedCorpus = ClassifierNB.readCorpusData(new File(args[2]));
			writeResult(extractionValidator.validate(originalCorpus, annotatedCorpus));
		} else
			System.out.println("validation: " + args[0] + " unknown");
	}

	public void performCrossValidation(Corpus originalCorpus, int crossValidationCount, File resultFolder) throws Exception {
		long startTime = System.currentTimeMillis();
		Corpus[] splittedCorpora = Corpus.splitCorpus(originalCorpus, crossValidationCount);
		double fmeasureSum = 0;
		double fmeasureFESum = 0;

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
			long[] result = validate(splittedCorpora[i], annotateCorpus);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			startTime = System.currentTimeMillis();
			System.out.print("--- -- write annotated corpus to file " + (i + 1) + "... ");
			annotateCorpus.writeCorpusToFile(foldFolder.getAbsolutePath() + File.separatorChar + "foldCorpus" + (i + 1) + ".xml");
			System.out.println((System.currentTimeMillis() - startTime) + "ms");

			//System.out.println("--- -- Finished validating annotated corpus " + (i + 1) + " ---");

			System.out.println("--- -- Result for fold " + (i + 1) + " ---");

			writeResult(result);
			double precision = result[idxTruePositiveFrameElementIDrefCount] / (double) result[idxClassyFrameElementIDrefCount];
			double recall = result[idxTruePositiveFrameElementIDrefCount] / (double) result[idxGoldFrameElementIDrefCount];
			double fmeasure = 2.0 * precision * recall / (precision + recall);
			double precisionFE = result[idxTruePositiveFrameElementCount] / (double) result[idxClassyFrameElementCount];
			double recallFE = result[idxTruePositiveFrameElementCount] / (double) result[idxGoldFrameElementCount];
			double fmeasureFE = 2.0 * precisionFE * recallFE / (precisionFE + recallFE);
			fmeasureSum += fmeasure;
			fmeasureFESum += fmeasureFE;
			System.out.println("F-Measure (IDrefs): \t" + fmeasure);
			System.out.println("F-Measure (FEs): \t" + fmeasureFE);
			System.out.println("--- -- finished fold " + (i + 1) + " ---");

			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
		}
		System.out.println();
		System.out.println("AVG F-Measure (correct IDref): \t" + (fmeasureSum / crossValidationCount));
		System.out.println("AVG F-Measure (FE identified in sentence): \t" + (fmeasureFESum / crossValidationCount));
		// write overall result
	}

	private long[] validate(Corpus originalCorpus, Corpus annotatedCorpus) {
		long[] resultValues = new long[9];
		List<Sentence> annotatedSentenceList = annotatedCorpus.getSentences();

		for (Sentence originalSentence : originalCorpus.getSentences()) {

			int index = annotatedSentenceList.indexOf(originalSentence);

			if (index > -1) {
				Sentence annotatedSentence = annotatedSentenceList.get(index);

				List<Frame> originalFrames = originalSentence.getFrames();
				List<Frame> annotatedFrames = annotatedSentence.getFrames();

				// check only sentences with found target...
				if (annotatedFrames.size() > 0) {
					Set<String> checkedIDrefs = new HashSet<String>(50);
					Set<String> correctIDrefs1 = new HashSet<String>();

					// check if all FEs are found...
					for (Frame origFrame : originalFrames) {
						for (FrameElement origFrameElement : origFrame.getFrameElements()) {
							boolean foundFE = false;
							for (Frame annotFrame : annotatedFrames) {
								if (annotFrame.getFrameElement(origFrameElement.getName()) != null)
									foundFE = true;
							}
							if (foundFE)
								resultValues[idxTruePositiveFrameElementCount]++;
							resultValues[idxGoldFrameElementCount]++;

							for (String origIDref : origFrameElement.getIdrefs()) {
								checkedIDrefs.add(origIDref);
								for (Frame annotFrame : annotatedFrames) {
									FrameElement matchingAnnotFE = annotFrame.getFrameElement(origFrameElement.getName());
									if (matchingAnnotFE != null && matchingAnnotFE.getIdrefs().contains(origIDref)) {
										matchingAnnotFE.setCorrect(origIDref);
										correctIDrefs1.add(origIDref);
										break;
									}
								}
							}
						}
					}
					resultValues[idxTruePositiveFrameElementIDrefCount] += correctIDrefs1.size();
					resultValues[idxGoldFrameElementIDrefCount] += checkedIDrefs.size();

					// check if all FEs which are found are correct...
					for (Frame annotFrame : annotatedFrames) {
						for (FrameElement annotFrameElement : annotFrame.getFrameElements()) {
							// shouldnt be a dummy...
							if (!annotFrameElement.getName().equals(Model.getDummyRole())) {
								resultValues[idxClassyFrameElementCount]++;
								resultValues[idxClassyFrameElementIDrefCount] += annotFrameElement.getIdrefs().size();
							}
						}
					}
				}
			}


			resultValues[idxSentenceCount]++;
		}

		return resultValues;
	}

	private static void writeResult(long[] resultValues) {
		System.out.println("SentenceCount: \t" + resultValues[idxSentenceCount]);
		System.out.println("TruePositiveFrameElementIDrefCount: \t" + resultValues[idxTruePositiveFrameElementIDrefCount]);
		//System.out.println("TruePositiveFrameElementIDrefCount_check: \t" + resultValues[idxTruePositiveFrameElementIDrefCount_check]);
		System.out.println("GoldFrameElementIDrefCount: \t" + resultValues[idxGoldFrameElementIDrefCount]);
		System.out.println("ClassyFrameElementIDrefCount: \t" + resultValues[idxClassyFrameElementIDrefCount]);
		System.out.println();
		System.out.println("TruePositiveFrameElementCount: \t" + resultValues[idxTruePositiveFrameElementCount]);
		//System.out.println("TruePositiveFrameElementCount_check: \t" + resultValues[idxTruePositiveFrameElementCount_check]);
		System.out.println("GoldFrameElementCount: \t" + resultValues[idxGoldFrameElementCount]);
		System.out.println("ClassyFrameElementCount: \t" + resultValues[idxClassyFrameElementCount]);

	}
}
