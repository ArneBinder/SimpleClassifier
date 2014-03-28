package Classifier.bean;

import java.io.*;
import java.util.*;

/**
 * Created by Arne on 22.01.14.
 */
public class FeatureTypes {
	//used while training: in model.addFeatureVector and model.calculateRelativeFrequenciesPerRole
	private static Set<String> usedFeatures;
	//used while classification: in model.classify and
	private static Map<String, List<String>> backOffRules = new HashMap<String, List<String>>();
	private static final String splitMap = ">";
	private static final String splitList = ",";
	private static final char commentIndicator = '%';

	static {
		String fn = "src" + System.getProperty("file.separator") + "featureTypes_default.txt";
		try {
			readFeatureTypesFromFile(new File(fn));
		} catch (IOException e) {
			System.out.println("WARNING: couldn't read default featureTypes from \"" + fn + "\". Ensure you have loaded own featureTypes via -featureTypes flag!\n" + e.getMessage());
		}
	}

	public static List<String> backOffFeature(String concatenatedFeature) {
		return backOffRules.get(concatenatedFeature);
	}

	public static Set<String> getUsedFeatures() {
		return usedFeatures;
	}

	public static void readFeatureTypesFromFile(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		backOffRules = new HashMap<String, List<String>>();
		usedFeatures = new HashSet<String>();
		usedFeatures.add(Const.roleTypeIdentifier);
		//usedFeatures.add(Const.targetTypeIdentifier);
		String line = in.readLine().replaceAll("\\s+", "");
		while (line != null) {
			if (!line.isEmpty() && line.charAt(0) != commentIndicator) {

				if (line.contains(splitMap)) {
					String[] pair = line.split(splitMap);
					String key = "";
					String[] valueStrings;
					//System.out.println("line: "+line);
					//System.out.println("pair.length: "+pair.length);
					if (pair.length == 0) {
						pair = new String[2];
						pair[0] = "";
						pair[1] = "";
					}
					if (!pair[0].isEmpty()) {
						key = pair[0];
						valueStrings = pair[1].split(splitList);
					} else {
						if (!pair[1].isEmpty()) {
							valueStrings = pair[1].split(splitList);
						} else {
							valueStrings = new String[1];
							valueStrings[0] = Const.targetTypeIdentifier;
						}
					}

					for (int i = 0; i < valueStrings.length; i++) {

						if (valueStrings[i].contains(Const.splitChar)) {
							for (String featureType : valueStrings[i].split(Const.splitChar)) {
								usedFeatures.add(featureType);
								usedFeatures.add(Const.roleTypeIdentifier + Const.splitChar + featureType);
							}
						}
						if (!usedFeatures.contains(valueStrings[i])) {
							usedFeatures.add(valueStrings[i]);
							usedFeatures.add(Const.roleTypeIdentifier + Const.splitChar + valueStrings[i]);
						}

						valueStrings[i] = valueStrings[i];
					}
					backOffRules.put(key, Arrays.asList(valueStrings));
				}
			}

			line = in.readLine();
			if (line != null)
				line = line.replaceAll("[\t ]+", "");
			//System.out.println();
		}
		in.close();
		//System.out.println();
	}

	public static boolean isUsedFeatureType(String featureType) {
		return usedFeatures.contains(featureType);
	}
}
