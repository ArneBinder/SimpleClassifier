package Classifier;

import Classifier.bean.MultiSet;

import java.util.Map;

/**
 * Created by Arne on 14.01.14.
 */
public class StatisticalTest {

	public double calculateChiSquare(Model model, String featureA, String featureB, String splitChar) {
		double all = model.getFeatureValueFrequency().get("all").get("all");
		double p = 0;
		double expected;
		double real;
		for (Map.Entry<String, Integer> fA : model.getFeatureValueFrequency().get(featureA).getMap().entrySet()) {
			for (Map.Entry<String, Integer> fB : model.getFeatureValueFrequency().get(featureB).getMap().entrySet()) {
				expected = fA.getValue() * fB.getValue() / all;
				real = model.getFeatureValueFrequency().get(featureA + splitChar + featureB).get(fA.getKey() + splitChar + fB.getKey());
				p += (real - expected) * (real - expected) / expected;
			}
		}
		return p;
	}

}
