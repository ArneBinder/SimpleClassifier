package Classifier;

import java.util.*;

import Classifier.bean.FeatureVector;
import Classifier.bean.Node;
import Classifier.bean.Sentence;

public class FeatureExtractor {

    private static class HeadRules {
        //smaler value --> higher priority!!!
        int priority = 0;
        Map<String, Map<String,Map<String, Integer>>> headRules = new HashMap<String, Map<String, Map<String, Integer>>>();

        public void addRule(List<String> categories, String edgeLabel, String childCategory) {
            addRule(categories, Arrays.asList(new String[]{edgeLabel}), Arrays.asList(new String[]{childCategory}));
        }

        public void addRule(String categories, String edgeLabel, List<String> childCategories) {
            addRule(Arrays.asList(new String[]{categories}), Arrays.asList(new String[]{edgeLabel}), childCategories);
        }

        public void addRule(String category, List<String> edgeLabels, List<String> childCategories) {
            addRule(Arrays.asList(new String[]{category}), edgeLabels, childCategories);
        }

        public void addRule(String category, String edgeLabel, String childCategory) {
            if (!headRules.containsKey(category)) {
                headRules.put(category, new HashMap<String, Map<String, Integer>>());
            }
            if (!headRules.get(category).containsKey(edgeLabel)) {
                headRules.get(category).put(edgeLabel, new HashMap<String, Integer>());
            }
            headRules.get(category).get(edgeLabel).put(childCategory, priority++);

        }

        public void addRule(String category, String edgeLabel) {
            addRule(category,  edgeLabel, "");
        }

        public void addRule(List<String> categories, List<String> edgeLabels, List<String> childCategories) {
            for (String category : categories) {
                for (String edgeLabel : edgeLabels) {
                    if (childCategories.isEmpty()) {
                        addRule(category, edgeLabel);
                    } else {
                        for (String childCategory : childCategories) {
                            addRule(category, edgeLabel, childCategory);
                        }
                    }
                }
            }
        }

        public int getHeadRulePriority(String category, String edgeLabel, String childCategory) {
            if(!headRules.containsKey(category) || !headRules.get(category).containsKey(edgeLabel))
                return Integer.MAX_VALUE;
            if(headRules.get(category).get(edgeLabel).containsKey(""))
                return headRules.get(category).get(edgeLabel).get("");
            if(headRules.get(category).get(edgeLabel).containsKey(childCategory))
                return headRules.get(category).get(edgeLabel).get(childCategory);
           return Integer.MAX_VALUE;
        }
    }

    private Sentence sentence = null;
    //public String dummy = "";
    //private static Map<String, List<String>> headPosTags;
    //private static Map<String, List<String>> headEdges;
    private static String[] phrasalCategories = {"AA", "AP", "AVP", "CAC", "CAVP", "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA", "NM", "NP", "PP", "QL", "S", "VP", "VZ"};
    private static HeadRules headRules;

    private static String[] addToPhrasalCat(String cat) {
        String[] temp = new String[phrasalCategories.length + 1];
        System.arraycopy(phrasalCategories, 0, temp, 0, phrasalCategories.length);
        temp[temp.length - 1] = cat;
        return temp;
    }

