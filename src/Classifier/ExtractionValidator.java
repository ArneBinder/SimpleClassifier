package Classifier;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Classifier.bean.FrameElement;
import org.apache.commons.lang3.ArrayUtils;

import Classifier.bean.Frame;
import Classifier.bean.Sentence;

import com.google.gson.Gson;

/**
 * Takes two annotated corpora and writes the result into a result file
 *
 * @author Robert
 */
public class ExtractionValidator {

	private final static int idxSentenceCount = 0;
	private final static int idxFrameCount = 1;
	private final static int idxGoldFrameElementCount = 2;

	private final static int idxCorrectSentenceCount = 3;
	private final static int idxCorrectFrameCount = 4;
	private final static int idxTruePositiveFrameElementCount = 5;
	private final static int idxClassyFrameElementCount = 6;
	private final static int idxTruePositiveFrameElementCount_check = 7;


	private final Corpus originalCorpus;
	private final File resultFolder;

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
			throw new IllegalArgumentException("3 arguments (paths) needed: <originalCorpus> <resultFolder> <type:[single,(number)]>");
		}

		File originalCorpusFile = new File(args[0]);
		File resultFolder = new File(args[1]);
		if (!resultFolder.isDirectory()) {
			throw new IllegalArgumentException("The <resultFolder> has to be a folder!");
		}
		long startTime = System.currentTimeMillis();
		System.out.println("--- Start CrossValidation ---");
		System.out.println("Original Corpus: " + originalCorpusFile.getAbsolutePath());
		System.out.println("Result Folder: " + resultFolder.getAbsolutePath());

		System.out.println("--- Read Corpora---");
		ExtractionValidator extractionValidator = new ExtractionValidator(originalCorpusFile, resultFolder);

		int crossValidationCount = 1;
		if (!args[2].equals("single")) {
			crossValidationCount = Integer.parseInt(args[2]);
		}

		System.out.println("--- Start cross validation ---");
		System.out.println("--- - FoldCount: " + crossValidationCount);
		extractionValidator.performCrossValidation(crossValidationCount);
		System.out.println("--- Finished cross validation ---");

		System.out.println("--- End ---");
		System.out.println((((System.currentTimeMillis()-startTime)/1000)/60)+"min"+(((System.currentTimeMillis()-startTime)/1000)%60)+"sec");
	}

	public ExtractionValidator(File originalCorpusFile, File resultFile) {
		this.originalCorpus = ClassifierNB.readCorpusData(originalCorpusFile);
		this.resultFolder = resultFile;
	}

	public void performCrossValidation(int crossValidationCount) throws Exception {
		Corpus[] splittedCorpora = Corpus.splitCorpus(originalCorpus, crossValidationCount);
		double fmeasureSum = 0;

		String folderName = new Date().toString().replace(":", "-");
		File currentCrossValidationFolder = new File(resultFolder.getAbsolutePath() + File.separatorChar + folderName);
		currentCrossValidationFolder.mkdir();

		Gson gson = new Gson();
		String annotateCorpusJson = "";
		Corpus trainCorpus;
		Corpus annotateCorpus;

		//Corpus[] sourceCorpora = new Corpus[0];
		File foldFolder;
		for (int i = 0; i < crossValidationCount; i++) {
			foldFolder = new File(currentCrossValidationFolder.getAbsolutePath() + File.separatorChar + "Fold " + (i + 1));
			foldFolder.mkdir();

			//annotateCorpusJson = gson.toJson(splittedCorpora[i], Corpus.class);

			trainCorpus = new Corpus();

			for (int j = 0; j < crossValidationCount; j++) {
				if (j != i || crossValidationCount == 1) {
					trainCorpus.addSentences(splittedCorpora[j].getSentences());
					//sourceCorpora = ArrayUtils.addAll(sourceCorpora, splittedCorpora[j]);
				}
			}

			//trainCorpus = Corpus.mergeCorpora(sourceCorpora);
			annotateCorpusJson = gson.toJson(splittedCorpora[i], Corpus.class);
			annotateCorpus = gson.fromJson(annotateCorpusJson, Corpus.class);
			annotateCorpus.deleteAnnotation();

			//try {
			System.out.println("--- -- Start training and writing model " + (i + 1) + " ---");
			Model model = trainCorpus.trainModel();
			model.writeModelToFile(foldFolder.getAbsolutePath() + File.separatorChar + "foldModel" + (i + 1) + ".txt");
			System.out.println("--- -- Finished training and writing model " + (i + 1) + " ---");

			System.out.println("--- -- Start annotating and writing corpus " + (i + 1) + " ---");
			annotateCorpus.annotateCorpus(model);
			annotateCorpus.writeCorpusToFile(foldFolder.getAbsolutePath() + File.separatorChar + "foldCorpus" + (i + 1) + ".xml");
			System.out.println("--- -- Finishing annotating and writing corpus " + (i + 1) + "---");

			System.out.println("--- -- Start validating annotated corpus " + (i + 1) + " and writing result ---");
			long[] result = validate(splittedCorpora[i], annotateCorpus);
			double precision = result[idxTruePositiveFrameElementCount] / (double) result[idxClassyFrameElementCount];
			double recall = result[idxTruePositiveFrameElementCount] / (double) result[idxGoldFrameElementCount];
			double fmeasure = 2.0 * precision * recall / (precision + recall);
			fmeasureSum +=fmeasure;
			System.out.println("--- -- Finished validating annotated corpus " + (i + 1) + " ---");

			System.out.println("--- -- Start writing result for fold " + (i + 1) + " ---");

			writeResult(result);
			System.out.println("F-Measure: \t" + fmeasure);
			System.out.println("--- -- Finished writing result for fold " + (i + 1) + " ---");

			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
		}
		System.out.println();
		System.out.println("AVG F-Measure: \t"+(fmeasureSum / crossValidationCount));
		// write overall result
	}

	// TODO add validating
	private long[] validate(Corpus originalCorpus, Corpus annotatedCorpus) {
		long[] resultValues = new long[8];
		List<Sentence> annotatedSentenceList = annotatedCorpus.getSentences();
		//TODO: ask robert if necessary...
		Collections.sort(annotatedSentenceList);
		Collections.sort(originalCorpus.getSentences());

		for (Sentence originalSentence : originalCorpus.getSentences()) {

			int index = annotatedSentenceList.indexOf(originalSentence);

			if (index > -1) {
				Sentence annotatedSentence = annotatedSentenceList.get(index);

				List<Frame> originalFrames = originalSentence.getFrames();
				List<Frame> annotatedFrames = annotatedSentence.getFrames();

				// TODO: compare
				// check if all FEs are found...
				for (Frame origFrame : originalFrames) {
					for (FrameElement origFrameElement : origFrame.getFrameElements())
						for (String origIDref : origFrameElement.getIdrefs()) {
							boolean foundFE = false;
							for (Frame annotFrame : annotatedFrames) {
								FrameElement matchingAnnotFE = annotFrame.getFrameElement(origFrameElement.getName());
								if (matchingAnnotFE != null && matchingAnnotFE.getIdrefs().contains(origIDref)) {
									foundFE = true;
									break;
								}
							}
							if (foundFE) {
								resultValues[idxTruePositiveFrameElementCount]++;
							}
							resultValues[idxGoldFrameElementCount]++;

						}

				}

				// check if all FEs which are found are correct...
				for (Frame annotFrame : annotatedFrames) {
					for (FrameElement annotFrameElement : annotFrame.getFrameElements())
						// shouldnt be a dummy...
						if (!annotFrameElement.getName().equals("dummyRole")) {
							for (String annotIDref : annotFrameElement.getIdrefs()) {
								boolean foundFE = false;
								for (Frame origFrame : originalFrames) {
									FrameElement matchingOrigFE = origFrame.getFrameElement(annotFrameElement.getName());
									if (matchingOrigFE != null && matchingOrigFE.getIdrefs().contains(annotIDref)) {
										foundFE = true;
										break;
									}
								}
								if (foundFE) {
									resultValues[idxTruePositiveFrameElementCount_check]++;
								}
								resultValues[idxClassyFrameElementCount]++;

							}
						}
				}

			}

			resultValues[idxSentenceCount]++;
		}

		return resultValues;
	}

	private void writeResult(long[] resultValues) {
		System.out.println("SentenceCount: \t" + resultValues[idxSentenceCount]);
		System.out.println("TruePositiveFrameElementCount: \t" + resultValues[idxTruePositiveFrameElementCount]);
		System.out.println("TruePositiveFrameElementCount_check: \t" + resultValues[idxTruePositiveFrameElementCount_check]);
		System.out.println("GoldFrameElementCount: \t" + resultValues[idxGoldFrameElementCount]);
		System.out.println("ClassyFrameElementCount: \t" + resultValues[idxClassyFrameElementCount]);

	}
}
