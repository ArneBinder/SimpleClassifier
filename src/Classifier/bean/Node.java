package Classifier.bean;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;

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
	private List<String[]> pathsFromRoot;
	private Map<String, String> attributes = new HashMap<String, String>();

	public Node(String id, String category) {
		this.setId(id);
		this.setCategory(category);
        this.pathsFromRoot = new LinkedList<String[]>();
	}


	public String getHeadIDref() {
		return headIDref;
	}

	public void setHeadIDref(String headIDref) {
		this.headIDref = headIDref;
	}

	public Node(String id) {
        this.pathsFromRoot = new LinkedList<String[]>();
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

	public List<String[]> getPathsFromRoot() {
		return pathsFromRoot;
	}
    public String[] getPathFromRoot(int index) {
        return pathsFromRoot.get(index);
    }

	public void addPathFromRoot(List<String> pathFromRoot) throws Exception{
		String[] newPathFromRoot = new String[pathFromRoot.size()];
        int i=0;
		for (String idRef: pathFromRoot) {
            newPathFromRoot[i] = idRef;
            i++;
		}
        try{
        pathsFromRoot.add(newPathFromRoot);
        }catch (Exception e){
            System.out.println("TEST");
            throw e;
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

