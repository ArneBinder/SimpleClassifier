package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Classifier.bean.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import Classifier.bean.FeatureVector;
import Classifier.bean.Frame;
import Classifier.bean.Sentence;

/**
 * Created by Arne on 09.12.13.
 */
public class Corpus {
	private List<Sentence> sentences;
	private FeatureExtractor featureExtractor;

	public FeatureExtractor getFeatureExtractor() {
		return featureExtractor;
	}

	public Corpus() {
		featureExtractor = new FeatureExtractor();
        sentences = new LinkedList<Sentence>();
	}

	public Model trainModel() throws Exception {
		Model model = new Model(featureExtractor);
		List<String> frameElementIDRefs;
		List<String> targetHeads;
		for (Sentence sentence : sentences) {

			//sentence.enrichInformation();

			featureExtractor.setSentence(sentence);
            //featureExtractor.enrichInformation();

			// process FrameElements
			frameElementIDRefs = new ArrayList<String>();
			for (Frame currentFrame : sentence.getFrames()) {
				model.addTargetWord(currentFrame.getName(), currentFrame.getTargetLemma());
				targetHeads = new LinkedList<String>();
				for(String curTargetIDref: currentFrame.getTargetIDs()){
					targetHeads.add(sentence.getNode(curTargetIDref).getHeadIDref());
				}
				sentence.setTargets(targetHeads);
				for (Entry<String, List<String>> frameElement : currentFrame.getFrameElements().entrySet()) {
					String frameElementName = frameElement.getKey(); // = role

					// get first idref only.. hopefully, there is only one
					String frameElementIDRef = frameElement.getValue().get(0);

					FeatureVector feFeatureVector = featureExtractor.extract(frameElementIDRef);
					model.addFeatureVector(frameElementName, feFeatureVector);
					frameElementIDRefs.add(frameElementIDRef);
				}

				// process all terminal elements
				FeatureVector currentVector;
				for (String id : sentence.getTerminals().keySet()) {
					if (!frameElementIDRefs.contains(id)) {
						currentVector = featureExtractor.extract(id);
						model.addFeatureVector(currentVector);
					}
				}

				// process all nonterminal elements
				for (String id : sentence.getNonterminals().keySet()) {
					if (!frameElementIDRefs.contains(id)) {
						currentVector = featureExtractor.extract(id);
						model.addFeatureVector(currentVector);
					}
				}
			}
		}
		
		model.calculateProbabilities();
		
		return model;
	}

	public void annotateCorpus(Model model) throws Exception {
		
		FeatureVector featureVector = null;
		Frame annotationFrame = new Frame("annotationID", "annotatedFrameElements");

		Entry<String, Double> assignedRoleWithProbability;
		for (Sentence sentence : sentences) {

			//sentence.enrichInformation();

			featureExtractor.setSentence(sentence);
			
			for (List<String> targetLemmaIDRefs : sentence.extractTargetIDRefs(model
					.getTargetLemmata().keySet())) {

				sentence.setTargets(targetLemmaIDRefs);
				// classify all terminals
				for (Node terminal : sentence.getTerminals().values()) {
					featureVector = featureExtractor.extract(terminal.getId());
					
					assignedRoleWithProbability = model.classify(featureVector);
					annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), terminal.getId()+":"+assignedRoleWithProbability.getValue());
				}

				// classify all terminals
				for (Node nonTerminal : sentence.getNonterminals()
						.values()) {
					featureVector = featureExtractor.extract(nonTerminal.getId());
					
					assignedRoleWithProbability = model.classify(featureVector);
					annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), nonTerminal.getId()+":"+assignedRoleWithProbability.getValue());
				}
			}
			
			sentence.addFrame(annotationFrame);
		}
	}

	public void deleteAnnotation() {
		for (Sentence sentence : sentences) {
			sentence.deleteAnnotation();
		}
	}

	public void writeCorpusToFile(String fileName) throws Exception{
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));
		out.write("<?xml version='1.0' encoding='UTF-8'?>");
		out.write("\n<corpus corpusname=\"tagged-corpus\">");
		out.write("\n\t<head>");
		out.write("\n\t</head>");
		out.write("\n\t<body>");
		for(Sentence sentence: sentences){
			out.write(sentence.toString());
		}
		out.write("\n\t</body>");
		out.write("\n</corpus>");
		out.close();
	}

	public void parseFile(String fileName) throws Exception {

		// InputSource inputSource = null;
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			throw new Exception(
					"Given corpus file does not exist or is no file!");
		}
		try {
			InputSource inputSource = new InputSource(new BufferedReader(
					new FileReader(fileName)));

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			XMLParser contentHandler = new XMLParser();
			xmlReader.setContentHandler(contentHandler);

			xmlReader.parse(inputSource);

			this.sentences.addAll(contentHandler.getSentences());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printSentences() {
		for (Sentence sentence : sentences) {
			System.out.println(sentence.toString());
		}
	}

}