    static {
        headRules = new HeadRules();
        headRules.addRule("S", Arrays.asList("MO", "OC", "SB"), new ArrayList<String>());
        headRules.addRule("NP", Arrays.asList("NK"), Arrays.asList("NE", "NN", "PDS", "PIS", "PPOSS", "PRELS", "PWS", "NP", "PN", "CS", "PPER", "CVP", "CNP", "NM", "CARD", "TRUNC", "PRF"));
        headRules.addRule("NP", Arrays.asList("NK"), Arrays.asList("ADJA", "AP")); //TODO: Preferenzliste wäre gut... (speziell für ADJA: sollte nach Noun-tags gewählt werden)
        headRules.addRule("NP", Arrays.asList("MO"), Arrays.asList("NP"));
        headRules.addRule("PN", Arrays.asList("PNC"), Arrays.asList("CNP"));
        headRules.addRule("PP", Arrays.asList("MO", "AC"), Arrays.asList("APPR", "APPRART", "APPO","FM"));
        headRules.addRule("PP", Arrays.asList("NK"), Arrays.asList("ADJA"));
        headRules.addRule("PN", Arrays.asList("PNC"), Arrays.asList("NP", "NN", "NE"));
        headRules.addRule("AVP", Arrays.asList("RE", "AVC"), Arrays.asList(addToPhrasalCat("ADV")));
        headRules.addRule("AVP", Arrays.asList("MO"), Arrays.asList("AVP"));
        headRules.addRule("DL", Arrays.asList("DH"), new ArrayList<String>());
        headRules.addRule("NM", Arrays.asList("NMC"), Arrays.asList("CARD", "NN"));
        headRules.addRule(Arrays.asList("AP", "PP", "NP"), Arrays.asList("RE"),  new ArrayList<String>());
        //headRules.addRule(Arrays.asList("CAC", "CAP", "CAVP","CCP","CNP","CO","CPP","CS","CVP","CVZ"), Arrays.asList("CD"), Arrays.asList(addToPhrasalCat("KON")));
        headRules.addRule("CAP", Arrays.asList("CJ"), Arrays.asList("CPP", "PP","CARD", "ADJA", "ADJD", "AP"));
        headRules.addRule("CAVP", Arrays.asList("CJ"), Arrays.asList("AVP", "ADV"));
        headRules.addRule("CCP", Arrays.asList("CJ"), Arrays.asList("CP", "KOUS"));
        headRules.addRule("CNP", Arrays.asList("CJ"), Arrays.asList("NP", "PN", "NN", "NE", "CARD"));
        headRules.addRule("CPP", Arrays.asList("CJ"), Arrays.asList("CPP", "PP"));
        headRules.addRule("CS", Arrays.asList("CJ"), Arrays.asList("S"));
        headRules.addRule("CVP", Arrays.asList("CJ"), Arrays.asList("VP"));
        headRules.addRule("CVZ", Arrays.asList("CJ"), Arrays.asList("VZ"));
        headRules.addRule("CO", Arrays.asList("CJ"), new ArrayList<String>());

        headRules.addRule("NP", Arrays.asList("CM"), Arrays.asList("KOKOM"));
        headRules.addRule("VZ", Arrays.asList("PM"), Arrays.asList("PTKZU"));
        headRules.addRule("CH", Arrays.asList(""), new ArrayList<String>());



    }

    public static final List<String> usedFeatures = Arrays.asList("target", "synCat", "position", "path", "path" + FeatureVector.getSplitChar() + "synCat", "head");

    public List<String> backOffFeature(String concatenatedFeature) {
        List<String> result = new LinkedList<String>();

        // starting rule
        if (concatenatedFeature.equals("")) {
            result.add("target");
            result.add("position");
            result.add("path" + FeatureVector.getSplitChar() + "synCat");
            result.add("head");
        }

        if (concatenatedFeature.equals("path" + FeatureVector.getSplitChar() + "synCat")) {
            result.add("path");
            result.add("synCat");
        }

        // TODO complete rule set @ P17 @ Jurafsky

        return result;
    }

    public List<String> getUsedFeatures() {
        return usedFeatures;
    }

    public void setSentence(Sentence s) throws Exception {
        this.sentence = s;
        //dummy = "";
        enrichInformation();
    }

    public FeatureVector extract(String idref)
            throws Exception {
        FeatureVector fv = new FeatureVector();

        fv.addFeature("target", extractTarget());
        fv.addFeature("synCat", extractSyntacticalCategory(idref));
        fv.addFeature("position", extractPosition(idref));
        fv.addFeature("path", extractPath(idref));
        fv.addFeature("head", extractHead(idref));

        return fv;
    }

    private String extractHead(String idref) throws Exception {
        String headIDref = sentence.getNode(idref).getHeadIDref();
        if (headIDref != null)
            return sentence.getNode(headIDref).getAttributes().get("lemma");
        return "null";
    }


    private String extractPath(String idref)
            throws Exception {

        String path = "";

        //TODO: use only first target??


        if (sentence.getTarget().getId().equals(idref)) {
            path = "TARGET";
        } else {
            List<String> idRefs = new ArrayList<String>(2);
            idRefs.add(idref);
            idRefs.add(sentence.getTarget().getId());
            int[] indices = sentence.calculateRootOfSubtree(idRefs);
            String[] ownIdPath = sentence.getNode(idref).getPathFromRoot(indices[1]);
            String[] targetIdPath = sentence.getTarget().getPathFromRoot(indices[2]);
            //TODO: check if correct...

            int i = indices[0];//0;
            //while (i < targetIdPath.length && i < ownIdPath.length && targetIdPath[i].equals(ownIdPath[i])) {
            //    i++;
            //}

            for (int j = ownIdPath.length - 1; j >= i; j--) {

                path += sentence.getNode(ownIdPath[j]).getCategory() + "+";
            }

            // nimm die wurzel des subtrees nur mit rein, wenn idref nicht auf (global) root zeigt. sonst fuege Kategorie manuell ein...
            if (i == 0)
                path += "VROOT";
            else {
                path += sentence.getNode(targetIdPath[i - 1]).getCategory();
            }


            for (int j = i; j < targetIdPath.length; j++) {
                path += "-" + sentence.getNode(targetIdPath[j]).getCategory();
            }

        }

        //System.out.println(sentence.getTargets().get(0).getId() + ", " + idref + ": " + path);
        return path;
    }


