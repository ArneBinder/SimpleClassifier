package Classifier.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arne on 09.12.13.
 */
public class MultiSet<K> {
	private Map<K, Integer> map = new HashMap<K, Integer>();

	public Map<K, Integer> getMap(){
		return map;
	}

	public void add(K o){
		if(map.containsKey(o)){
			map.put(o,map.get(o)+1);
		}else{
			map.put(o,1);
		}
	}

	public int get(K key){
		if(map.containsKey(key))
			return map.get(key);
		else
			return 0;
	}
}
