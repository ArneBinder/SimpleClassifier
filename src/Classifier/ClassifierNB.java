package Classifier;


public class ClassifierNB {

	//private List<Sentence> sentences = null;
	private Corpus corpus;

	public static void main(String[] args) {
		if (args.length < 3)
			System.out.println("Wrong arguments: (-train|-annotate) corpusFileName modelFileName [annotatedCorpusFileName])");
		else {

			ClassifierNB classifier = new ClassifierNB();
			try {
				String corpusPath = args[1];
				String modelFileName = args[2];
				classifier.corpus = new Corpus();
				System.out.print("parse corpus file: " + corpusPath+"... ");
				classifier.corpus.parseFile(corpusPath);
				System.out.println("done.");

				if (args[0].equals("-train")) {

					classifier.trainModel(modelFileName);

				} else if (args[0].equals("-annotate")) {
					if (args.length < 4)
						System.out.println("Wrong arguments: -annotate corpusFileName modelFileName annotatedCorpusFileName)");
					else {
						String annotatedCorpusFileName = args[3];
						classifier.corpus.deleteAnnotation();
						classifier.startClassifying(modelFileName, annotatedCorpusFileName);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void startClassifying(String modelFileName, String annotatedCorpusFileName) throws Exception {
		Model model = new Model(corpus.getFeatureExtractor());
		System.out.print("read model from file: "+modelFileName+"... ");
		model.readModelFromFile(modelFileName);
		System.out.println("done.");
		System.out.print("annotate corpus... ");
		corpus.annotateCorpus(model);
		System.out.println("done.");
		System.out.print("write annotated corpus to file: " + annotatedCorpusFileName + "... ");
		corpus.writeCorpusToFile(annotatedCorpusFileName);
		System.out.println("done");

	}

	private void trainModel(String modelFileName) throws Exception {
		//FeatureExtractor featureExtractor = new FeatureExtractor();
		System.out.print("train model with corpus... ");
		Model model = corpus.trainModel();
		System.out.println("done.");
		System.out.print("write model to file: "+modelFileName+"... ");
		model.writeModelToFile(modelFileName);
		System.out.println("done.");
	}

}
