
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
	private List<String> dummyRole = Arrays.asList("dummyRole1", "dummyRole2", "dummyRole3", "dummyRole4", "dummyRole5", "dummyRole6", "dummyRole7", "dummyRole8", "dummyRole9", "dummyRole10");//new ArrayList<String>();//"dummy";
	private double smoothingValue = Math.log(0.000001);
	private static final String modelOutSplitChar = "\t";


	// FeatureType > [ FeatureValue > [ Role + Count]] : (count per role) per (featureValue per featureType)
	private Map<String, Map<String, MultiSet<String>>> featureValueCounts = new HashMap<String, Map<String, MultiSet<String>>>();
	// FeatureType > [ FeatureValue > [ Role > Probability]]
	private Map<String, Map<String, Map<String, Double>>> probabilities = new HashMap<String, Map<String, Map<String, Double>>>();

	// TargetLemma > FrameName 
	private Map<String, List<String>> targetLemmata = new HashMap<String, List<String>>();
	private List<String> roles = new ArrayList<String>();

	private FeatureExtractor featureExtractor;

	public Model(FeatureExtractor featureExtractor) {
		roles.addAll(dummyRole);
		this.featureExtractor = featureExtractor;
	}

	public Map<String, List<String>> getTargetLemmata() {
		return targetLemmata;
	}

	public void addTargetWord(String frameName, String targetWord) {
		List<String> targetFrames;
		if (!targetLemmata.containsKey(targetWord)) {
			targetFrames = new LinkedList<String>();
			targetFrames.add(frameName);
		} else {
			targetFrames = targetLemmata.get(targetWord);
		}
		targetLemmata.put(targetWord, targetFrames);
	}

	private void incCount(String role, String featureType, String featureValue) {

		if (!featureValueCounts.containsKey(featureType)) {
			Map<String, MultiSet<String>> featureValues = new HashMap<String, MultiSet<String>>();
			featureValueCounts.put(featureType, featureValues);
		}

		if (!featureValueCounts.get(featureType).containsKey(featureValue)) {
			featureValueCounts.get(featureType).put(featureValue, new MultiSet<String>());
		}

		featureValueCounts.get(featureType).get(featureValue).add(role);

		if (!roles.contains(role)) roles.add(role);
	}

	public void addFeatureVector(String role, FeatureVector featureVector) throws Exception {
		for (Entry<String, String> pair : featureVector.getFilteredPowerSet(featureExtractor.getUsedFeatures()).entrySet()) {
			incCount(role, pair.getKey(), pair.getValue());
		}
	}

	public void addFeatureVector(FeatureVector featureVector) throws Exception {
		for (Entry<String, String> pair : featureVector.getFilteredPowerSet(featureExtractor.getUsedFeatures()).entrySet()) {
			incCount(dummyRole.get(myRandom(0, dummyRole.size()-1)), pair.getKey(), pair.getValue());
		}
	}

	public void calculateProbabilities() {
		int sum;

		for (String featureType : featureExtractor.getUsedFeatures()) {

			for (Entry<String, MultiSet<String>> countsPerRolePerFeatureValue : featureValueCounts.get(featureType).entrySet()) {
				sum = 0;

				String featureValue = countsPerRolePerFeatureValue.getKey();
				Set<Entry<String, Integer>> countPerRole = countsPerRolePerFeatureValue.getValue().getMap().entrySet();

				for (Entry<String, Integer> roleCount : countPerRole) {
					sum += roleCount.getValue();
				}

				if (!probabilities.containsKey(featureType)) {
					Map<String, Map<String, Double>> valueProbabilities = new HashMap<String, Map<String, Double>>();
					probabilities.put(featureType, valueProbabilities);
				}

				if (!probabilities.get(featureType).containsKey(featureValue)) {
					Map<String, Double> countsPerRole = new HashMap<String, Double>();
					probabilities.get(featureType).put(featureValue, countsPerRole);
				}

				for (Entry<String, Integer> roleCount : countPerRole) {
					probabilities.get(featureType).get(featureValue).put(roleCount.getKey(), Math.log(roleCount.getValue()) - Math.log(sum));
				}
			}
		}
	}

	// return [role > probability]
	public Entry<String, Double> classify(FeatureVector featureVector) throws Exception {
		List<String> startingFeatureTypes = featureExtractor.backOffFeature("");

		String bestRoleName = "";
		double bestRoleProbability = 0.0;

		double roleProbability = 0.0;

		for (String role : roles) {
			roleProbability = getRoleProbability(role, startingFeatureTypes, featureVector);
			if (bestRoleProbability < Math.exp(roleProbability)) {
				bestRoleProbability = Math.exp(roleProbability);
				bestRoleName = role;
			}
		}
		Entry<String, Double> bestRole;
        if(dummyRole.contains(bestRoleName))
            bestRole = new AbstractMap.SimpleEntry<String, Double>("dummyRole", bestRoleProbability);
        else
		//if(bestRoleProbability > Math.exp(smoothingValue))//Math.exp(smoothingValue))
			bestRole = new AbstractMap.SimpleEntry<String, Double>(bestRoleName, bestRoleProbability);
		//else{
		//	bestRole = new AbstractMap.SimpleEntry<String, Double>(dummyRole, bestRoleProbability);
		//}
		return bestRole;
	}

	public Double getRoleProbability(String role, List<String> featureTypes, FeatureVector featureVector) throws Exception {

		if (featureTypes.size() == 0)
			return smoothingValue;

		double probability = 0.0;
		for (String featureType : featureTypes) {

			if (probabilities.containsKey(featureType)) {
				String featureValue = featureVector.getFeatureValue(featureType);

				if (probabilities.get(featureType).containsKey(featureValue)) {

					if (probabilities.get(featureType).get(featureValue).containsKey(role)) {
						probability += probabilities.get(featureType).get(featureValue).get(role);
					} else {
						probability += getRoleProbability(role, featureExtractor.backOffFeature(featureType), featureVector);
					}
				} else {
					probability += getRoleProbability(role, featureExtractor.backOffFeature(featureType), featureVector);
				}
			} else {
				probability += getRoleProbability(role, featureExtractor.backOffFeature(featureType), featureVector);
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
		out.write(">targetLemmata\n");
		for (String targetLemma : targetLemmata.keySet()) {
			out.write(targetLemma + FeatureVector.getSplitChar());
		}
		out.write("\n");

		//write roles
		out.write(">roles\n");
		for (String role : roles) {
			out.write(role + FeatureVector.getSplitChar());
		}
		out.write("\n");

		//write probabilities
		out.write(">probabilities: featureType\tfeatureValue\trole\tprobability\tcount\n");
		//featureType:featureValue:role:probability
		String role = "";
		Double probability = 0.0;
		for (String featureType : probabilities.keySet()) {
			for (String featureValue : probabilities.get(featureType).keySet()) {
				for (Entry<String, Double> roleProbability : probabilities.get(featureType).get(featureValue).entrySet()) {
					role = roleProbability.getKey();
					probability = roleProbability.getValue();
					out.write(featureType+modelOutSplitChar+featureValue+modelOutSplitChar+role+modelOutSplitChar+Double.toString(probability)+modelOutSplitChar+featureValueCounts.get(featureType).get(featureValue).get(role)+"\n");
				}
			}
		}
		out.close();
	}

	public void readModelFromFile(String fileName) throws Exception{

		BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));

		//read smoothingValue
		in.readLine();
		smoothingValue = Double.parseDouble(in.readLine());

		//read targetLemmata
		in.readLine();
		for(String targetLemma: in.readLine().split(FeatureVector.getSplitChar())){
			targetLemmata.put(targetLemma,new LinkedList<String>());
		}

		//read roles
		in.readLine();
		roles = Arrays.asList(in.readLine().split(FeatureVector.getSplitChar()));

		//read probabilities
		in.readLine();
		probabilities = new HashMap<String, Map<String, Map<String, Double>>>();
		String line=in.readLine();
		String[] lineParts;
		String[] lastLineParts = new String[2];
		while(line!=null){
			lineParts = line.split(modelOutSplitChar);
			if(!lineParts[0].equals(lastLineParts[0])){
				probabilities.put(lineParts[0],new HashMap<String, Map<String, Double>>());
				lastLineParts[0] = lineParts[0];
			}
			if(!lineParts[1].equals(lastLineParts[1])){
				probabilities.get(lineParts[0]).put(lineParts[1],new HashMap<String, Double>());
				lastLineParts[1] = lineParts[1];
			}
			probabilities.get(lineParts[0]).get(lineParts[1]).put(lineParts[2], Double.parseDouble(lineParts[3]));
			line=in.readLine();
		}
		in.close();
	}
    public static int myRandom(int low, int high) {
        return (int) (Math.random() * (high - low) + low);
    }
}
