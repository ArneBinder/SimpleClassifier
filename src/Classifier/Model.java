
package Classifier;

import Classifier.bean.FeatureVector;
import Classifier.bean.MultiSet;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Arne on 09.12.13.
 */
public class Model {
    private static List<String> dummyRoles = Arrays.asList("dummyRole1");//,"dummyRole2","dummyRole3","dummyRole4","dummyRole5","dummyRole6","dummyRole7","dummyRole8","dummyRole9","dummyRole10");//new ArrayList<String>();//"dummy";
    private double smoothingValue = Math.log(0.000001);
    private static final String modelOutSplitChar = "\t";


    // FeatureType > [ FeatureValue > Frequency] : count per (featureValue per featureType)
    private Map<String, MultiSet<String>> featureValueFrequency = new HashMap<String, MultiSet<String>>();
    // FeatureType > [ FeatureValue > [ Role > Probability]]
    private Map<String, Map<String, Double>> featureValueRelativeRoleFrequency = new HashMap<String, Map<String, Double>>();

    // TargetLemma > FrameName
    //private Map<String, List<String>> targetLemmata = new HashMap<String, List<String>>();
    //private List<String> roles = new ArrayList<String>();

    private FeatureExtractor featureExtractor;

    public Model(FeatureExtractor featureExtractor) {
        //roles.addAll(dummyRoles);
        this.featureExtractor = featureExtractor;
    }

    public static List<String> getDummyRoles() {
        return dummyRoles;
    }

    public Set<String> getTargetLemmata() {

        return featureValueFrequency.get("target").getSet();
    }
/*
   public void addTargetWord(String frameName, String targetWord) {
        List<String> targetFrames;
        if (!targetLemmata.containsKey(targetWord)) {
            targetFrames = new LinkedList<String>();
            targetFrames.add(frameName);
        } else {
            targetFrames = targetLemmata.get(targetWord);
        }
        targetLemmata.put(targetWord, targetFrames);
    }*/

    private void incCount(String featureType, String featureValue) {

        if (!featureValueFrequency.containsKey(featureType)) {
            MultiSet<String> featureValues = new MultiSet<String>();
            featureValueFrequency.put(featureType, featureValues);
        }
        featureValueFrequency.get(featureType).add(featureValue);
    }

    public void addFeatureVector(FeatureVector featureVector) throws Exception {
        for (Entry<String, String> pair : featureVector.getFilteredPowerSet(featureExtractor.getUsedFeatures()).entrySet()) {
            incCount(pair.getKey(), pair.getValue());
        }
    }