    private String extractPosition(String idref)
            throws Exception {

        String position = "";

		/*
         * Possible cases:
		 * 0 - idref in front of the separated target 1 - idref
		 * inbetween of the separated target 2 - idref in front of a single
		 * target 
		 * 3 - idref behind the single / separated target
		 */
        int targetFirstPos = sentence.getTarget().getFirstWordPos();
        int targetLastPos = sentence.getTarget().getLastWordPos();
        int ownPos = getPosFromID(sentence.getNode(idref).getHeadIDref());


        if (targetFirstPos != targetLastPos && ownPos <= targetFirstPos)
            return "0";
        if (targetFirstPos != targetLastPos && ownPos <= targetLastPos)
            return "1";
        if (targetFirstPos == targetLastPos && ownPos <= targetLastPos)
            return "2";
        if (ownPos >= targetLastPos)
            return "3";

        if (position.isEmpty()) {
            throw new Exception("The position could not be determined!");
        }
        return "";

    }


    /**
     * For a terminal: Pos-tag For a non-terminal: ConstituentenName
     *
     * @param idref
     * @return
     * @throws Exception
     */
    private String extractSyntacticalCategory(String idref) throws Exception {
        return sentence.getNode(idref).getCategory();
    }

    private String extractTarget() throws Exception {

        String targetLemma = "";
        targetLemma = sentence.getNode(sentence.getTarget().getHeadIDref()).getAttributes().get("lemma");

        return targetLemma;
    }

    // sets following values of all Nodes:
    //  - parent
    //  - pathFromRoot
    //  - firstWordPos
    //  - lastWordPos
    //  - headIDref
    private Map.Entry<Integer, Integer> traverseTree(String curIDRef, ArrayList<String> pathFromRoot) throws Exception {
        String parentIdRef = "";
        Node curNode = sentence.getNode(curIDRef);
        if (curNode.getHeadIDref() == null) {
            Node newHead = calculateHeadWord(Arrays.asList(curIDRef));
            if (newHead != null)
                curNode.setHeadIDref(newHead.getId());
        }
        if (pathFromRoot.size() > 0)
            parentIdRef = pathFromRoot.get(pathFromRoot.size() - 1);
        curNode.addPathFromRoot(pathFromRoot);
        if (pathFromRoot.size() > 0)
            curNode.addParentIDref(parentIdRef);

        Map.Entry<Integer, Integer> lastFirstIDref;

        if (curNode.isTerminal()) {
            //pathFromRoot.add(curIDRef);
            lastFirstIDref = new AbstractMap.SimpleEntry<Integer, Integer>(getPosFromID(curIDRef), getPosFromID(curIDRef));
        } else {

            int firstPos = Integer.MAX_VALUE;
            int lastPos = 0;
            pathFromRoot.add(curIDRef);
            for (String childIdRef : curNode.getEdges().keySet()) {
                lastFirstIDref = traverseTree(childIdRef, (ArrayList<String>) pathFromRoot.clone());
                if (lastFirstIDref.getKey() < firstPos)
                    firstPos = lastFirstIDref.getKey();
                if (lastFirstIDref.getValue() > lastPos)
                    lastPos = lastFirstIDref.getValue();
            }

            curNode.setFirstWordPos(firstPos);
            curNode.setLastWordPos(lastPos);

            lastFirstIDref = new AbstractMap.SimpleEntry<Integer, Integer>(firstPos, lastPos);
        }
        return lastFirstIDref;
    }

