package Classifier.bean;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Frame {

	private String id = "";
	private String name = "";

	private String targetLemma = "";
	private List<String> targetIDs = new ArrayList<String>();

	// frameElement name > [idrefs]
	private Map<String, List<String>> frameElements = new HashMap<String, List<String>>();

	public Frame(String id, String name) {
		this.setId(id);
		this.setName(name);
	}

	@Override
	public String toString() {
		String result = "\n\t\t\t\t\t<frame name=\"" + StringEscapeUtils.escapeXml(name) + "\" id=\"" + id + "\">";
		result += "\n\t\t\t\t\t\t<target lemma=\"" + StringEscapeUtils.escapeXml(targetLemma) + "\">";
		for (String targetID : targetIDs) {
			result += "\n\t\t\t\t\t\t\t<fenode idref=\"" + targetID + "\"/>";
		}
		result += "\n\t\t\t\t\t\t</target>";

		for(Map.Entry<String,List<String>> frameElement: frameElements.entrySet()){
			result += "\n\t\t\t\t\t\t<fe name=\""+StringEscapeUtils.escapeXml(frameElement.getKey())+"\">";
			for(String idref: frameElement.getValue()){
				result += "\n\t\t\t\t\t\t\t<fenode idref=\""+idref+"\"/>";
			}
			result += "\n\t\t\t\t\t\t</fe>";
		}

		result += "\n\t\t\t\t\t</frame>";
		return result;
		//return "\n\t\t Frame [id=" + id + ", name=" + name + ", targetLemma="
		//		+ targetLemma + ", targetIDs=" + targetIDs + ", frameElements="
		//		+ frameElements + "]";
	}


	public Set<String> getFrameElementNames() {
		return frameElements.keySet();
	}

	public void setTarget(String lemma) {
		this.setTargetLemma(lemma);
	}

	public void addTargetID(String idref) {
		targetIDs.add(idref);
	}

	public void addFrameElement(String frameElementName) {
		frameElements.put(frameElementName, new ArrayList<String>());
	}

	public void addFrameElementWithIDRef(String frameElementName, String idref) {
		List<String> list = getFrameElements().get(frameElementName);
		if (list == null) {
			list = new ArrayList<String>();
		}
		list.add(idref);
		frameElements.put(frameElementName, list);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetLemma() {
		return targetLemma;
	}

	public void setTargetLemma(String targetLemma) {
		this.targetLemma = targetLemma;
	}

	public List<String> getTargetIDs() {
		return targetIDs;
	}

	public void setTargetIDs(List<String> targetIDs) {
		this.targetIDs = targetIDs;
	}

	public void setFrameElements(Map<String, List<String>> frameElements) {
		this.frameElements = frameElements;
	}

	public Map<String, List<String>> getFrameElements() {
		return frameElements;
	}
}