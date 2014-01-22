package Classifier.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 22.01.14.
 */
public class FeatureTypes {
	private static final List<String> usedFeatures;
	private static Map<String, List<String>> backOffRules = new HashMap<String, List<String>>();
	private static final String splitMap = ">";
	private static final String splitList = ",";
	private static final String commentIndicator = "%";

	static {
		String splitChar = Const.splitChar;
		String roleIdent = Const.roleTypeIdentifier;
		//used while classification: in model.classify and
		backOffRules.put("",
				Arrays.asList(
						roleIdent + splitChar + "target",
						roleIdent + splitChar + "position",
						roleIdent + splitChar + "path" + splitChar + "synCat",
						roleIdent + splitChar + "head",
						roleIdent + splitChar + "nextHead"

						//roleIdent + splitChar + "terminal"
						//roleIdent + splitChar + "funcPath"
						//roleIdent + splitChar + "funcBig",
						//roleIdent + splitChar + "funcSmall",
						//roleIdent + splitChar + "funcBig"+ splitChar + "funcSmall"
				));
		backOffRules.put(roleIdent + splitChar + "path" + splitChar + "synCat",
				Arrays.asList(
						roleIdent + splitChar + "path",
						roleIdent + splitChar + "synCat"));
		/*backOffRules.put(roleIdent + splitChar + "funcBig"+ splitChar + "funcSmall",
				Arrays.asList(
						roleIdent + splitChar + "funcBig",
						roleIdent + splitChar + "funcSmall"));
        */


		//used while training: in model.addFeatureVector and model.calculateRelativeFrequenciesPerRole
		// TODO complete rule set @ P17 @ Jurafsky
		usedFeatures = Arrays.asList(
				roleIdent,
				"target",
				"synCat",
				"position",
				"path",
				"path" + splitChar + "synCat",
				"head",
				"nextHead",
				//"terminal",
				//"funcPath",
				//"funcBig",
				//"funcSmall",
				//"funcBig"+ splitChar + "funcSmall",
				roleIdent + splitChar + "target",
				roleIdent + splitChar + "synCat",
				roleIdent + splitChar + "position",
				roleIdent + splitChar + "path",
				roleIdent + splitChar + "path" + splitChar + "synCat",
				roleIdent + splitChar + "head",
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

	public static void readFeatureTypesFromFile(String fileName){


	}
}
