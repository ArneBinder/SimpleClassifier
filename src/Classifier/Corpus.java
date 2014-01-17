package Classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import Classifier.bean.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.gson.Gson;

/**
 * Created by Arne on 09.12.13.
 */
public class Corpus {
	private List<Sentence> sentences;
	private FeatureExtractor featureExtractor;
	private static double threshold = 0.001;

	public FeatureExtractor getFeatureExtractor() {
		return featureExtractor;
	}

	public Corpus() {
		featureExtractor = new FeatureExtractor();
		sentences = new LinkedList<Sentence>();
	}

	public static Corpus[] splitCorpus(Corpus corpus, int splitCount) {
		Corpus[] splittedCorpora = new Corpus[splitCount];

		// initialize all corpora
		for (int i = 0; i < splittedCorpora.length; i++) {
			splittedCorpora[i] = new Corpus();
		}

		int counter = 0;
		for (Sentence sentence : corpus.getSentences()) {
			splittedCorpora[counter].addSentence(sentence);
			counter = (counter + 1) % splitCount;
		}

		return splittedCorpora;
	}

	public static Corpus mergeCorpora(Corpus[] corpora) {
		Corpus mergeCorpus = new Corpus();

		for (Corpus corpus : corpora) {
			if (corpus == null) continue;
			mergeCorpus.addSentences(corpus.getSentences());
		}

		return mergeCorpus;
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


	public Model trainModel() throws Exception {
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
						if (!frameElementIDRef.equals(sentence.getRootIDref())) {
							FeatureVector feFeatureVector = featureExtractor.extract(frameElementIDRef);
							feFeatureVector.addFeature(FeatureVector.getRoleTypeIdentifier(), frameElementName);
							model.addFeatureVector(feFeatureVector);
							frameElementIDRefs.add(frameElementIDRef);
						}
					}


					// process all terminal elements
					FeatureVector currentVector;
					for (String id : sentence.getTerminals().keySet()) {
						if (!frameElementIDRefs.contains(id)) {

							currentVector = featureExtractor.extract(id);
							currentVector.addFeature(FeatureVector.getRoleTypeIdentifier(),
									model.getDummyRoles().get(myRandom(0, model.getDummyRoles().size() - 1)));
							model.addFeatureVector(currentVector);
						}
					}

					// process all nonterminal elements
					for (String id : sentence.getNonterminals().keySet()) {
						if (!frameElementIDRefs.contains(id) && !sentence.getRootIDref().equals(id)) {

							currentVector = featureExtractor.extract(id);
							currentVector.addFeature(FeatureVector.getRoleTypeIdentifier(),
									model.getDummyRoles().get(myRandom(0, model.getDummyRoles().size() - 1)));
							model.addFeatureVector(currentVector);
						}
					}

				}

			} catch (Exception e) {
				System.out.println("alreadyProcessed: " + allreadyProcessed);
				throw e;
			}
			allreadyProcessed++;
		}
		// dout.close();
		model.calculateRelativeFrequenciesPerRole();

		return model;
	}

	public void annotateCorpus(Model model) throws Exception {

		FeatureVector featureVector = null;
		Frame annotationFrame;

		Gson gson = new Gson();
		String json;

		Entry<String, Double> assignedRoleWithProbability;

		for (Sentence sentence : sentences) {
			sentence.enrichInformation();
			featureExtractor.setSentence(sentence);
			//System.out.println("Current sentence: " + sentence.getId());

			// List<List<String>> test = sentence.extractTargetIDRefs(model.getTargetLemmata());
			double annotationProbability = 1.0;
			double bestAnnotationProb = 0;
			Frame bestAnnotationFrame = null;
			for (List<String> targetLemmaIDRefs : sentence.extractTargetIDRefs(model.getTargetLemmata())) {

				for (String targetLemmaIDRef : targetLemmaIDRefs) {
					//annotationProbability = 0.0;
					Node targetNode = sentence.getNode(targetLemmaIDRef);
					if (targetNode.getHeadIDref() != null) {
						annotationProbability = 1.0;
						String targetLemma;
						if (targetNode.isTerminal()) {
							targetLemma = sentence.getNode(targetLemmaIDRef).getAttributes().get("lemma");
						} else {
							targetLemma = sentence.getNode(targetNode.getHeadIDref()).getAttributes().get("lemma");
						}
						annotationFrame = new Frame("annotationID", "annotatedFrameElements_" + targetLemma);
						annotationFrame.setTargetLemma(targetLemma);
						sentence.setTarget(targetLemmaIDRef);
						// classify all terminals
						for (Node terminal : sentence.getTerminals().values()) {
							if (terminal.getHeadIDref() != null) {
								featureVector = featureExtractor.extract(terminal.getId());

								assignedRoleWithProbability = model.classify(featureVector);
								annotationProbability *= assignedRoleWithProbability.getValue();
								//if (assignedRoleWithProbability.getValue() > threshold) {
									FrameElement frameElement = annotationFrame.getFrameElement(assignedRoleWithProbability.getKey());
									if (frameElement == null) {
										frameElement = new FrameElement(assignedRoleWithProbability.getKey());
										annotationFrame.addFrameElement(frameElement);
									}
									frameElement.addIdRef(terminal.getId(), assignedRoleWithProbability.getValue());
								//}
								//annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), terminal.getId() + ":"
								//		+ assignedRoleWithProbability.getValue());
							}
						}

						// classify all terminals
						for (Node nonTerminal : sentence.getNonterminals().values()) {
							if (!sentence.getRootIDref().equals(nonTerminal.getId()) && nonTerminal.getHeadIDref() != null) {

								featureVector = featureExtractor.extract(nonTerminal.getId());

								assignedRoleWithProbability = model.classify(featureVector);
								annotationProbability *= assignedRoleWithProbability.getValue();
								//if (assignedRoleWithProbability.getValue() > threshold) {
									FrameElement frameElement = annotationFrame.getFrameElement(assignedRoleWithProbability.getKey());
									if (frameElement == null) {
										frameElement = new FrameElement(assignedRoleWithProbability.getKey());
										annotationFrame.addFrameElement(frameElement);
									}
									frameElement.addIdRef(nonTerminal.getId(), assignedRoleWithProbability.getValue());
									//annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), nonTerminal.getId() + ":"
									//		+ assignedRoleWithProbability.getValue());
								//}
							}
						} // for
						if (bestAnnotationProb < annotationProbability) {
							bestAnnotationProb = annotationProbability;

							// deep copy
							json = gson.toJson(annotationFrame);
							bestAnnotationFrame = gson.fromJson(json, Frame.class);
						}
					}
				} // for targetLemmaIdRef
			} // for targetLemmaIdRefs

			// no target word detected
			if (bestAnnotationFrame != null) {
				sentence.addFrame(bestAnnotationFrame);
			}
		}
	}

	public void deleteAnnotation() {
		for (Sentence sentence : sentences) {
			sentence.deleteAnnotation();
		}
	}

	public void writeCorpusToFile(String fileName) throws Exception {
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

	public void parseFile(File file) throws Exception {

		// InputSource inputSource = null;
		if (!file.exists() || !file.isFile()) {
			throw new Exception("Given corpus file does not exist or is no file!");
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

	public void printSentences() {
		for (Sentence sentence : sentences) {
			System.out.println(sentence.toString());
		}
	}

	public void filter(List<String> allowedFrameElements) {
		Iterator<Sentence> iter = sentences.iterator();
		while (iter.hasNext()) {
			if (!iter.next().filter(allowedFrameElements)) {
				iter.remove();
			}
		}


		//for(Sentence sentence: sentences){
		//	if(!sentence.filter(allowedFrameElements)){
		//		sentences.remove(sentence);
		//	}
		//}
	}

	public static int myRandom(int low, int high) {
		return (int) (Math.random() * (high - low) + low);
	}

}
