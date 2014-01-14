package Classifier.bean;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 14.01.14.
 */
public class FrameElement {
	private String name;
	private List<String> idrefs;
	private Map<String, Double> probabilities;

	public FrameElement(String name){
		this.name = name;
		this.idrefs = new ArrayList<String>();
		this.probabilities = new HashMap<String, Double>(0);
	}

	public String getName() {
		return name;
	}

	public List<String> getIdrefs() {
		return idrefs;
	}

	public void addIdRef(String idRef){
		idrefs.add(idRef);
	}

	public void addIdRef(String idRef, double probability){
		idrefs.add(idRef);
		probabilities.put(idRef, probability);
	}

	@Override
	public String toString() {
		String result = "\n\t\t\t\t\t\t<fe name=\""+ StringEscapeUtils.escapeXml(getName())+"\">";
		for(String idref: getIdrefs()){
			result += "\n\t\t\t\t\t\t\t<fenode idref=\""+idref+"\"";
			if(probabilities.containsKey(idref)){
				result += " probability=\""+probabilities.get(idref)+"\"";
			}
			result +="/>";
		}
		result += "\n\t\t\t\t\t\t</fe>";
		return result;
	}

}