    private int getPosFromID(String idRef) {
        String[] temp = new String[0];
        try {
            temp = idRef.split("_");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return Integer.parseInt(temp[temp.length - 1]);
    }

    public Node calculateHeadWord(List<String> idrefs) throws Exception {
        Collections.sort(idrefs);
        //if(idrefs.contains("s21270_1"))
        //  System.out.println("test");
        if (idrefs.get(0).equals(sentence.getRootIDref()) && !sentence.getNode(sentence.getRootIDref()).isTerminal() && sentence.getNode(sentence.getRootIDref()).getCategory().equals("VROOT"))
            return null;

//        if(idrefs.contains("s5142_509")){
//            System.out.println("TEST");
//        }
        Node curNode;
        List<String> newIdRefs = new ArrayList<String>(40);
        for (String idref : idrefs) {
            curNode = sentence.getNode(idref);
            if (curNode.isTerminal()) {
                curNode.setHeadIDref(curNode.getId());
                return curNode;
            } else {
                if (curNode.getEdges().values().contains("HD")) {
                    //find idref for HD-edge
                    for (Map.Entry<String, String> edge : curNode.getEdges().entrySet()) {
                        if (edge.getValue().equals("HD")) {
                            Node curHead = calculateHeadWord(Arrays.asList(edge.getKey()));
                            if (curHead != null) {
                                curNode.setHeadIDref(curHead.getId());
                                return curHead;
                            } else {
                                throw new Exception("HD-edge (idref: " + edge.getKey() + ") contains no head");
                            }
                        }
                    }
                } else {
                    String curCat = curNode.getCategory();
                    String directHead = chooseHeadChild(curCat,curNode.getEdges());
                    if(directHead!=null){
                        newIdRefs.add(directHead);
                    //}
//                    if (headRules.headRuleIsDefined(curCat)){//headEdges.containsKey(curCat) && headPosTags.containsKey(curCat)) {
//                        //List<String> curHeadEdges = headEdges.get(curCat);
//                        //List<String> curHeadPosTags = headPosTags.get(curCat);
//
//                        for (Map.Entry<String, String> edge : curNode.getEdges().entrySet()) {
//                            String curEdge = edge.getValue();
//                            String curChildCat = sentence.getNode(edge.getKey()).getCategory();
//                            //if(curHeadEdges.contains(curEdge))
//                            //    System.out.println("TEST2");
//                            //if(curHeadPosTags.contains(curChildCat))
//                            //    System.out.println("TEST3");
//                            //if (curHeadEdges.contains(curEdge) && (curHeadPosTags.contains(curChildCat) || (curHeadPosTags.size() == 0))) {
//                            if(headRules.allowedHeadEdge(curCat,curEdge,curChildCat)){
//                                newIdRefs.add(edge.getKey());
//                            }
//                        }
                    } else {
                        throw new Exception("no head-edge or no head-pos-tag for category " + curCat + " (for idref: " + curNode.getId() + ")");
                    }
                }
            }
        }
        if (newIdRefs.isEmpty()) {
            if (!idrefs.contains(sentence.getRootIDref())) {
                //System.out.println(sentence);
                /*for(String idref: idrefs){
                    for (Map.Entry<String, String> edge : sentence.getNode(idref).getEdges().entrySet()) {
                        String curEdge = edge.getValue();
                        String curChildCat = sentence.getNode(edge.getKey()).getCategory();
                        dummy+="\n"+sentence.getNode(idref).getCategory()+"\t"+curEdge+"\t"+curChildCat+"\t"+edge.getKey();
                    }
                }*/
                throw new Exception("no head found for idrefs: " + idrefs + "\n"+sentence.getNode(idrefs.get(0)));
            }
            // TODO: Throw
            //throw new Exception("no head word found for: "+idrefs);
            return null;
        }
        return calculateHeadWord(newIdRefs);
    }

    private String chooseHeadChild(String parentCategory, Map<String, String> edges) throws Exception{
        //smaller is better!
        int bestEdgeQuality = Integer.MAX_VALUE;
        String bestChildRef = null;
        int curEdgeQuality;
        for (Map.Entry<String, String> edge : edges.entrySet()){
            curEdgeQuality = headRules.getHeadRulePriority(parentCategory, edge.getValue(), sentence.getNode(edge.getKey()).getCategory());
            if(bestEdgeQuality > curEdgeQuality){
                 bestEdgeQuality = curEdgeQuality;
                bestChildRef = edge.getKey();
            }
        }
        return bestChildRef;
    }

    public void enrichInformation() throws Exception {
        traverseTree(sentence.getRootIDref(), new ArrayList<String>(30));


    }

}
