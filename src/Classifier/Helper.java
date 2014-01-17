package Classifier;

import java.util.*;

/**
 * Created by Arne on 17.01.14.
 */
public class Helper {
	public static <K, V extends Comparable<? super V>> Map<K, V>
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}


	public static <K extends Comparable<? super K>, V > Map<K, V>
	sortByKey( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getKey()).compareTo( o2.getKey() );
			}
		} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public static int getPosFromID(String idRef) {
		String[] temp = new String[0];
		try {
			temp = idRef.split("_");
			Integer.parseInt(temp[temp.length - 1]);
		} catch (Exception e) {
			System.out.println(idRef);
		}
		return Integer.parseInt(temp[temp.length - 1]);
	}

	public static int myRandom(int low, int high) {
		return (int) (Math.random() * (high - low) + low);
	}
}
