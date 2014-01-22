
package Classifier;

import Classifier.bean.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Arne on 09.12.13.
 */
public class Model {
	//private static List<String> dummyRoles = Arrays.asList("dummyRole1");//,"dummyRole2","dummyRole3","dummyRole4","dummyRole5","dummyRole6","dummyRole7","dummyRole8","dummyRole9","dummyRole10");//new ArrayList<String>();//"dummy";

	private static double smoothingValue = Math.log(0.000001);
	private static final String modelOutSplitChar = "\t";


	private int totalCount = 0;

	// FeatureType > [ FeatureValue > Frequency] : count per (featureValue per featureType)
	private Map<String, MultiSet<String>> featureValueFrequency = new HashMap<String, MultiSet<String>>();
	// FeatureType > [ FeatureValue > [ Role > Probability]]
	private Map<String, Map<String, Double>> featureValueRelativeRoleFrequency = new HashMap<String, Map<String, Double>>();

	// TargetLemma > FrameName
	//private Map<String, List<String>> targetLemmata = new HashMap<String, List<String>>();
	//private List<String> roles = new ArrayList<String>();

	private FeatureExtractor featureExtractor;

	public Model(FeatureExtractor featureExtractor) {
		this.featureExtractor = featureExtractor;
	}

	public Map<String, MultiSet<String>> getFeatureValueFrequency() {
		return featureValueFrequency;
	}

	//public static String getDummyRole() {
	//	return dummyRole;
	//}

	public Set<String> getTargetLemmata() {

		return featureValueFrequency.get("target").getSet();
	}

	private void incCount(String featureType, String featureValue) {

		if (!featureValueFrequency.containsKey(featureType)) {
			MultiSet<String> featureValues = new MultiSet<String>();
			featureValueFrequency.put(featureType, featureValues);
		}
		featureValueFrequency.get(featureType).add(featureValue);
	}

	public void addFeatureVector(FeatureVector featureVector) throws Exceptions.FeatureTypeNotFoundException {
		for (Entry<String, String> pair : featureVector.getFilteredPowerSet(featureExtractor.getFeatureTypes().getUsedFeatures()).entrySet()) {
			incCount(pair.getKey(), pair.getValue());
			totalCount++;
		}
	}


	public void calculateRelativeFrequenciesPerRole() {

		featureValueFrequency.put("all", new MultiSet<String>());
		featureValueFrequency.get("all").add("all", totalCount);
		for (String featureType : featureExtractor.getFeatureTypes().getUsedFeatures()) {
			if (featureType.contains(Const.roleTypeIdentifier)) {
				int roleIndex = 0;
				String role = "";
				int i = 0;
				for (String featureTypePart : featureType.split(Const.splitChar)) {
					if (featureTypePart.equals(Const.roleTypeIdentifier)) {
						roleIndex = i;
						role = featureTypePart;
					}
					i++;
				}

				for (Entry<String, Integer> countsPerRolePerFeatureValue : featureValueFrequency.get(featureType).getMap().entrySet()) {
					String featureValue = countsPerRolePerFeatureValue.getKey();
					String[] featureValueParts = featureValue.split(Const.splitChar);

					if (!featureValueRelativeRoleFrequency.containsKey(featureType)) {
						featureValueRelativeRoleFrequency.put(featureType, new HashMap<String, Double>());
					}
					if (featureValueParts.length == 1) {
						featureValueRelativeRoleFrequency.get(featureType).put(featureValue, Math.log(countsPerRolePerFeatureValue.getValue()) - Math.log(totalCount));
					} else
						featureValueRelativeRoleFrequency.get(featureType).put(featureValue, Math.log(countsPerRolePerFeatureValue.getValue()) - Math.log(featureValueFrequency.get(role).get(featureValueParts[roleIndex])));
				}
			} else {
				// wirklich noetig??
				/*for (Entry<String, Integer> countsPerRolePerFeatureValue : featureValueFrequency.get(featureType).getMap().entrySet()) {
					String featureValue = countsPerRolePerFeatureValue.getKey();
					if (!featureValueRelativeRoleFrequency.containsKey(featureType)) {
						Map<String, Double> valueProbabilities = new HashMap<String, Double>();
						featureValueRelativeRoleFrequency.put(featureType, valueProbabilities);
					}
					featureValueRelativeRoleFrequency.get(featureType).put(featureValue, Math.log(countsPerRolePerFeatureValue.getValue()) - Math.log(totalCount));
				} */
			}
		}
		//System.out.println();
	}

	// return [role > probability]
	public Entry<String, Double> classify(FeatureVector featureVector) throws Exceptions.FeatureTypeNotFoundException {
		List<String> startingFeatureTypes = FeatureTypes.backOffFeature("");

		String bestRoleName = "";
		double bestRoleProbability = Double.NEGATIVE_INFINITY;

		double roleProbability = 0.0;

		for (String role : featureValueRelativeRoleFrequency.get(Const.roleTypeIdentifier).keySet()) {
			featureVector.addFeature(Const.roleTypeIdentifier, role);
			if(featureValueRelativeRoleFrequency.get(Const.roleTypeIdentifier).containsKey(role)){
				roleProbability = featureValueRelativeRoleFrequency.get(Const.roleTypeIdentifier).get(role) + getRoleProbability(startingFeatureTypes, featureVector);
			} else{
				roleProbability = smoothingValue + getRoleProbability(startingFeatureTypes, featureVector);
		}
			if (bestRoleProbability < roleProbability) {
				bestRoleProbability = roleProbability;
				bestRoleName = role;
			}
		}
		Entry<String, Double> bestRole;

		bestRole = new AbstractMap.SimpleEntry<String, Double>(bestRoleName, bestRoleProbability);
		return bestRole;
	}

	public Double getRoleProbability(List<String> featureTypes, FeatureVector featureVector) throws Exceptions.FeatureTypeNotFoundException {

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
					probability += getRoleProbability(FeatureTypes.backOffFeature(featureType), featureVector);
				}

			} else {
				probability += getRoleProbability(FeatureTypes.backOffFeature(featureType), featureVector);
			}
		}

		return probability;
	}

	public void writeModelToFile(String fileName) throws IOException {

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));

		//write smoothingValue
		out.write(">smoothingValue\n");
		out.write(Double.toString(smoothingValue) + "\n");

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

	public void readModelFromFile(String fileName) throws IOException{

		BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));

		//read smoothingValue
		in.readLine();
		smoothingValue = Double.parseDouble(in.readLine());

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
