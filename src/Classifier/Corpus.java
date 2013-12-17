package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    //public BufferedWriter dout;
    private FeatureExtractor featureExtractor;

    public FeatureExtractor getFeatureExtractor() {
        return featureExtractor;
    }

    public Corpus() {
//        try{
//        dout= new BufferedWriter(new FileWriter(new File("dummy.txt")));
//        }catch (Exception e){
//
//        }
        featureExtractor = new FeatureExtractor();
        sentences = new LinkedList<Sentence>();
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

    public Model trainModel() throws Exception {
        Model model = new Model(featureExtractor);

        List<String> frameElementIDRefs;
        List<String> targetHeads;
        int allreadyProcessed = 0;
        for (Sentence sentence : sentences) {
            try {
                //sentence.enrichInformation();

                featureExtractor.setSentence(sentence);
               // if(!featureExtractor.dummy.equals("")){
                //dout.write(featureExtractor.dummy);
                //System.out.println(featureExtractor.dummy);
                //}
                //featureExtractor.enrichInformation();

                // process FrameElements
                frameElementIDRefs = new ArrayList<String>();
                for (Frame currentFrame : sentence.getFrames()) {
                    model.addTargetWord(currentFrame.getName(), currentFrame.getTargetLemma());
                    
                    // TODO: wieso targetHead?
//                    targetHeads = new LinkedList<String>();
//                    for (String curTargetIDref : currentFrame.getTargetIDs()) {
//                        String headIDref = sentence.getNode(curTargetIDref).getHeadIDref();
//                        if (headIDref != null)
//                            targetHeads.add(sentence.getNode(curTargetIDref).getHeadIDref());
//                    }
                    sentence.setTargets(currentFrame.getTargetIDs());
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
                        if (!frameElementIDRefs.contains(id) && !sentence.getRootIDref().equals(id)) {
                            currentVector = featureExtractor.extract(id);
                            model.addFeatureVector(currentVector);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("allreadyProcessed: " + allreadyProcessed);
                throw e;
            }
            allreadyProcessed++;
        }
        //dout.close();
        model.calculateProbabilities();

        return model;
    }

    public void annotateCorpus(Model model) throws Exception {

        FeatureVector featureVector = null;
        Frame annotationFrame;

        Entry<String, Double> assignedRoleWithProbability;

        for (Sentence sentence : sentences) {

            featureExtractor.setSentence(sentence);

            for (List<String> targetLemmaIDRefs : sentence.extractTargetIDRefs(model
                    .getTargetLemmata().keySet())) {
        	
        	for (String targetLemmaIDRef : targetLemmaIDRefs) {
        	    String targetLemma = sentence.getNode(targetLemmaIDRef).getAttributes().get("lemma");
        	    annotationFrame = new Frame("annotationID", "annotatedFrameElements_" + targetLemma);

        	    sentence.setTargets(Arrays.asList(new String[]{targetLemmaIDRef}));
        	    // classify all terminals
        	    for (Node terminal : sentence.getTerminals().values()) {
        		featureVector = featureExtractor.extract(terminal.getId());
        		
        		assignedRoleWithProbability = model.classify(featureVector);
        		annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), terminal.getId() + ":" + assignedRoleWithProbability.getValue());
        	    }
        	    
        	    // classify all terminals
        	    for (Node nonTerminal : sentence.getNonterminals()
        		    .values()) {
        		if (!sentence.getRootIDref().equals(nonTerminal.getId())) {
        		    
        		    featureVector = featureExtractor.extract(nonTerminal.getId());
        		    
        		    assignedRoleWithProbability = model.classify(featureVector);
        		    annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), nonTerminal.getId() + ":" + assignedRoleWithProbability.getValue());
        		}
        	    } // for
        	    annotationFrame.setTargetLemma(targetLemma);
        	    sentence.addFrame(annotationFrame);
		} // for targetLemmaIdRef
            } // for targetLemmaIdRefs

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

            //if sentence is already in corpus --> just add frames TODO: test!
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

}
