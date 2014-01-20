package Classifier.bean;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;

public class Frame {

	private String id = "";
	private String name = "";
	private double probability = Double.NEGATIVE_INFINITY;

	private String targetLemma = "";
	private List<String> targetIDs = new ArrayList<String>();

	// frameElement name > [idrefs]
	//private Map<String, List<String>> frameElements = new HashMap<String, List<String>>();
	private Map<String, FrameElement> frameElements = new HashMap<String, FrameElement>();

	public Frame(String id, String name) {
		this.setId(id);
		this.setName(name);
	}

	/*public Frame(Frame frame){
		this.setId(frame.getId());
		this.setName(frame.getName());
		this.setTargetLemma(frame.getTargetLemma());
		this.targetIDs = new ArrayList<String>(frame.getTargetIDs().size());
		for(String targetID: frame.getTargetIDs()){
			this.targetIDs.add(targetID);
		}
		this.frameElements = new HashMap<String, FrameElement>(frame.getFrameElements().size());
		Cloner
		for(FrameElement fe: frame.getFrameElements()){
			this.frameElements.put(fe.getName(), new FrameElement());
		}
	} */

	public void setProbability(double probability) {
		this.probability = probability;
	}

	@Override
	public String toString() {
		String probabilityS = (probability != Double.NEGATIVE_INFINITY) ? "probability=\"" + probability + "\"" : "";
		String result = "\n\t\t\t\t\t<frame name=\"" + StringEscapeUtils.escapeXml(name) + "\" id=\"" + id + "\" " + probabilityS + ">";
		result += "\n\t\t\t\t\t\t<target lemma=\"" + StringEscapeUtils.escapeXml(targetLemma) + "\">";
		for (String targetID : targetIDs) {
			result += "\n\t\t\t\t\t\t\t<fenode idref=\"" + targetID + "\"/>";
		}
		result += "\n\t\t\t\t\t\t</target>";

		for(FrameElement frameElement: frameElements.values()){
			result += frameElement;
		}

		result += "\n\t\t\t\t\t</frame>";
		return result;
		//return "\n\t\t Frame [id=" + id + ", name=" + name + ", targetLemma="
		//		+ targetLemma + ", targetIDs=" + targetIDs + ", frameElements="
		//		+ frameElements + "]";
	}


	//public Set<String> getFrameElementNames() {
	//	return frameElements.keySet();
	//}

	//public void setTarget(String lemma) {
	//	this.setTargetLemma(lemma);
	//}

	public void addTargetID(String idref) {
		targetIDs.add(idref);
	}

	public void addFrameElement(FrameElement frameElement){
		frameElements.put(frameElement.getName(),frameElement);
	}

	public void addNewFrameElement(String frameElementName) {
		frameElements.put(frameElementName, new FrameElement(frameElementName));
	}

	public void addFrameElementWithIDRef(String frameElementName, String idref) {
		//TODO: check
		FrameElement frameElement = frameElements.get(frameElementName);
		if(frameElement==null){
			frameElement = new FrameElement(frameElementName);
		}
		frameElement.addIdRef(idref);
		frameElements.put(frameElementName, frameElement);

		//List<String> list = getFrameElements().get(frameElementName);
		//if (list == null) {
		//	list = new ArrayList<String>();
		//}
		//list.add(idref);
		//frameElements.put(frameElementName, list);
	}

	public boolean filter(List<String> allowedFrameElements){
		boolean result = false;

		Iterator<String> iter = frameElements.keySet().iterator();
		while (iter.hasNext()) {
			if(allowedFrameElements.contains(iter.next())){
				result = true;
			}else{
				iter.remove();
			}
		}
		return result;
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

	//public void setFrameElements(Map<String, List<String>> frameElements) {
	//	this.frameElements = frameElements;
	//}

	public Collection<FrameElement> getFrameElements() {
		return frameElements.values();
	}
	public FrameElement getFrameElement(String frameElementName){
		return frameElements.get(frameElementName);
	}

	//public List<String> getFrameElementIDrefs(String frameElement){ return frameElements.get(frameElement);	}
}