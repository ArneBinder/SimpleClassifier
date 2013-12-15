package Classifier.bean;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 11.12.13.
 */
public class Node {
	private String id = "";
	private String category = "";
	private String headIDref = null;
	//idref > label
	private Map<String, String> edges = new HashMap<String, String>();
	private List<String> parentIDrefs = new ArrayList<String>(1);
	private int firstWordPos;
	private int lastWordPos;
	private String[] pathFromRoot;
	private Map<String, String> attributes = new HashMap<String, String>();

	public Node(String id, String category) {
		this.setId(id);
		this.setCategory(category);
	}

	public String getHeadIDref() {
		return headIDref;
	}

	public void setHeadIDref(String headIDref) {
		this.headIDref = headIDref;
	}

	public Node(String id) {
		this.setId(id);
	}

	public boolean isTerminal(){
		return edges.isEmpty();
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> elements) {
		this.attributes = elements;
		category = elements.get("pos");
	}

	public String[] getPathFromRoot() {
		return pathFromRoot;
	}

	public void setPathFromRoot(String[] pathFromRoot) {
		this.pathFromRoot = new String[pathFromRoot.length];
		for (int i = 0; i < pathFromRoot.length; i++) {
			this.pathFromRoot[i] = pathFromRoot[i];
		}
	}

	public int getLastWordPos() {
		return lastWordPos;
	}

	public void setLastWordPos(int lastWordPos) {
		this.lastWordPos = lastWordPos;
	}

	public int getFirstWordPos() {
		return firstWordPos;
	}

	public void setFirstWordPos(int firstWordPos) {
		this.firstWordPos = firstWordPos;
	}

	public String getParentIDref(int index) {
		return parentIDrefs.get(index);
	}

	public void addParentIDref(String parentIDref) {
		this.parentIDrefs.add(parentIDref);
	}

	public void addEdge(String idref, String label) {
		edges.put(idref, label);
	}

	@Override
	public String toString() {
		String result;
		//Terminal?
		if (isTerminal()) {
			result = "\n\t\t\t\t\t<t";
			for (Map.Entry<String, String> attribute : attributes.entrySet()) {
				result += " " + attribute.getKey() + "=\"" + StringEscapeUtils.escapeXml(attribute.getValue()) + "\"";
			}
			result += "/>";
			return result;
		} else {
			result = "\n\t\t\t\t\t<nt id=\"" + id + "\"" + " cat=\"" + StringEscapeUtils.escapeXml(category) + "\">";
			for (Map.Entry<String, String> edge : edges.entrySet()) {
				result += "\n\t\t\t\t\t\t<edge label=\"" + StringEscapeUtils.escapeXml(edge.getValue()) + "\" idref=\"" + edge.getKey() + "\"/>";
			}
			result += "\n\t\t\t\t\t</nt>";
			return result;
			//return "\n\t\t NonTerminal [id=" + id + ", category=" + category + ", edges="+ edges + "]";
		}
	}

	// get / set
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Map<String, String> getEdges() {
		return edges;
	}

	public void setEdges(Map<String, String> edges) {
		this.edges = edges;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

