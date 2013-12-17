package Classifier;

import java.util.*;

import Classifier.bean.FeatureVector;
import Classifier.bean.Node;
import Classifier.bean.Sentence;

public class FeatureExtractor {

    public static final List<String> usedFeatures = Arrays.asList("target", "synCat", "position", "path", "target" + FeatureVector.getSplitChar() + "synCat", "head");
    private Sentence sentence = null;
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

    private static class HeadRules {
        Map<String, Map<String, Set<String>>> headRules = new HashMap<String, Map<String, Set<String>>>();

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
            addRule(category, edgeLabel);
            headRules.get(category).get(edgeLabel).add(childCategory);
        }

        public void addRule(String category, String edgeLabel) {
            if (!headRules.containsKey(category)) {
                headRules.put(category, new HashMap<String, Set<String>>());
            }
            if (!headRules.get(category).containsKey(edgeLabel)) {
                headRules.get(category).put(edgeLabel, new HashSet<String>());
            }
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

        public boolean allowedHeadEdge(String category, String edgeLabel, String childCategory) {
            return (headRules.containsKey(category) && headRules.get(category).containsKey(edgeLabel) && (headRules.get(category).get(edgeLabel).isEmpty() || headRules.get(category).get(edgeLabel).contains(childCategory)));
        }

        public boolean headRuleIsDefined(String category){
            return headRules.containsKey(category);
        }
    }

    static {

        headRules = new HeadRules();
        headRules.addRule("S", Arrays.asList("MO", "OC", "SB"), new ArrayList<String>());
        headRules.addRule("NP", Arrays.asList("NK"), Arrays.asList("NE", "NN", "PDS", "PIS", "PPOSS", "PRELS", "PWS", "NP", "PN", "CS", "PPER", "CVP"));
        headRules.addRule("NP", Arrays.asList("RE"), Arrays.asList("CS", "CVP"));
        headRules.addRule("PP", Arrays.asList("MO", "AC"), Arrays.asList("APPR", "APPRART", "APPO"));
        headRules.addRule("PN", Arrays.asList("PNC"), Arrays.asList("NP", "NN", "NE"));
        headRules.addRule("AVP", Arrays.asList("RE", "AVC"), Arrays.asList(addToPhrasalCat("ADV")));
        headRules.addRule("DL", Arrays.asList("DH"), new ArrayList<String>());
        headRules.addRule("NM", Arrays.asList("NMC"), Arrays.asList("CARD", "NN"));
        headRules.addRule(Arrays.asList("AP", "PP", "NP"), Arrays.asList("RE"),  new ArrayList<String>());
        headRules.addRule(Arrays.asList("CAC", "CAP", "CAVP","CCP","CNP","CO","CPP","CS","CVP","CVZ"), Arrays.asList("CD"), Arrays.asList(addToPhrasalCat("KON")));
        headRules.addRule("CS", Arrays.asList("CJ"), Arrays.asList("S"));
        headRules.addRule("VZ", Arrays.asList("PM"), Arrays.asList("PTKZU"));
        headRules.addRule("CH", Arrays.asList(""), new ArrayList<String>());


        // TODO: "contains" is bad.. Create abstracted version for np/vp > change contains to equal (e.g. tree bank hierarchy)
        // TODO: Replace ~ !!!
        //headPosTags = new HashMap<String, List<String>>();
        //headPosTags.put("S", new ArrayList<String>());
        //headPosTags.put("NP", Arrays.asList("NE", "NN", "PDS", "PIS", "PPOSS", "PRELS", "PWS", "NP", "PN", "CS", "PPER", "CVP"));
        //headPosTags.put("PP", Arrays.asList("APPR", "APPRART", "APPO"));
        //headPosTags.put("PN", Arrays.asList("NP", "NN", "NE"));
        //headPosTags.put("AVP", Arrays.asList(addToPhrasalCat("ADV")));
        //headPosTags.put("DL", new ArrayList<String>()); // alle erlaubt!
        //headPosTags.put("CAC", Arrays.asList("KON"));
        //headPosTags.put("CAP", Arrays.asList("KON", "APPR", "ADJA")); //TODO: check if APPR correct
        //headPosTags.put("CAVP", Arrays.asList("KON", "APPR"));
        //headPosTags.put("CCP", Arrays.asList("KON"));
        //headPosTags.put("CNP", Arrays.asList("KON"));
        //headPosTags.put("CO", Arrays.asList("KON"));
        //headPosTags.put("CPP", Arrays.asList("KON"));
        //headPosTags.put("CS", new ArrayList<String>());//TODO: fix workaround (s778_507)// Arrays.asList("KON"));
        //headPosTags.put("CVP", Arrays.asList(addToPhrasalCat("KON"))); //TODO: fix workaround (s6057_512)
        //headPosTags.put("CVZ", Arrays.asList("KON"));
        //headPosTags.put("NM", Arrays.asList("CARD", "NN"));
        //headPosTags.put("VZ", Arrays.asList("PTKZU"));//workaround for incorrect annotated s5240_525
        //headPosTags.put("CH", new ArrayList<String>());
        //headPosTags.put("VP", Arrays.asList("PTKVZ"));
        //headPosTags.put("ISU", Arrays.asList("$."));
        //headPosTags.put("MTA", Arrays.asList("ADJA"));
        //headPosTags.put("AP", Arrays.asList("PP"));

        //headEdges = new HashMap<String, List<String>>();
        //headEdges.put("S", Arrays.asList("MO", "OC", "SB"));
        //headEdges.put("NP", Arrays.asList("NK", "RE"));
        //headEdges.put("PN", Arrays.asList("PNC"));
        //headEdges.put("AVP", Arrays.asList("RE", "AVC")); //workaround: s12479_503
        //headEdges.put("PP", Arrays.asList("MO", "AC"));
        //headEdges.put("DL", Arrays.asList("DH"));
        //headEdges.put("CAC", Arrays.asList("CD"));
        //headEdges.put("CAP", Arrays.asList("CD", "CJ")); //TODO: fix workaround (s45832_501)
        //headEdges.put("CAVP", Arrays.asList("CD"));
        //headEdges.put("CCP", Arrays.asList("CD"));
        //headEdges.put("CNP", Arrays.asList("CD"));
        //headEdges.put("CO", Arrays.asList("CD"));
        //headEdges.put("CPP", Arrays.asList("CD"));
        //headEdges.put("CS", Arrays.asList("CD", "CJ")); //TODO: fix workaround (s778_507)
        //headEdges.put("CVP", Arrays.asList("CD", "CJ")); //TODO: fix workaround (s6057_512)
        //headEdges.put("CVZ", Arrays.asList("CD"));
        //headEdges.put("NM", Arrays.asList("NMC"));
        //headEdges.put("VZ", Arrays.asList("PM")); //workaround for incorrect annotated s5240_525
        //headEdges.put("CH", new ArrayList<String>());
        //headEdges.put("VP", Arrays.asList("SVP"));
        //headEdges.put("ISU", Arrays.asList("UC"));
        //headEdges.put("MTA", Arrays.asList("ADC"));
        //headEdges.put("AP", Arrays.asList("RE"));
    }

