package Classifier;

import java.util.*;

import Classifier.bean.FeatureVector;
import Classifier.bean.Node;
import Classifier.bean.Sentence;

public class FeatureExtractor {

	public static final List<String> usedFeatures = Arrays.asList("target", "synCat", "position", "path", "target" + FeatureVector.getSplitChar() + "synCat", "head");
	private Sentence sentence = null;
    private static Map<String, List<String>> headPosTags;
    private static Map<String, List<String>> headEdges;
    private static String[] phrasalCategories = {"AA","AP","AVP","CAC", "CAVP", "CCP","CH","CNP","CO","CPP","CS","CVP","CVZ","DL","ISU","MPN","MTA","NM","NP","PP","QL","S","VP","VZ"};



    static{

        // TODO: "contains" is bad.. Create abstracted version for np/vp > change contains to equal (e.g. tree bank hierarchy)
        // TODO: Replace ~ !!!
        headPosTags = new HashMap<String, List<String>>();
        headPosTags.put("S", new ArrayList<String>());
        headPosTags.put("NP", Arrays.asList("NE", "NN", "PDS", "PIS", "PPOSS", "PRELS", "PWS", "NP"));
        headPosTags.put("PP", Arrays.asList("APPR", "APPRART","APPO"));
        headPosTags.put("PN", Arrays.asList("NP"));
        String[] temp =  new String[phrasalCategories.length+1];
        System.arraycopy(phrasalCategories,0,temp,0,phrasalCategories.length);
        temp[temp.length-1] = "ADV";
        headPosTags.put("AVP", Arrays.asList(temp));
        headPosTags.put("DL", new ArrayList<String>()); // alle erlaubt!
        headPosTags.put("CAC", Arrays.asList("KON"));
        headPosTags.put("CAP", Arrays.asList("KON"));
        headPosTags.put("CAVP", Arrays.asList("KON"));
        headPosTags.put("CCP", Arrays.asList("KON"));
        headPosTags.put("CNP", Arrays.asList("KON"));
        headPosTags.put("CO", Arrays.asList("KON"));
        headPosTags.put("CPP", Arrays.asList("KON"));
        headPosTags.put("CS", new ArrayList<String>());//TODO: fix workaround (s778_507)// Arrays.asList("KON"));
        headPosTags.put("CVP", Arrays.asList("KON"));
        headPosTags.put("CVZ", Arrays.asList("KON"));
        headPosTags.put("NM", Arrays.asList("CARD"));
        headPosTags.put("VZ", Arrays.asList("PTKZU"));//workaround for incorrect annotated s5240_525



        headEdges = new HashMap<String, List<String>>();
        headEdges.put("S", Arrays.asList("MO"));
        headEdges.put("NP", Arrays.asList("NK"));
        headEdges.put("PN", Arrays.asList("PNC"));
        headEdges.put("AVP", Arrays.asList("RE", "AVC")); //workaround: s12479_503
        headEdges.put("PP", Arrays.asList("MO", "AC"));
        headEdges.put("DL", Arrays.asList("DH"));
        headEdges.put("CAC", Arrays.asList("CD"));
        headEdges.put("CAP", Arrays.asList("CD"));
        headEdges.put("CAVP", Arrays.asList("CD"));
        headEdges.put("CCP", Arrays.asList("CD"));
        headEdges.put("CNP", Arrays.asList("CD"));
        headEdges.put("CO", Arrays.asList("CD"));
        headEdges.put("CPP", Arrays.asList("CD"));
        headEdges.put("CS", Arrays.asList("CD", "CJ")); //TODO: fix workaround (s778_507)
        headEdges.put("CVP", Arrays.asList("CD"));
        headEdges.put("CVZ", Arrays.asList("CD"));
        headEdges.put("NM", Arrays.asList("NMC"));
        headEdges.put("VZ", Arrays.asList("PM")); //workaround for incorrect annotated s5240_525
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

	public void setSentence(Sentence s)  throws Exception {
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
		String[] targetIdPath = sentence.getTargets().get(0).getPathFromRoot();
		String[] ownIdPath = sentence.getNode(idref).getPathFromRoot();

		if (sentence.getTargets().get(0).getId().equals(idref)) {
			path = "TARGET";
		} else {
			int i = 0;
			while (i < targetIdPath.length && i < ownIdPath.length && targetIdPath[i].equals(ownIdPath[i])) {
				i++;
			}

			for (int j = ownIdPath.length - 1; j >= i; j--) {

				path += sentence.getNode(ownIdPath[j]).getCategory() + "+";
			}
			// nimm die wurzel des subtrees nur mit rein, wenn idref nicht auf (global) root zeigt. sonst fuege Kategorie manuell ein...
			if (i == 0)
				path += "VROOT";
			else{
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
		int firstTargetNumber = 0;
		int seconTargetdNumber = 0;
		int idRefNumber = 0;

		// get positions of targets
		if (sentence.getTargets().size() == 2) {
			firstTargetNumber = sentence.getTargets().get(0).getFirstWordPos();
			seconTargetdNumber = sentence.getTargets().get(1).getFirstWordPos();
		} else {
			firstTargetNumber = sentence.getTargets().get(0).getFirstWordPos();
		}

		// get position of given idref
		if (sentence.getTerminals().containsKey(idref)) {
			idRefNumber = sentence.getTerminals().get(idref).getFirstWordPos();
			//idRefNumber = Integer.parseInt(idref.split("_")[1]);
		} else {
			idRefNumber = sentence.getNonterminals().get(idref).getFirstWordPos();
			//idRefNumber = Integer.parseInt(getFirstTerminalOfNonTerminal(idref).split("_")[0]);
		}

		// apply position rules
		if (seconTargetdNumber == 0) {
			if (idRefNumber < firstTargetNumber) {
				position = "2";
			} else {
				position = "3";
			}
		} else {
			if (idRefNumber < firstTargetNumber) {
				position = "0";
			} else if (idRefNumber < seconTargetdNumber) {
				position = "1";
			} else {
				position = "3";
			}
		}

		if (position.isEmpty()) {
			throw new Exception("The position could not be determined!");
		}

		return position;
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

		if (sentence.getTargets().size() == 2) {

			int firstNumber = sentence.getTargets().get(0).getFirstWordPos();//Integer.parseInt(firstTargetIDRef.split("_")[1]);
			int secondNumber = sentence.getTargets().get(1).getFirstWordPos();// Integer.parseInt(secondTargetIDRef.split("_")[1]);

			if (Math.abs(firstNumber - secondNumber) == 1) {

				if (firstNumber > secondNumber) {
					targetLemma = sentence.getTargets().get(1).getAttributes().get("lemma") + sentence.getTargets().get(0).getAttributes().get("lemma");
					//targetLemma = sentence.getTerminals().get(secondTargetIDRef).getAttributes().get("lemma") + sentence.getTerminals().get(firstTargetIDRef).getAttributes().get("lemma");
				} else {
					targetLemma = sentence.getTargets().get(0).getAttributes().get("lemma") + sentence.getTargets().get(1).getAttributes().get("lemma");
					//targetLemma = sentence.getTerminals().get(firstTargetIDRef).getAttributes().get("lemma") + sentence.getTerminals().get(secondTargetIDRef).getAttributes().get("lemma");
				}
			} else {

				if (firstNumber > secondNumber) {
					targetLemma = sentence.getTargets().get(0).getAttributes().get("lemma") + sentence.getTargets().get(1).getAttributes().get("lemma");
					//targetLemma = sentence.getTerminals().get(firstTargetIDRef).getAttributes().get("lemma") + sentence.getTerminals().get(secondTargetIDRef).getAttributes().get("lemma");
				} else {
					targetLemma = sentence.getTargets().get(1).getAttributes().get("lemma") + sentence.getTargets().get(0).getAttributes().get("lemma");
					//targetLemma = sentence.getTerminals().get(secondTargetIDRef).getAttributes().get("lemma") + sentence.getTerminals().get(firstTargetIDRef).getAttributes().get("lemma");
				}
			}

		} else {
			String targetIDRef = sentence.getTargets().get(0).getId();

			if (sentence.getTerminals().containsKey(targetIDRef)) {
				targetLemma = sentence.getTerminals().get(targetIDRef).getAttributes().get("lemma");
			} else {
				String headIDref = sentence.getNode(targetIDRef).getHeadIDref();
				if (headIDref != null)
					targetLemma = sentence.getTerminals().get(headIDref).getAttributes().get("lemma");
				else
					throw new Exception("Das TargetIDRef ist kein Terminal bzw. hat kein HeadWord!");
			}
		}


		return targetLemma;
	}


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
        curNode.setPathFromRoot(pathFromRoot.toArray(new String[pathFromRoot.size()]));
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
        String[] temp = idRef.split("_");
        return Integer.parseInt(temp[temp.length - 1]);
    }

    public Node calculateHeadWord(List<String> idrefs) throws Exception {
        Collections.sort(idrefs);
        //if(idrefs.contains("s21270_1"))
          //  System.out.println("test");
        if(idrefs.get(0).equals(sentence.getRootIDref()) && !sentence.getNode(sentence.getRootIDref()).isTerminal())
            return null;
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
                    if (headEdges.containsKey(curCat) && headPosTags.containsKey(curCat)) {
                        List<String> curHeadEdges = headEdges.get(curCat);
                        List<String> curHeadPosTags = headPosTags.get(curCat);
                        for (Map.Entry<String, String> edge : curNode.getEdges().entrySet()) {
                            if (curHeadEdges.contains(edge.getValue()) && (curHeadPosTags.contains(sentence.getNode(edge.getKey()).getCategory()) || (curHeadPosTags.size()==0))) {
                                newIdRefs.add(edge.getKey());
                            }
                        }

                        if(newIdRefs.isEmpty()){

                        }

                    } else {
                        throw new Exception("no head-edge or no head-pos-tag for category " + curCat + " (for idref: " + curNode.getId() + ")");
                    }
                }
            }
        }
        if (newIdRefs.isEmpty()){
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
