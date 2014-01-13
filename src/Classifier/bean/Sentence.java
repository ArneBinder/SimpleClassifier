package Classifier.bean;

import java.util.*;

public class Sentence {
    private String id;
    private String rootIDref = null;

    // ID of t > t object
    private Map<String, Node> terminals = new HashMap<String, Node>();
    // ID of nt > nt object
    private Map<String, Node> nonterminals = new HashMap<String, Node>();
    //private List<Node> targets = new ArrayList<Node>(3);
    private Node target;
    // list of frames
    private List<Frame> frames = new ArrayList<Frame>();

    
    
    public Sentence(String id) {
        this.setId(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return ((Sentence) obj).getId().equals(getId());
    }

    public String getRootIDref() {
        return rootIDref;
    }

    public boolean containsNode(String idRef) {
        return terminals.containsKey(idRef) || nonterminals.containsKey(idRef);
    }

    public void addSecEdge(String[] secEdge) {
        try {
            getNode(secEdge[0]).addEdge(secEdge[2], secEdge[1]);

        } catch (Exception e) {
            System.out.println("ERROR: could not add second edge from iderf: " + secEdge[0] + " to idref: " + secEdge[2] + " with label: " + secEdge[1]);
        }
    }

    public Node getNode(String idRef) throws Exception {
        if (terminals.containsKey(idRef))
            return terminals.get(idRef);
        else if (nonterminals.containsKey(idRef))
            return nonterminals.get(idRef);
        else throw new Exception("idRef: " + idRef + " not in Sentence: " + getId());
    }

    public Node getTarget() {
        return target;
    }

    // smallest subtree, which contains all idRefs
    // returns: [depth, indexOfPathFromRoot_0, indexOfPathFromRoot_1, indexOfPathFromRoot_2, ... ]
    // -> indexOfPathFromRoot for each idref (ordered!),
    public int[] calculateRootOfSubtree(List<String> idRefs) throws Exception {
        //idref Index > pathsFromRoot Index > pathFromRoot Path
        String[][][] pathsFromRoot = new String[idRefs.size()][][];

        String idRef;
        for (int idRefIndex = 0; idRefIndex < idRefs.size(); idRefIndex++) {
            idRef = idRefs.get(idRefIndex);
            pathsFromRoot[idRefIndex] = new String[getNode(idRef).getPathsFromRoot().size()][];
            int rootPathIndex = 0;
            for (String[] pathFromRoot : getNode(idRef).getPathsFromRoot()) {
                pathsFromRoot[idRefIndex][rootPathIndex] = new String[pathFromRoot.length + 1];
                System.arraycopy(pathFromRoot, 0, pathsFromRoot[idRefIndex][rootPathIndex], 0, pathFromRoot.length);
                //pathsFromRoot[idRefIndex][rootPathIndex] = pathFromRoot;
                pathsFromRoot[idRefIndex][rootPathIndex][pathFromRoot.length] = idRef;
                rootPathIndex++;
            }
        }

        String curIDref;

        boolean abort = false;
        boolean found = true;
        int[] indices = new int[idRefs.size() + 1];
        int[] result = new int[idRefs.size() + 1];
        //depth,indices of pathsFromRoot
        int curDepth = 0;
        while (found && !abort) {
            found = false;
            indices = new int[idRefs.size() + 1];
            indices[0] = curDepth;

            int countPointer = 0;
            while (!found && !abort) {
                if (valueExists(indices, pathsFromRoot)) {
                    curIDref = pathsFromRoot[0][indices[1]][curDepth];
                    found = true;
                    for (int idIndex = 1; found && idIndex < idRefs.size(); idIndex++) {
                        if (!curIDref.equals(pathsFromRoot[idIndex][indices[idIndex + 1]][curDepth])) {
                            found = false;
                            break;
                        }
                    }
                }

                if (!found) {
                    indices[countPointer + 1]++;
                    while (indices[countPointer + 1] == pathsFromRoot[countPointer].length) {
                        indices[countPointer + 1] = 0;
                        countPointer++;

                        if (countPointer == idRefs.size()) {
                            abort = true;
                            break;
                        } else {
                            indices[countPointer + 1]++;
                        }
                    }
                    countPointer = 0;
                }else{
                    System.arraycopy(indices, 0, result, 0, idRefs.size() + 1);
                }
            }

            if (found)
                curDepth++;
        }
        if (indices[0] > 0) {
            //indices[0]--;
        } else {
            throw new Exception("Could not calculate common path for passed idrefs (VROOT not in path)");
        }

        return result;
    }

    private boolean valueExists(int[] values, String[][][] pathsFromRoot) {
        for (int i = 0; i < values.length - 1; i++) {
            if (!(pathsFromRoot[i].length > values[i + 1])) {
                return false;
            } else {
                if (!(pathsFromRoot[i][values[i + 1]].length > values[0])) {
                    return false;
                }
            }
        }
        return true;
    }

    //TODO: report to the others: targets can be nonTerminals too!!! --> solution (already implemented): take head  (kriechen.xml)
    public void setTarget(List<String> targetIdRefs) throws Exception {
        //if(getId().equals("s38580"))
        //  System.out.println("test");
        if (targetIdRefs.size() == 0) {
            //System.out.println("test");
            throw new Exception(
                    "No targets for sentenceID: " + getId() + " to add.");
        }

        if (targetIdRefs.size() > 1) {
            int[] indices = calculateRootOfSubtree(targetIdRefs);
            try {
                target = getNode(getNode(targetIdRefs.get(0)).getPathsFromRoot().get(indices[1])[indices[0]]);
            } catch (ArrayIndexOutOfBoundsException e) { // if index to targetIdRefs.get(0) itself is returned as subTReeRoot (is on the path of the other target)...
                target = getNode(targetIdRefs.get(0));
            }

        } else {
            target = getNode(targetIdRefs.get(0));
        }
    }

    public void setTarget(String targetIdRef) throws Exception {
        target = getNode(targetIdRef);
    }

    public void addTerminal(Node t) {
        terminals.put(t.getId(), t);
    }

    public void addNonterminal(Node nt) {
        nonterminals.put(nt.getId(), nt);
    }

    public void addFrame(Frame frame) {
	if(frame == null){
	    System.err.println("oh no! frame is null");
	}
        String[] idRefParts;
        for (List<String> fes : frame.getFrameElements().values()) {
            for (String feID : fes) {
                idRefParts = feID.split(":");
                if (!containsNode(idRefParts[0]))
                    return;
            }
        }

        for (String targetID : frame.getTargetIDs()) {
            if (!containsNode(targetID))
                return;
        }

        getFrames().add(frame);
    }

    public void setRootID(String rootID) {
        this.rootIDref = rootID;
    }

    public List<List<String>> extractTargetIDRefs(Set<String> targetLemmata) throws Exception {
        List<List<String>> result = new LinkedList<List<String>>();
        List<String> extractTargetIDRefs;

        for (String targetLemma : targetLemmata) {

            extractTargetIDRefs = extractTargetIDRefs(targetLemma);
            if (extractTargetIDRefs != null) {
                result.add(extractTargetIDRefs);
            }

            extractTargetIDRefs = null;
        }

        return result;
    }

    public List<String> extractTargetIDRefs(String targetLemma) throws Exception {
        List<String> targetIDRefs = null;

        for (Node nonterminal : nonterminals.values()) {
            if (!nonterminal.getId().equals(rootIDref)) {
                String headIdRef = nonterminal.getHeadIDref();
                //System.out.println("XXX" + headIdRef + " " + nonterminal);
                if (getNode(headIdRef).getAttributes().get("lemma").equals(targetLemma)) {
                    if (targetIDRefs == null) {
                        targetIDRefs = new LinkedList<String>();
                    }
                    targetIDRefs.add(nonterminal.getId());
                }
            }
        }

        for (Node terminal : terminals.values()) {
            if (terminal.getAttributes().get("lemma").equals(targetLemma)) {
                if (targetIDRefs == null) {
                    targetIDRefs = new LinkedList<String>();
                }
                targetIDRefs.add(terminal.getId());
            }
        }
        return targetIDRefs;
    }


    public void deleteAnnotation() {
        frames = new ArrayList<Frame>();
    }

	public boolean filter(List<String> allowedFrameElements){
		boolean result = false;

		Iterator<Frame> iter = frames.iterator();
		while (iter.hasNext()) {
			if(iter.next().filter(allowedFrameElements)){
				result = true;
			}else{
				iter.remove();
			}
		}
		return result;
	}

    // get / set

    public Map<String, Node> getTerminals() {
        return terminals;
    }

    public void setTerminals(Map<String, Node> terminals) {
        this.terminals = terminals;
    }

    public Map<String, Node> getNonterminals() {
        return nonterminals;
    }

    public void setNonterminals(Map<String, Node> nonterminals) {
        this.nonterminals = nonterminals;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        String result = "\n\t\t<s id=\"" + id + "\">";
        result += "\n\t\t\t<graph root=\"" + rootIDref + "\">";
        result += "\n\t\t\t\t<terminals>";
        for (Node terminal : terminals.values()) {
            result += terminal;
        }
        result += "\n\t\t\t\t</terminals>";
        result += "\n\t\t\t\t<nonterminals>";
        for (Node nonTerminal : nonterminals.values()) {
            result += nonTerminal;
        }
        result += "\n\t\t\t\t</nonterminals>";
        result += "\n\t\t\t</graph>";
        result += "\n\t\t\t<matches>\n\t\t\t</matches>";
        result += "\n\t\t\t<sem>";
        result += "\n\t\t\t\t<globals>\n\t\t\t\t</globals>";
        result += "\n\t\t\t\t<frames>";
        for (Frame frame : frames) {
            result += frame;
        }
        result += "\n\t\t\t\t</frames>";
        result += "\n\t\t\t\t<usp>";
        result += "\n\t\t\t\t\t<uspframes/>";
        result += "\n\t\t\t\t\t<uspfes/>";
        result += "\n\t\t\t\t</usp>";
        result += "\n\t\t\t\t<wordtags>";
        result += "\n\t\t\t\t</wordtags>";
        result += "\n\t\t\t</sem>";
        result += "\n\t\t</s>";
        return result;
        //return "Sentence [id=" + id + ",\n\t terminals=" + terminals
        //		+ ",\n\t nonterminals=" + nonterminals + ",\n\t frames=" + frames + "]";
    }

}