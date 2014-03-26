package Classifier.bean;

import java.io.*;
import java.util.*;

/**
 * Created by Arne on 22.01.14.
 */
public class FeatureTypes {
	private static List<String> usedFeatures;
	private static Map<String, List<String>> backOffRules = new HashMap<String, List<String>>();
	private static final String splitMap = ">";
	private static final String splitList = ",";
	private static final char commentIndicator = '%';

	static {
		String splitChar = Const.splitChar;
		String roleIdent = Const.roleTypeIdentifier;

		//// Back-Off-Lattice Jurafsk @P20 Fig. 8c ///
		//used while classification: in model.classify and
		backOffRules.put("",
				Arrays.asList(
						roleIdent + splitChar + "head" + splitChar + "synCat" + splitChar + "target",
						roleIdent + splitChar + "target" + splitChar + "synCat" + splitChar + "path",
						roleIdent + splitChar + "nextHead"
						//roleIdent + splitChar + "target",
						//roleIdent + splitChar + "position",
						//roleIdent + splitChar + "path" + splitChar + "synCat",
						//roleIdent + splitChar + "head",
						//roleIdent + splitChar + "nextHead"

						//roleIdent + splitChar + "terminal"
						//roleIdent + splitChar + "funcPath"
						//roleIdent + splitChar + "funcBig",
						//roleIdent + splitChar + "funcSmall",
						//roleIdent + splitChar + "funcBig"+ splitChar + "funcSmall"
				));
		backOffRules.put(roleIdent + splitChar + "head" + splitChar + "synCat" + splitChar + "target",
				Arrays.asList(
						roleIdent + splitChar + "head" + splitChar + "target",
						roleIdent + splitChar + "target" + splitChar + "synCat"));
		backOffRules.put(roleIdent + splitChar + "target" + splitChar + "synCat" + splitChar + "path",
				Arrays.asList(
						roleIdent + splitChar + "target" + splitChar + "synCat",
						roleIdent + splitChar + "path" + splitChar + "synCat"));
		backOffRules.put(roleIdent + splitChar + "head" + splitChar + "target",
				Arrays.asList(
						roleIdent + splitChar + "head",
						roleIdent + splitChar + "target"));
		backOffRules.put(roleIdent + splitChar + "target" + splitChar + "synCat",
				Arrays.asList(
						roleIdent + splitChar + "target"));

		backOffRules.put(roleIdent + splitChar + "path" + splitChar + "synCat",
				Arrays.asList(
						roleIdent + splitChar + "path",
						roleIdent + splitChar + "synCat"));
		/*backOffRules.put(roleIdent + splitChar + "funcBig"+ splitChar + "funcSmall",
				Arrays.asList(
						roleIdent + splitChar + "funcBig",
						roleIdent + splitChar + "funcSmall"));
        */

		/*usedFeatures = new ArrayList<String>();
		usedFeatures.add(roleIdent);
		for(List<String> backOffValues: backOffRules.values()){
			for(String backOffValue: backOffValues){
				usedFeatures.add(backOffValue);
				usedFeatures.add(backOffValue.replaceFirst(roleIdent + splitChar, ""));
			}
		}*/
		//used while training: in model.addFeatureVector and model.calculateRelativeFrequenciesPerRole
		usedFeatures = Arrays.asList(
				roleIdent,
				"target",
				"target" + splitChar + "synCat",
				"target" + splitChar + "synCat" + splitChar + "path",
				"synCat",
				//"position",
				"path",
				"path" + splitChar + "synCat",
				"head",
				//"head" + splitChar + "synCat",
				"head" + splitChar + "target",
				"head" + splitChar + "synCat" + splitChar + "target",
				"nextHead",
				//"terminal",
				//"funcPath",
				//"funcBig",
				//"funcSmall",
				//"funcBig"+ splitChar + "funcSmall",
				////roleIdent + splitChar + "target",   //decreases F-Measure by 12% !
				roleIdent + splitChar + "target" + splitChar + "synCat",
				roleIdent + splitChar + "target" + splitChar + "synCat" + splitChar + "path",
				roleIdent + splitChar + "synCat",
				//roleIdent + splitChar + "position",
				roleIdent + splitChar + "path",
				roleIdent + splitChar + "path" + splitChar + "synCat",
				roleIdent + splitChar + "head",
				//roleIdent + splitChar + "head" + splitChar + "synCat",
				roleIdent + splitChar + "head" + splitChar + "target",
				roleIdent + splitChar + "head" + splitChar + "synCat" + splitChar + "target",
				roleIdent + splitChar + "nextHead"
				//roleIdent + splitChar + "terminal"
				//roleIdent + splitChar + "funcPath"
				//roleIdent + splitChar + "funcBig",
				//roleIdent + splitChar + "funcSmall",
				//roleIdent + splitChar + "funcBig"+ splitChar + "funcSmall"
		);
	}

	public static List<String> backOffFeature(String concatenatedFeature) {
		return backOffRules.get(concatenatedFeature);
	}

	public static List<String> getUsedFeatures() {
		return usedFeatures;
	}

	public static void readFeatureTypesFromFile(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		backOffRules = new HashMap<String, List<String>>();
		usedFeatures = new LinkedList<String>();
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
					if(pair.length==0){
						pair = new String[2];
						pair[0] = "";
						pair[1] = "";
					}
					if (!pair[0].isEmpty()) {
						key = Const.roleTypeIdentifier + Const.splitChar + pair[0];
						valueStrings = pair[1].split(splitList);
					} else {
						if(!pair[1].isEmpty()){
							valueStrings = pair[1].split(splitList);
						}else{
							valueStrings = new String[1];
							valueStrings[0] = Const.targetTypeIdentifier;
						}
					}

					for (int i = 0; i < valueStrings.length; i++) {
						if(!usedFeatures.contains(valueStrings[i])) {
							usedFeatures.add(valueStrings[i]);
						}
						valueStrings[i] = Const.roleTypeIdentifier + Const.splitChar + valueStrings[i];
						if(!usedFeatures.contains(valueStrings[i])) {
							usedFeatures.add(valueStrings[i]);
						}
					}
					backOffRules.put(key, Arrays.asList(valueStrings));
				} /*else {
					if (line.contains(splitList)) {
						for (String featureType : line.split(splitList)) {
							usedFeatures.add(featureType);
							usedFeatures.add(Const.roleTypeIdentifier + Const.splitChar + featureType);
						}
					} else {
						usedFeatures.add(line);
						usedFeatures.add(Const.roleTypeIdentifier + Const.splitChar + line);
					}
				} */
			}

			line = in.readLine();
			if (line != null)
				line = line.replaceAll("[\t ]+", "");
			//System.out.println();
		}
		in.close();
		Collections.sort(usedFeatures);
		System.out.println();
	}

	public static boolean isUsedFeatureType(String featureType) {
		return usedFeatures.contains(featureType);
	}
}
