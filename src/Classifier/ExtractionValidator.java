package Classifier;

import java.io.File;
import java.util.List;

import Classifier.bean.Frame;
import Classifier.bean.Sentence;

/**
 * Takes two annotated corpora and writes the result into a result file
 * 
 * @author Robert
 */
public class ExtractionValidator {

    private final static int idxSentenceCount = 0;
    private final static int idxFrameCount = 1;
    private final static int idxFrameElementCount = 2;

    private final static int idxCorrectSentenceCount = 3;
    private final static int idxCorrectFrameCount = 4;
    private final static int idxCorrectframeElementCount = 5;

    private final Corpus originalCorpus = new Corpus();
    private final Corpus annotatedCorpus = new Corpus();

    private final File resultFile;

    private final long[] resultValues = { // see next line
	    0, // sentenceCount
	    0, // frameCount
	    0, // frameElementCount

	    0, // correctSentenceCount
	    0, // correctFrameCount
	    0 // correctFrameElementCount
    };

    public static void main(String[] args) throws Exception {
	if (args.length < 3) {
	    throw new IllegalArgumentException("3 arguments (paths) needed: <originalCorpus> <annotatedCorpus> <resultFile>");
	}

	System.out.println("Original Corpus: " + args[0]);
	System.out.println("Annotated Corpus: " + args[1]);

	System.out.println("--- Read Corpora---");
	ExtractionValidator extractionValidator = new ExtractionValidator(args[0], args[1], args[2]);

	System.out.println("--- Start Validating ---");
	extractionValidator.validate();

	System.out.println("Result File: " + args[2]);
	System.out.println("--- End ---");
    }

    public ExtractionValidator(String originalCorpusPath, String annotatedCorpusPath, String resultFilePath) throws Exception {
	originalCorpus.parseFile(new File(originalCorpusPath).getAbsolutePath());
	originalCorpus.parseFile(new File(annotatedCorpusPath).getAbsolutePath());

	resultFile = new File(resultFilePath);
    }

    private void validate() {
	List<Sentence> annotatedSentenceList = annotatedCorpus.getSentences();

	for (Sentence originalSentence : originalCorpus.getSentences()) {
	    int index = annotatedSentenceList.indexOf(originalSentence);

	    if (index > -1) {
		Sentence annotatedSentence = annotatedSentenceList.get(index);

		List<Frame> originalFrames = originalSentence.getFrames();
		List<Frame> annotatedFrames = annotatedSentence.getFrames();

		// TODO: compare

	    }

	    resultValues[idxSentenceCount]++;
	}
    }

}
