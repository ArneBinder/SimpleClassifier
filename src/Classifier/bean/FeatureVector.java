package Classifier.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureVector {

	private static String splitChar = "#";
	private Map<String, String> features = new HashMap<String, String>();

	public static String getSplitChar() {
		return splitChar;
	}

	public Map<String, String> getFilteredPowerSet(List<String> usedFeatures)
			throws Exception {
		Map<String, String> newFeatures = new HashMap<String, String>();
		for (String usedFeature : usedFeatures) {
			newFeatures.put(usedFeature, getFeatureValue(usedFeature));
		}
		return newFeatures;
	}

	public String getFeatureValue(String featureType) throws Exception {
		String featureValue = "";

		String[] singleFeatureTypes = featureType.split(splitChar);
		for (String singleFeatureType : singleFeatureTypes) {

			if (features.containsKey(singleFeatureType)) {

				if (featureValue.isEmpty()) {
					featureValue = features.get(singleFeatureType);
				} else {
					featureValue += "#" + features.get(singleFeatureType);
				}
			} else {
				throw new Exception("FeatureType: " + featureType
						+ " is missing on current FeatureVector. "
						+ this.toString());
			}
		}

		return featureValue;
	}

	public void addFeature(String identifer, String value) {
		features.put(identifer, value);
	}
}