    public List<String> backOffFeature(String concatenatedFeature) {
        List<String> result = new LinkedList<String>();

        // starting rule
        if (concatenatedFeature.equals("")) {
            result.add("target" + FeatureVector.getSplitChar() + "synCat");
            result.add("position");
            result.add("path");
            result.add("head");
        }

        if (concatenatedFeature.equals("target" + FeatureVector.getSplitChar()
                + "synCat")) {
            result.add("target");
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

//        int firstTargetNumber = 0;
//        int seconTargetdNumber = 0;
//        int idRefNumber = 0;
//
//        // get positions of targets
//        if (sentence.getTargets().size() == 2) {
//            firstTargetNumber = sentence.getTargets().get(0).getFirstWordPos();
//            seconTargetdNumber = sentence.getTargets().get(1).getFirstWordPos();
//        } else {
//            firstTargetNumber = sentence.getTargets().get(0).getFirstWordPos();
//        }
//
//        // get position of given idref
//        if (sentence.getTerminals().containsKey(idref)) {
//            idRefNumber = sentence.getTerminals().get(idref).getFirstWordPos();
//            //idRefNumber = Integer.parseInt(idref.split("_")[1]);
//        } else {
//            idRefNumber = sentence.getNonterminals().get(idref).getFirstWordPos();
//            //idRefNumber = Integer.parseInt(getFirstTerminalOfNonTerminal(idref).split("_")[0]);
//        }
//
//        // apply position rules
//        if (seconTargetdNumber == 0) {
//            if (idRefNumber < firstTargetNumber) {
//                position = "2";
//            } else {
//                position = "3";
//            }
//        } else {
//            if (idRefNumber < firstTargetNumber) {
//                position = "0";
//            } else if (idRefNumber < seconTargetdNumber) {
//                position = "1";
//            } else {
//                position = "3";
//            }
//        }
//
//        if (position.isEmpty()) {
//            throw new Exception("The position could not be determined!");
//        }
//
//        return position;
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

        //if(idrefs.contains("s1005_504")){
        //    System.out.println("TEST");
        //}
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
                    if (headRules.headRuleIsDefined(curCat)){//headEdges.containsKey(curCat) && headPosTags.containsKey(curCat)) {
                        //List<String> curHeadEdges = headEdges.get(curCat);
                        //List<String> curHeadPosTags = headPosTags.get(curCat);
                        for (Map.Entry<String, String> edge : curNode.getEdges().entrySet()) {
                            String curEdge = edge.getValue();
                            String curChildCat = sentence.getNode(edge.getKey()).getCategory();
                            //if(curHeadEdges.contains(curEdge))
                            //    System.out.println("TEST2");
                            //if(curHeadPosTags.contains(curChildCat))
                            //    System.out.println("TEST3");
                            //if (curHeadEdges.contains(curEdge) && (curHeadPosTags.contains(curChildCat) || (curHeadPosTags.size() == 0))) {
                            if(headRules.allowedHeadEdge(curCat,curEdge,curChildCat)){
                                newIdRefs.add(edge.getKey());
                            }
                        }
                    } else {
                        throw new Exception("no head-edge or no head-pos-tag for category " + curCat + " (for idref: " + curNode.getId() + ")");
                    }
                }
            }
        }
        if (newIdRefs.isEmpty()) {
            if (!idrefs.contains(sentence.getRootIDref())) {
                //System.out.println(sentence);
                throw new Exception("no head found for idrefs: " + idrefs);
            }
            // TODO: Throw
            //throw new Exception("no head word found for: "+idrefs);
            return null;
        }
        return calculateHeadWord(newIdRefs);
    }

    public void enrichInformation() throws Exception {
        traverseTree(sentence.getRootIDref(), new ArrayList<String>(30));
//		for (Node node : terminals.values()) {
//			if(node.getHeadIDref()!=null)
//			System.out.println(node.getId() + ": " + getNode(node.getHeadIDref()).getAttributes().get("word"));
//
//		}
//
//		for (Node node : nonterminals.values()) {
//			if(node.getHeadIDref()!=null)
//			System.out.println(node.getId() + ": " + getNode(node.getHeadIDref()).getAttributes().get("word"));
//
//		}
    }

}
