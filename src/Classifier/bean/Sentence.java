package Classifier.bean;

import java.util.*;

public class Sentence {
    private String id;
    private String rootIDref = null;


    // ID of t > t object
    private Map<String, Node> terminals = new HashMap<String, Node>();

    // ID of nt > nt object
    private Map<String, Node> nonterminals = new HashMap<String, Node>();

    //
    private List<Node> targets = new ArrayList<Node>(3);

    // list of frames
    private List<Frame> frames = new ArrayList<Frame>();

    public Sentence(String id) {
        this.setId(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        return ((Sentence) obj).getId().equals(getId());
    }

    public String getRootIDref() {
        return rootIDref;
    }

    public boolean containsNode(String idRef){
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
        else throw new Exception("idRef: " + idRef + " not in Sentence: "+getId());
    }

    public List<Node> getTargets() {
        return targets;
    }

    //TODO: report to the others: targets can be nonTerminals too!!! --> solution (already implemented): take head  (kriechen.xml)
    public void setTargets(List<String> targetIdRefs) throws Exception {
        //if(getId().equals("s38580"))
          //  System.out.println("test");
        if (targetIdRefs.size() == 0) {
            //System.out.println("test");
            throw new Exception(
                    "No targets for sentenceID: " + getId() + " to add.");
        }
        if (targetIdRefs.size() > 2) {
            Set<String> parentIDrefs = new HashSet<String>();
            for (String targetIDref : targetIdRefs) {
                parentIDrefs.add(getNode(targetIDref).getParentIDref(0));

            }
            //TODO: fix this part to flatten the tree of targets: if deleted in wrong order, it become decomposed instead of collapsing to the root (of target-subtree) parent constituent (example: Bank_s38580_f1 here it works, but not in general)
            for (Iterator<String> iter = parentIDrefs.iterator(); iter.hasNext();){
                String parentIDref = iter.next();
                if(parentIDrefs.contains(getNode(parentIDref).getParentIDref(0)))
                    iter.remove();
            }
            if (parentIDrefs.size() <= 2) {
                targets = new ArrayList<Node>(3);
                for (String parentIdRef : parentIDrefs) {
                    targets.add(getNode(getNode(parentIdRef).getHeadIDref()));
                }
            } else {
                throw new Exception(
                        "Wir k�nnen nicht mit mehr als 2 TargetIDRefs umgehen. Sorry! sentenceID: " + getId()+" "+targetIdRefs);
            }

        } else {

            targets = new ArrayList<Node>(3);
            for (String targetIdRef : targetIdRefs) {
                targets.add(getNode(targetIdRef));
            }
        }
    }

    public void addTerminal(Node t) {
        terminals.put(t.getId(), t);
    }

    public void addNonterminal(Node nt) {
        nonterminals.put(nt.getId(), nt);
    }

    public void addFrame(Frame frame) {
        for(List<String> fes: frame.getFrameElements().values()){
            for(String feID: fes){
                if(!containsNode(feID))
                    return;
            }
        }
        for(String targetID: frame.getTargetIDs()){
            if(!containsNode(targetID))
                return;
        }

        getFrames().add(frame);
    }

    public void setRootID(String rootID) {
        this.rootIDref = rootID;
    }


    // sets following values of all Nodes:
    //  - parent
    //  - pathFromRoot
    //  - firstWordPos
    //  - lastWordPos
    //  - headIDref


    public List<List<String>> extractTargetIDRefs(Set<String> targetLemmata) {
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

    public List<String> extractTargetIDRefs(String targetLemma) {
        List<String> targetIDRefs = null;

        for (Node terminal : terminals.values()) {

            // FIXME: Concatenate terminal lemmas trying to create the targetLemma
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