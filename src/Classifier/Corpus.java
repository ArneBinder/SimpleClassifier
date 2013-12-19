package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
                featureExtractor.setSentence(sentence);

                // process FrameElements
                frameElementIDRefs = new ArrayList<String>();
                for (Frame currentFrame : sentence.getFrames()) {

                    sentence.setTarget(currentFrame.getTargetIDs());
                    for (Entry<String, List<String>> frameElement : currentFrame.getFrameElements().entrySet()) {
                        String frameElementName = frameElement.getKey(); // = role

                        // get first idref only.. hopefully, there is only one
                        int[] indices = sentence.calculateRootOfSubtree(frameElement.getValue());
                        String frameElementIDRef;

                        if(sentence.getNode(frameElement.getValue().get(0)).getPathsFromRoot().get(indices[1]).length > indices[0])
                            frameElementIDRef = sentence.getNode(frameElement.getValue().get(0)).getPathsFromRoot().get(indices[1])[indices[0]];
                        else
                            frameElementIDRef = frameElement.getValue().get(0);

                        //frameElementIDRef = sentence.getNode(frameElementIDRef).getHeadIDref();
                         //frameElement.getValue().get(0);

                        FeatureVector feFeatureVector = featureExtractor.extract(frameElementIDRef);
                        feFeatureVector.addFeature(FeatureVector.getRoleTypeIdentifier(), frameElementName);
                        model.addFeatureVector(feFeatureVector);
                        frameElementIDRefs.add(frameElementIDRef);
                    }

                    // process all terminal elements
                    FeatureVector currentVector;
                    for (String id : sentence.getTerminals().keySet()) {
                        if (!frameElementIDRefs.contains(id)) {

                            currentVector = featureExtractor.extract(id);
                            currentVector.addFeature(FeatureVector.getRoleTypeIdentifier(), model.getDummyRoles().get(myRandom(0, model.getDummyRoles().size() - 1)));
                            model.addFeatureVector(currentVector);
                        }
                    }

                    // process all nonterminal elements
                    for (String id : sentence.getNonterminals().keySet()) {
                        if (!frameElementIDRefs.contains(id) && !sentence.getRootIDref().equals(id)) {

                            currentVector = featureExtractor.extract(id);
                            currentVector.addFeature(FeatureVector.getRoleTypeIdentifier(), model.getDummyRoles().get(myRandom(0, model.getDummyRoles().size() - 1)));
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
        //dout.close();
        model.calculateRelativeFrequenciesPerRole();

        return model;
    }

    public void annotateCorpus(Model model) throws Exception {

        FeatureVector featureVector = null;
        Frame annotationFrame;

        Entry<String, Double> assignedRoleWithProbability;

        for (Sentence sentence : sentences) {

            featureExtractor.setSentence(sentence);

            //List<List<String>> test = sentence.extractTargetIDRefs(model.getTargetLemmata());
            double annotationProbability = 1.0;
            double bestAnnotationProb = 0;
            Frame bestAnnotationFrame = null;
            for (List<String> targetLemmaIDRefs : sentence.extractTargetIDRefs(model
                    .getTargetLemmata())) {

                for (String targetLemmaIDRef : targetLemmaIDRefs) {
                    annotationProbability = 1.0;
                    String targetLemma = sentence.getNode(targetLemmaIDRef).getAttributes().get("lemma");
                    annotationFrame = new Frame("annotationID", "annotatedFrameElements_" + targetLemma);
                    annotationFrame.setTargetLemma(targetLemma);
                    sentence.setTarget(targetLemmaIDRef);
                    // classify all terminals
                    for (Node terminal : sentence.getTerminals().values()) {

                        featureVector = featureExtractor.extract(terminal.getId());

                        assignedRoleWithProbability = model.classify(featureVector);
                        annotationProbability *= assignedRoleWithProbability.getValue();
                        annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), terminal.getId() + ":" + assignedRoleWithProbability.getValue());
                    }

                    // classify all terminals
                    for (Node nonTerminal : sentence.getNonterminals()
                            .values()) {
                        if (!sentence.getRootIDref().equals(nonTerminal.getId())) {

                            featureVector = featureExtractor.extract(nonTerminal.getId());

                            assignedRoleWithProbability = model.classify(featureVector);
                            annotationProbability *= assignedRoleWithProbability.getValue();
                            annotationFrame.addFrameElementWithIDRef(assignedRoleWithProbability.getKey(), nonTerminal.getId() + ":" + assignedRoleWithProbability.getValue());
                        }
                    } // for
                    if(bestAnnotationProb < annotationProbability){
                        bestAnnotationProb = annotationProbability;
                        bestAnnotationFrame = annotationFrame;
                    }
                } // for targetLemmaIdRef
            } // for targetLemmaIdRefs

            sentence.addFrame(bestAnnotationFrame);
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

    public static int myRandom(int low, int high) {
        return (int) (Math.random() * (high - low) + low);
    }

}