    public void calculateRelativeFrequenciesPerRole() {
        for (String featureType : featureExtractor.getUsedFeatures()) {
            if (featureType.contains(FeatureVector.getRoleTypeIdentifier())) {
                int roleIndex = 0;
                String featureTypeWithOutRole = "";
                int i = 0;
                for (String featureTypePart : featureType.split(FeatureVector.getSplitChar())) {
                    if (featureTypePart.equals(FeatureVector.getRoleTypeIdentifier()))
                        roleIndex = i;
                    else
                        featureTypeWithOutRole += FeatureVector.getSplitChar() + featureTypePart;
                    i++;
                }
                if(featureTypeWithOutRole.length() > 0)
                    featureTypeWithOutRole = featureTypeWithOutRole.substring(1);

                for (Entry<String, Integer> countsPerRolePerFeatureValue : featureValueFrequency.get(featureType).getMap().entrySet()) {
                    String featureValue = countsPerRolePerFeatureValue.getKey();
                    String[] featureValueParts = featureValue.split(FeatureVector.getSplitChar());
                    //String currRole = featureValueParts[roleIndex];
                    String featureValueWithoutRole = "";
                    for (int j = 0; j < roleIndex; j++) {
                        featureValueWithoutRole += FeatureVector.getSplitChar() + featureValueParts[j];
                    }
                    for (int j = roleIndex + 1; j < featureValueParts.length; j++) {
                        featureValueWithoutRole += FeatureVector.getSplitChar() + featureValueParts[j];
                    }
                    if(featureValueWithoutRole.length() > 0)
                        featureValueWithoutRole = featureValueWithoutRole.substring(1);

                    if (!featureValueRelativeRoleFrequency.containsKey(featureType)) {
                        Map<String, Double> valueProbabilities = new HashMap<String, Double>();
                        featureValueRelativeRoleFrequency.put(featureType, valueProbabilities);
                    }

                    try{
                    if (featureTypeWithOutRole.equals(""))
                        featureValueRelativeRoleFrequency.get(featureType).put(featureValue, Math.log(1.0));
                    else
                        featureValueRelativeRoleFrequency.get(featureType).put(featureValue, Math.log(countsPerRolePerFeatureValue.getValue()) - Math.log(featureValueFrequency.get(featureTypeWithOutRole).get(featureValueWithoutRole)));

                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
            }
        }
        System.out.println();
    }

    // return [role > probability]
    public Entry<String, Double> classify(FeatureVector featureVector) throws Exception {
        List<String> startingFeatureTypes = featureExtractor.backOffFeature("");

        String bestRoleName = "";
        double bestRoleProbability = 0.0;

        double roleProbability = 0.0;

        for (String role : featureValueRelativeRoleFrequency.get(FeatureVector.getRoleTypeIdentifier()).keySet()) {
            featureVector.addFeature(FeatureVector.getRoleTypeIdentifier(), role);
            roleProbability = getRoleProbability(startingFeatureTypes, featureVector);
            if (bestRoleProbability < Math.exp(roleProbability)) {
                bestRoleProbability = Math.exp(roleProbability);
                bestRoleName = role;
            }
        }
        Entry<String, Double> bestRole;
        if (dummyRoles.contains(bestRoleName))
            bestRole = new AbstractMap.SimpleEntry<String, Double>("dummyRole", bestRoleProbability);
        else
            //if(bestRoleProbability > Math.exp(smoothingValue))//Math.exp(smoothingValue))
            bestRole = new AbstractMap.SimpleEntry<String, Double>(bestRoleName, bestRoleProbability);
        //else{
        //	bestRole = new AbstractMap.SimpleEntry<String, Double>(dummyRole, bestRoleProbability);
        //}
        return bestRole;
    }

    public Double getRoleProbability(List<String> featureTypes, FeatureVector featureVector) throws Exception {

        if (featureTypes == null)
            return smoothingValue;


        double probability = 0.0;

        for (String featureType : featureTypes) {

            if (featureValueRelativeRoleFrequency.containsKey(featureType)) {
                String featureValue = featureVector.getFeatureValue(featureType);

                if (featureValueRelativeRoleFrequency.get(featureType).containsKey(featureValue)) {

                    probability += featureValueRelativeRoleFrequency.get(featureType).get(featureValue);

                } else {
                    //try{
                        probability += getRoleProbability(featureExtractor.backOffFeature(featureType), featureVector);



                }

            } else {
                probability += getRoleProbability(featureExtractor.backOffFeature(featureType), featureVector);
            }
        }

        return probability;
    }

    public void writeModelToFile(String fileName) throws Exception {

        BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));

        //write smoothingValue
        out.write(">smoothingValue\n");
        out.write(Double.toString(smoothingValue) + "\n");

        //write targetLemmata
        //out.write(">targetLemmata\n");
        //for (String targetLemma : targetLemmata.keySet()) {
        //    out.write(targetLemma + FeatureVector.getSplitChar());
        //}
        //out.write("\n");

        //write roles
        //out.write(">roles\n");
        //for (String role : roles) {
        //    out.write(role + FeatureVector.getSplitChar());
        //}
        //out.write("\n");

        //write featureValueRelativeRoleFrequency
        out.write(">frequencies: featureType\tfeatureValue\tfrequency\t[relativeFrequency]\n");
        //role#featureType:featureValue:probability
        //String valueFrequency = "";
        int frequency;
        String featureValue;
        //Double probability = 0.0;
        String relativeFrequency;
        for (String featureType : featureValueFrequency.keySet()) {
            //for (String featureValue : featureValueRelativeRoleFrequency.get(featureType).keySet()) {
            for (Entry<String, Integer> valueFrequency : featureValueFrequency.get(featureType).entrySet()) {
                frequency = valueFrequency.getValue();
                featureValue = valueFrequency.getKey();
                try {
                    relativeFrequency = Double.toString(Math.exp(featureValueRelativeRoleFrequency.get(featureType).get(featureValue)));
                } catch (NullPointerException e) {
                    relativeFrequency = Double.toString(0.0);
                }
                out.write(featureType + modelOutSplitChar + featureValue + modelOutSplitChar + frequency + modelOutSplitChar + relativeFrequency + "\n");
            }
            //}
        }
        out.close();
    }

    public void readModelFromFile(String fileName) throws Exception {

        BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));

        //read smoothingValue
        in.readLine();
        smoothingValue = Double.parseDouble(in.readLine());

        //read targetLemmata
        //in.readLine();
        //for (String targetLemma : in.readLine().split(FeatureVector.getSplitChar())) {
        //    targetLemmata.put(targetLemma, new LinkedList<String>());
        //}

        //read roles
        //in.readLine();
        //roles = Arrays.asList(in.readLine().split(FeatureVector.getSplitChar()));

        //read featureValueRelativeRoleFrequency
        in.readLine();
        featureValueRelativeRoleFrequency = new HashMap<String, Map<String, Double>>();
		featureValueFrequency = new HashMap<String, MultiSet<String>>();
        String line = in.readLine();
        String[] lineParts;
        String[] lastLineParts = new String[2];
        while (line != null) {
            lineParts = line.split(modelOutSplitChar);
            if (!lineParts[0].equals(lastLineParts[0])) {
                featureValueRelativeRoleFrequency.put(lineParts[0], new HashMap<String, Double>());
				featureValueFrequency.put(lineParts[0], new MultiSet<String>());
                lastLineParts[0] = lineParts[0];
            }
            featureValueRelativeRoleFrequency.get(lineParts[0]).put(lineParts[1], Math.log(Double.parseDouble(lineParts[3])));
			featureValueFrequency.get(lineParts[0]).add(lineParts[1], Integer.parseInt(lineParts[2]));
            line = in.readLine();
        }
        in.close();
    }


}
