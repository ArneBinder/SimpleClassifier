package Classifier.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	public void add(K o, int value){
		if(map.containsKey(o)){
			map.put(o,map.get(o)+value);
		}else{
			map.put(o,value);
		}
	}

    public Set<Map.Entry<K, Integer>> entrySet(){
        return map.entrySet();
    }

	public int get(K key){
		if(map.containsKey(key))
			return map.get(key);
		else
			return 0;
	}

	public Set<K> getSet(){
		return map.keySet();
	}
}
