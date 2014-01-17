package Classifier;

import Classifier.bean.FeatureVector;
import Classifier.bean.MultiSet;

import java.util.Map;

/**
 * Created by Arne on 14.01.14.
 */
public class StatisticalTest {
	public static void main(String[] args) throws Exception {
		if (args.length < 4)
			System.out.println("Wrong arguments: -chisquare modelFileName featureA featureB");
		else {
			Corpus dummyCorpus = new Corpus();
			Model model = new Model(dummyCorpus.getFeatureExtractor());
			System.out.println("read model from file "+args[1]+"...");
			model.readModelFromFile(args[1]);
			if (args[0].equals("-chisquare")) {
				System.out.println("start calculation... "+args[2]+" x "+args[3]);
				double p = calculateChiSquare(model, args[2], args[3], FeatureVector.getSplitChar());
				System.out.println("p-value: " + p);
			} else {
				System.out.println(args[0] + ": kind of test unknown");
			}
		}
	}

	public static double calculateChiSquare(Model model, String featureA, String featureB, String splitChar) {
		if (model.getFeatureValueFrequency().get(featureA + splitChar + featureB) == null) {
			System.out.println("feature key \""+featureA + splitChar + featureB+"\" not found in model. wrong order of feature keys?");
			return 0.0;
		} else {
			double all = model.getFeatureValueFrequency().get("all").get("all");
			double p = 0;
			double expected;
			double real;
			for (Map.Entry<String, Integer> fA : model.getFeatureValueFrequency().get(featureA).getMap().entrySet()) {
				for (Map.Entry<String, Integer> fB : model.getFeatureValueFrequency().get(featureB).getMap().entrySet()) {
					expected = (fA.getValue() / all) * fB.getValue() ;
					/*if(all < 0)
						System.out.println("all: "+all);
					if(fA.getValue() < 0)
						System.out.println("fA: "+fA.getValue());
					if(fB.getValue() < 0)
						System.out.println("fB: "+fB.getValue());
					if(expected < 0)
						System.out.println("fA: "+fA.getValue() + "; fB: "+fB.getValue()+ " => "+(fA.getValue()*fB.getValue())+" => "+expected);   */
					real = model.getFeatureValueFrequency().get(featureA + splitChar + featureB).get(fA.getKey() + splitChar + fB.getKey());
					p += (real - expected) * (real - expected) / expected;
				}
			}
			return p;
		}
	}

}
