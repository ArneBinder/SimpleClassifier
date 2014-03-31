package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import Classifier.bean.*;
import Classifier.bean.Exceptions.*;
import com.rits.cloning.Cloner;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Created by Arne on 09.12.13.
 */
public class Corpus {
	private double threshold = -400.0 - Math.log(35); //25: ca durchschnittliche Anzahl Konstituenten pro Satz

	private List<Sentence> sentences;
	private FeatureExtractor featureExtractor;

	public FeatureExtractor getFeatureExtractor() {
		return featureExtractor;
	}

	public Corpus() {
		featureExtractor = new FeatureExtractor();
		sentences = new LinkedList<Sentence>();
	}

	public Corpus(double threshold) {
		featureExtractor = new FeatureExtractor();
		sentences = new LinkedList<Sentence>();
		this.threshold = threshold;
	}

	public static Corpus[] splitCorpus(Corpus corpus, int splitCount) {
		Corpus[] splittedCorpora = new Corpus[splitCount];

		// initialize all corpora
		for (int i = 0; i < splittedCorpora.length; i++) {
			splittedCorpora[i] = new Corpus(corpus.getThreshold());
		}

		int counter = 0;
		for (Sentence sentence : corpus.getSentences()) {
			splittedCorpora[counter].addSentence(sentence);
			counter = (counter + 1) % splitCount;
		}

		return splittedCorpora;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getSentenceCount() {
		return sentences.size();
	}

	public Sentence getSentence(String id) {
		for (Sentence sentence : sentences) {
			if (sentence.getId().equals(id))
				return sentence;
		}
		return null;
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void addSentence(Sentence sentence) {
		sentences.add(sentence);
	}

	public void addSentences(List<Sentence> sentences) {
		this.sentences.addAll(sentences);
	}


	public Model trainModel() throws SRLException {
		Model model = new Model(featureExtractor);

		List<String> frameElementIDRefs;
		int allreadyProcessed = 0;
		for (Sentence sentence : sentences) {
			try {
				sentence.enrichInformation();
				featureExtractor.setSentence(sentence);

				// process FrameElements
				frameElementIDRefs = new ArrayList<String>();
				for (Frame currentFrame : sentence.getFrames()) {

					sentence.setTarget(currentFrame.getTargetIDs());
					for (FrameElement frameElement : currentFrame.getFrameElements()) {
						String frameElementName = frameElement.getName(); // = role

						int[] indices = sentence.calculateRootOfSubtree(frameElement.getIdrefs());
						String frameElementIDRef;

						if (sentence.getNode(frameElement.getIdrefs().get(0)).getPathsFromRoot().get(indices[1]).length > indices[0])
							frameElementIDRef = sentence.getNode(frameElement.getIdrefs().get(0)).getPathsFromRoot().get(indices[1])[indices[0]];
						else
							frameElementIDRef = frameElement.getIdrefs().get(0);

						// frameElementIDRef = sentence.getNode(frameElementIDRef).getHeadIDref();
						// frameElement.getValue().get(0);
						if (!frameElementIDRef.equals(sentence.getRootIDref()) && sentence.getNode(frameElementIDRef).getHeadIDref() != null) {
							FeatureVector feFeatureVector = featureExtractor.extract(frameElementIDRef);
							feFeatureVector.addFeature(Const.roleTypeIdentifier, frameElementName);
							model.addFeatureVector(feFeatureVector);
							frameElementIDRefs.add(frameElementIDRef);
						}
					}


					// process all terminal elements
					FeatureVector currentVector;
					for (String id : sentence.getTerminals().keySet()) {
						if (!frameElementIDRefs.contains(id) && sentence.getNode(id).getHeadIDref() != null) {

							currentVector = featureExtractor.extract(id);
							currentVector.addFeature(Const.roleTypeIdentifier, Const.dummyRole);
							model.addFeatureVector(currentVector);
						}
					}

					// process all nonterminal elements
					for (String id : sentence.getNonterminals().keySet()) {
						if (!frameElementIDRefs.contains(id) && !sentence.getRootIDref().equals(id) && sentence.getNode(id).getHeadIDref() != null) {

							currentVector = featureExtractor.extract(id);
							currentVector.addFeature(Const.roleTypeIdentifier, Const.dummyRole);
							model.addFeatureVector(currentVector);
						}
					}

				}

			} catch (SRLException e) {
				System.out.println("alreadyProcessed: " + allreadyProcessed);
				throw e;
			}
			allreadyProcessed++;
		}
		model.calculateRelativeFrequenciesPerRole();

		return model;
	}

	public void annotateCorpus(Model model) throws SRLException {

		FeatureVector featureVector = null;
		Frame annotationFrame;

		//Gson gson = new Gson();
		//String json;
		Cloner cloner = new Cloner();

		Entry<String, Double> assignedRoleWithProbability;

		for (Sentence sentence : sentences) {

			sentence.enrichInformation();
			featureExtractor.setSentence(sentence);
			//System.out.println("Current sentence: " + sentence.getId());

			// List<List<String>> test = sentence.extractTargetIDRefs(model.getTargetLemmata());
			double annotationProbability = 1.0;

			// for every targetLemma...
			for (List<String> targetConstituentIDRefs : sentence.extractTargetIDRefs(model.getTargetLemmata())) {
				double bestAnnotationProb = Double.NEGATIVE_INFINITY;
				Frame bestAnnotationFrame = null;
				// for every occurrence of of the targetLemma...
				for (String targetConstituentIDRef : targetConstituentIDRefs) {
					//annotationProbability = 0.0;
					Node targetNode = sentence.getNode(targetConstituentIDRef);
					if (targetNode.getHeadIDref() != null) {
						annotationProbability = 0.0;
						Node target;
						String targetLemma;
						if (targetNode.isTerminal()) {
							target = sentence.getNode(targetConstituentIDRef);
						} else {
							target = sentence.getNode(targetNode.getHeadIDref());
						}
						targetLemma = target.getAttributes().get("lemma");
						annotationFrame = new Frame("annotationID", "annotatedFrameElements_" + targetLemma);
						//annotationFrame.addTargetID(target.getId());
						annotationFrame.setTargetLemmaIDref(target.getId());
						annotationFrame.setTargetLemma(targetLemma);
						sentence.setTarget(targetConstituentIDRef);
						// classify all terminals
						for (Node terminal : sentence.getTerminals().values()) {
							if (terminal.getHeadIDref() != null) {
								featureVector = featureExtractor.extract(terminal.getId());

								assignedRoleWithProbability = model.classify(featureVector);
								annotationProbability += assignedRoleWithProbability.getValue();

								FrameElement frameElement = annotationFrame.getFrameElement(assignedRoleWithProbability.getKey());
								if (frameElement == null) {
									frameElement = new FrameElement(assignedRoleWithProbability.getKey());
									annotationFrame.addFrameElement(frameElement);
								}
								frameElement.addIdRef(terminal.getId(), Math.exp(assignedRoleWithProbability.getValue()));
							}
						}

						// classify all nonTerminals
						for (Node nonTerminal : sentence.getNonterminals().values()) {
							if (!sentence.getRootIDref().equals(nonTerminal.getId()) && nonTerminal.getHeadIDref() != null) {

								featureVector = featureExtractor.extract(nonTerminal.getId());

								assignedRoleWithProbability = model.classify(featureVector);
								annotationProbability += assignedRoleWithProbability.getValue();

								FrameElement frameElement = annotationFrame.getFrameElement(assignedRoleWithProbability.getKey());
								if (frameElement == null) {
									frameElement = new FrameElement(assignedRoleWithProbability.getKey());
									annotationFrame.addFrameElement(frameElement);
								}
								frameElement.addIdRef(nonTerminal.getId(), Math.exp(assignedRoleWithProbability.getValue()));
							}
						} // for
						if (bestAnnotationProb < annotationProbability) {
							bestAnnotationProb = annotationProbability;

							// deep copy
							//json = gson.toJson(annotationFrame);
							bestAnnotationFrame = cloner.deepClone(annotationFrame);//gson.fromJson(json, Frame.class);
						}
					}
				} // for targetLemmaIdRef

				// no target word detected?
				if (bestAnnotationFrame != null) {
					if (bestAnnotationProb <= threshold + Math.log(sentence.getNonterminals().size() + sentence.getTerminals().size())) {
						bestAnnotationFrame.deleteFrameElements();
						//bestAnnotationFrame.setTargetLemmaIDref("");  //evaluate only frames with "good" probability
					}
					bestAnnotationFrame.setProbability(bestAnnotationProb);
					sentence.addFrame(bestAnnotationFrame);

				}

			} // for targetLemmaIdRefs
		}
	}

	public void deleteAnnotation() {
		for (Sentence sentence : sentences) {
			sentence.deleteAnnotation();
		}
	}

	public void writeCorpusToFile(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));
		out.write("<?xml version='1.0' encoding='UTF-8'?>");
		out.write("\n<corpus corpusname=\"tagged-corpus\">");
		out.write("\n\t<head>");
		out.write("\n\t</head>");
		out.write("\n\t<body>");
		for (Sentence sentence : sentences) {
			out.write(sentence.toString());
		}
		out.write("\n\t</body>");
		out.write("\n</corpus>");
		out.close();
	}

	public void parseFile(File file) throws IOException {

		// InputSource inputSource = null;
		if (!file.exists() || !file.isFile()) {
			throw new IOException("Given corpus file does not exist or is no file!");
		}
		try {
			InputSource inputSource = new InputSource(new BufferedReader(new FileReader(file)));

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			XMLParser contentHandler = new XMLParser();
			xmlReader.setContentHandler(contentHandler);

			xmlReader.parse(inputSource);

			// if sentence is already in corpus --> just add frames TODO: test!
			for (Sentence NewSentence : contentHandler.getSentences()) {
				if (sentences.contains(NewSentence)) {
					Sentence sentenceInCorpus = getSentence(NewSentence.getId());
					for (Frame frame : NewSentence.getFrames()) {
						sentenceInCorpus.addFrame(frame);
					}
				} else {
					sentences.add(NewSentence);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void filter(List<String> allowedFrameElements) {
		Iterator<Sentence> iter = sentences.iterator();
		while (iter.hasNext()) {
			if (!iter.next().filter(allowedFrameElements)) {
				iter.remove();
			}
		}
	}


}
