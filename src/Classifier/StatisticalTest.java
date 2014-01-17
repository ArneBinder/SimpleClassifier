package Classifier;

import Classifier.bean.FeatureVector;
import Classifier.bean.MultiSet;

import java.util.Map;
import java.util.*;

/**
 * Created by Arne on 14.01.14.
 */
public class StatisticalTest {
	public static void main(String[] args) throws Exception {
		if ((args.length < 2) || (args.length < 4 && !args[0].equals("-chisquareALL"))) {
			System.out.println("Wrong arguments: -chisquare modelFileName featureA featureB");
			System.out.println("                 -chisquareALL modelFileName");
		} else {
			Map<String, Double> results = new HashMap<String, Double>();
			Corpus dummyCorpus = new Corpus();
			Model model = new Model(dummyCorpus.getFeatureExtractor());
			System.out.println("read model from file " + args[1] + "...");
			model.readModelFromFile(args[1]);
			if (args[0].equals("-chisquare")) {
				System.out.println("start calculation... " + args[2] + " x " + args[3]);
				double p = calculateChiSquare(model, args[2], args[3], FeatureVector.getSplitChar());
				System.out.println("p-value: " + p);
			} else if (args[0].equals("-chisquareALL")) {
				for (Map.Entry<String, MultiSet<String>> feature : model.getFeatureValueFrequency().entrySet()) {
					if (!feature.getKey().contains(FeatureVector.getRoleTypeIdentifier()) && !feature.getKey().equals("all")) {
						System.out.print("start calculation... " + FeatureVector.getRoleTypeIdentifier() + " x " + feature.getKey());
						double p = calculateChiSquare(model, FeatureVector.getRoleTypeIdentifier(), feature.getKey(), FeatureVector.getSplitChar());
						results.put(feature.getKey(), p);
						System.out.println("\tp-value: " + p);
					}
				}
				results = sortByValue(results);
				System.out.println("sorted:");
				for(Map.Entry<String, Double> entry : results.entrySet()) {
					System.out.println(entry.getValue()+"\t"+entry.getKey());
				}
			} else {
				System.out.println(args[0] + ": kind of test unknown");
			}
		}
	}

	public static double calculateChiSquare(Model model, String featureA, String featureB, String splitChar) {
		if (model.getFeatureValueFrequency().get(featureA + splitChar + featureB) == null) {
			System.out.println("feature key \"" + featureA + splitChar + featureB + "\" not found in model. wrong order of feature keys?");
			return 0.0;
		} else {
			double all = model.getFeatureValueFrequency().get("all").get("all");
			double p = 0;
			double expected;
			double real;
			for (Map.Entry<String, Integer> fA : model.getFeatureValueFrequency().get(featureA).getMap().entrySet()) {
				for (Map.Entry<String, Integer> fB : model.getFeatureValueFrequency().get(featureB).getMap().entrySet()) {
					expected = (fA.getValue() / all) * fB.getValue();
					real = model.getFeatureValueFrequency().get(featureA + splitChar + featureB).get(fA.getKey() + splitChar + fB.getKey());
					if (real < 0)
						System.out.println("ERROR real: " + real);
					if (fA.getValue() < 0)
						System.out.println("ERROR fA: " + fA.getValue());
					if (fB.getValue() < 0)
						System.out.println("ERROR fB: " + fB.getValue());
					if (expected < 0)
						System.out.println("ERROR fA: " + fA.getValue() + "; fB: " + fB.getValue() + " => " + (fA.getValue() * fB.getValue()) + " => " + expected);
					p += (real - expected) * (real - expected) / expected;
				}
			}
			return p;
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V>
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
		} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

}
