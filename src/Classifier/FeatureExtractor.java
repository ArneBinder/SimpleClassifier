package Classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Classifier.bean.*;
import Classifier.bean.Exceptions.*;

public class FeatureExtractor {

	private static class HeadRules {
		//smaler value --> higher priority!!!
		int priority = 0;
		Map<String, Map<String, Map<String, Integer>>> headRules = new HashMap<String, Map<String, Map<String, Integer>>>();

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
			addRule(category, edgeLabel, "");
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
			if (!headRules.containsKey(category) || !headRules.get(category).containsKey(edgeLabel))
				return Integer.MAX_VALUE;
			if (headRules.get(category).get(edgeLabel).containsKey(""))
				return headRules.get(category).get(edgeLabel).get("");
			if (headRules.get(category).get(edgeLabel).containsKey(childCategory))
				return headRules.get(category).get(edgeLabel).get(childCategory);
			return Integer.MAX_VALUE;
		}

		public String chooseHeadChild(String parentCategory, Map<String, String> edges, Sentence sentence) throws IDrefNotInSentenceException {
			//smaller is better!
			int bestEdgeQuality = Integer.MAX_VALUE;
			String bestChildRef = null;
			int curEdgeQuality;
			for (Map.Entry<String, String> edge : edges.entrySet()) {
				curEdgeQuality = getHeadRulePriority(parentCategory, edge.getValue(), sentence.getNode(edge.getKey()).getCategory());
				if (bestEdgeQuality > curEdgeQuality) {
					bestEdgeQuality = curEdgeQuality;
					bestChildRef = edge.getKey();
				}
			}
			//TODO: fix this hack!!!
			//if(bestChildRef==null)
			//	return edges.keySet().iterator().next();
			return bestChildRef;
		}
	}

	private Sentence sentence = null;
	//private static final String roleIdent = FeatureVector.getRoleTypeIdentifier();
	//private static final String splitChar = FeatureVector.getSplitChar();
	private static FeatureTypes featureTypes = new FeatureTypes();
	private static String[] phrasalCategories = {"AA", "AP", "AVP", "CAC", "CAVP", "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA", "NM", "NP", "PP", "QL", "S", "VP", "VZ"};
	private static HeadRules headRules;

	private static Map<String, String> abstractSynCat = new HashMap<String, String>();

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
		headRules.addRule("PP", Arrays.asList("MO", "AC"), Arrays.asList("APPR", "APPRART", "APPO", "FM"));
		headRules.addRule("PP", Arrays.asList("NK"), Arrays.asList("ADJA"));
		headRules.addRule("PN", Arrays.asList("PNC"), Arrays.asList("NP", "NN", "NE"));
		headRules.addRule("AVP", Arrays.asList("RE", "AVC"), Arrays.asList(addToPhrasalCat("ADV")));
		headRules.addRule("AVP", Arrays.asList("MO"), Arrays.asList("AVP"));
		headRules.addRule("DL", Arrays.asList("DH"), new ArrayList<String>());
		headRules.addRule("NM", Arrays.asList("NMC"), Arrays.asList("CARD", "NN"));
		headRules.addRule(Arrays.asList("AP", "PP", "NP"), Arrays.asList("RE"), new ArrayList<String>());
		//headRules.addRule(Arrays.asList("CAC", "CAP", "CAVP","CCP","CNP","CO","CPP","CS","CVP","CVZ"), Arrays.asList("CD"), Arrays.asList(addToPhrasalCat("KON")));
		headRules.addRule("CAP", Arrays.asList("CJ"), Arrays.asList("CPP", "PP", "CARD", "ADJA", "ADJD", "AP", "NN", "NM", "PIAT"));
		headRules.addRule("CAVP", Arrays.asList("CJ"), Arrays.asList("AVP", "ADV", "PWAV"));
		headRules.addRule("CCP", Arrays.asList("CJ"), Arrays.asList("CP", "KOUS"));
		headRules.addRule("CNP", Arrays.asList("CJ"), Arrays.asList("NP", "PN", "NN", "NE", "CARD"));
		headRules.addRule("CPP", Arrays.asList("CJ"), Arrays.asList("CPP", "PP"));
		headRules.addRule("CS", Arrays.asList("CJ"), Arrays.asList("S"));
		headRules.addRule("CVP", Arrays.asList("CJ"), Arrays.asList("VP"));
		headRules.addRule("CVZ", Arrays.asList("CJ"), Arrays.asList("VZ"));
		headRules.addRule("CAC", Arrays.asList("CJ"), Arrays.asList("APPR"));
		headRules.addRule("CO", Arrays.asList("CJ"), new ArrayList<String>());

		headRules.addRule("NP", Arrays.asList("CM"), Arrays.asList("KOKOM"));
		headRules.addRule("VZ", Arrays.asList("PM"), Arrays.asList("PTKZU"));
		headRules.addRule("CH", Arrays.asList(""), new ArrayList<String>());


		headRules.addRule("AP", "MO", "PP");
		headRules.addRule("AVP", "MO", "ADV");
		headRules.addRule("AVP", "CC", "S");
		headRules.addRule("AVP", "CM", "KOKOM"); //s22498_21
		headRules.addRule("AVP", "CC", "S"); //s121_5
		headRules.addRule("AVP", "RE", "PN"); //s9397_505
		headRules.addRule("AVP", "MO", "ADV"); //s4427_2
		headRules.addRule("CH", "UC", "FM"); //s38695_21
		headRules.addRule("CH", "UC", "NE"); //s5384_5
		headRules.addRule("CH", "OC", "PN"); //s23054_507
		headRules.addRule("CNP", "CJ", "CNP"); //s39395_505
		headRules.addRule("CNP", "NK", "NE"); //s44566_12
		headRules.addRule("CNP", "CJ", "PTKANT"); //s7097_13
		headRules.addRule("CNP", "CJ", "PIS"); //s7097_42
		headRules.addRule("CNP", "CJ", "TRUNC"); //s6056_16
		headRules.addRule("CNP", "CJ", "FM"); //s18292_38
		headRules.addRule("CNP", "CJ", "PPER"); //s24726_25
		headRules.addRule("CS", "CJ", "CS"); //s5614_516
		headRules.addRule("CVP", "CJ", "VVPP"); //s3693_29
		headRules.addRule("CVP", "CJ", "VVINF"); //s35976_7
		headRules.addRule("CVP", "CJ", "VZ"); //s33265_506
		headRules.addRule("ISU", "UC", "$."); //s10077_17
		headRules.addRule("ISU", "UC", "ADV"); //s7097_7
		headRules.addRule("ISU", "UC", "KOKOM"); //s36167_26
		headRules.addRule("MTA", "ADC", "NE"); //s37131_4
		headRules.addRule("MTA", "ADC", "ADJA"); //s36037_10
		headRules.addRule("NP", "NK", "FM"); //s15343_25
		headRules.addRule("NP", "NK", "PIAT"); //s33529_5
		headRules.addRule("NP", "NK", "CH"); //s38695_502
		headRules.addRule("NP", "NK", "ISU"); //s7097_500
		headRules.addRule("NP", "PH", "PPER"); //s45389_11
		headRules.addRule("NP", "NK", "PIAT"); //s8142_23
		headRules.addRule("NP", "NK", "CAP"); //s34758_502
		//TODO: s39166_507 has wrong annotation (category)!! has to be AVP instead of NP
		headRules.addRule("NP", "NK", "ADV"); //s34870_29
		headRules.addRule("NP", "NK", "S"); //s5387_508
		headRules.addRule("NP", "NK", "ART"); //s5387_508
		headRules.addRule("PN", "PNC", "FM"); //s22611_4
		headRules.addRule("CVP", "CJ", "VVFIN"); //s14032_500

		//TODO: check these... (look up idrefs)

		headRules.addRule("PN", "PNC", "TRUNC"); //s36594_18
		headRules.addRule("PN", "PNC", "CARD"); //s39912_2
		headRules.addRule("PN", "PNC", "PP"); //s21687_502
		headRules.addRule("PN", "PNC", "VP"); //s21923_501
		headRules.addRule("PN", "PNC", "S"); //s25449_504
		headRules.addRule("PN", "PNC", "XY"); //s989_16
		headRules.addRule("PN", "PNC", "PN"); //s12711_504
		headRules.addRule("PN", "PNC", "CO"); //s26293_510
		headRules.addRule("PN", "PNC", "AVP"); //s23394_504
		headRules.addRule("PN", "PNC", "CAP"); //s26269_502
		headRules.addRule("PP", "AC", "PROAV"); //s40876_1
		headRules.addRule("PP", "AC", "APZR"); //s40876_2
		headRules.addRule("PP", "AC", "CAC"); //s5285_500
		headRules.addRule("PP", "NK", "NN"); //s5285_6
		headRules.addRule("PP", "NK", "ART"); //s6826_36
		headRules.addRule("PP", "AG", "NP"); //s2090_504
		headRules.addRule("PP", "MNR", "PP"); //s17706_506
		headRules.addRule("PP", "NK", "CARD"); //s17706_27
		headRules.addRule("PP", "RC", "S"); //s11874_509
		headRules.addRule("PP", "NK", "CPP"); //s50227_508
		headRules.addRule("PP", "MNR", "ADV"); //s22280_28
		headRules.addRule("PP", "MO", "PP"); //s23557_500
		headRules.addRule("PP", "NK", "NE"); //s1236_16
		headRules.addRule("PP", "CM", "KOKOM"); //s23274_14
		headRules.addRule("PP", "MO", "ADV"); //s34060_27
		headRules.addRule("PP", "AC", "KOKOM"); //s363_16
		headRules.addRule("PP", "AG", "NN"); //s363_19
		headRules.addRule("PP", "AC", "PP"); //s5372_500
		headRules.addRule("S", "PD", "NP"); //s19893_508
		headRules.addRule("S", "PD", "ADJD"); //s28274_23
		headRules.addRule("S", "CP", "KOUS"); //s28274_22
		headRules.addRule("S", "JU", "KON"); //s24517_1
		headRules.addRule("S", "OA", "NP"); //s12321_505
		headRules.addRule("S", "OA", "NN"); //s31182_4
		headRules.addRule("S", "SVP", "PTKVZ"); //s31182_5
		headRules.addRule("S", "NG", "PTKNEG"); //s14210_17
		headRules.addRule("S", "PD", "CAP"); //s11402_505
		headRules.addRule("S", "DM", "PTKANT"); //s35505_2
		headRules.addRule("S", "VO", "NN"); //s10772_9
		headRules.addRule("S", "PD", "VVPP"); //s9241_17
		headRules.addRule("VP", "MO", "PP"); //s6057_502
		headRules.addRule("VP", "SVP", "PTKVZ"); //s6057_14
		headRules.addRule("VP", "OC", "CVP"); //s13992_512
		headRules.addRule("VP", "MO", "S"); //s13992_511
		headRules.addRule("VP", "OC", "VVPP"); //s8981_15
		headRules.addRule("VP", "OC", "VP"); //s36315_509
		headRules.addRule("VP", "CM", "KOKOM"); //s25578_6
		headRules.addRule("VP", "MO", "ADV"); //s26673_31
		headRules.addRule("VP", "OA", "NP"); //s121_505
		headRules.addRule("VP", "NG", "PTKNEG"); //s121_25
		headRules.addRule("VP", "PD", "NP"); //s46300_503
		headRules.addRule("VP", "CM", "VVPP"); //s4305_10
		headRules.addRule("VP", "MO", "ADJD"); //s4305_4
		headRules.addRule("VP", "MO", "VVPP"); //s2398_38
		headRules.addRule("VP", "OP", "PP"); //s2398_506
		headRules.addRule("NP", "MO", "ADV"); //s39166_507


		abstractSynCat.put("AA", "AA"); // superlative phrase with "am"
		abstractSynCat.put("CVZ", "Z"); // coordinated zu-marked infinitive
		abstractSynCat.put("AP", "A"); //adjektive phrase
		abstractSynCat.put("DL", "DL"); //discourse level constituent
		abstractSynCat.put("AVP", "AV"); //adverbial phrase
		abstractSynCat.put("ISU", "ISU"); //idiosyncratis unit
		abstractSynCat.put("CAC", "P"); //coordinated adposition
		abstractSynCat.put("MPN", "N"); //multi-word proper noun
		abstractSynCat.put("CAP", "A"); //coordinated adjektive phrase
		abstractSynCat.put("MTA", "A"); //multi-token adjective
		abstractSynCat.put("CAVP", "AV"); //coordinated adverbial phrase
		abstractSynCat.put("NM", "NM"); //multi-token number
		abstractSynCat.put("CCP", "CP"); //coordinated complementiser
		abstractSynCat.put("NP", "N"); //noun phrase
		abstractSynCat.put("CH", "CH"); //chunk
		abstractSynCat.put("PP", "P"); //adpositional phrase
		abstractSynCat.put("CNP", "N"); // coordinated noun phrase
		abstractSynCat.put("QL", "QL"); // quasi-languag
		abstractSynCat.put("CO", "CO"); // coordination
		abstractSynCat.put("S", "S"); // sentence
		abstractSynCat.put("CPP", "P"); // coordinated adpositional phrase
		abstractSynCat.put("VP", "V"); // verb phrase (non-finite)
		abstractSynCat.put("CS", "S"); // coordinated sentence
		abstractSynCat.put("VZ", "Z"); // zu-marked infinitive
		abstractSynCat.put("CVP", "V"); // coordinated verb phrase (non-finite)

		abstractSynCat.put("ADJA", "A"); //   attributives Adjektiv                   [das] große [Haus]
		abstractSynCat.put("ADJD", "A"); //     adverbiales oder prädikatives Adjektiv                       [er fährt] schnell;  [er ist] schnell
		abstractSynCat.put("ADV", "AV"); //      Adverb                                  schon, bald, doch
		abstractSynCat.put("APPR", "P"); //     Präposition; Zirkumposition links       in [der Stadt], ohne [mich]
		abstractSynCat.put("APPRART", "P"); //  Präposition mit Artikel                 im [Haus], zur [Sache]
		abstractSynCat.put("APPO", "P"); //     Postposition                            [ihm] zufolge, [der Sache] wegen
		abstractSynCat.put("APZR", "P"); //     Zirkumposition rechts                   [von jetzt] an
		abstractSynCat.put("ART", "D"); //      bestimmter oder unbestimmter Artikel                        der, die, das, ein, eine, ...
		abstractSynCat.put("CARD", "NM"); //     Kardinalzahl                            zwei [Männer], [im Jahre] 1994
		abstractSynCat.put("FM", ""); //       Fremdsprachliches Material              [Er hat das mit ``] A big fish ['' übersetzt]
		abstractSynCat.put("ITJ", ""); //      Interjektion                            mhm, ach, tja
		abstractSynCat.put("ORD", "NM"); //      Ordinalzahl                             [der] neunte [August]
		abstractSynCat.put("KOUI", "C"); //     unterordnende Konjunktion mit ``zu'' und Infinitiv              um [zu leben], anstatt [zu fragen]
		abstractSynCat.put("KOUS", "C"); //     unterordnende Konjunktion mit Satz              weil, daß, damit, wenn, ob
		abstractSynCat.put("KON", "C"); //      nebenordnende Konjunktion               und, oder, aber
		abstractSynCat.put("KOKOM", "C"); //    Vergleichskonjunktion                   als, wie
		abstractSynCat.put("NN", "N"); //       normales Nomen                          Tisch, Herr, [das] Reisen
		abstractSynCat.put("NE", "N"); //       Eigennamen                              Hans, Hamburg, HSV
		abstractSynCat.put("PDS", "PR"); //      substituierendes Demonstrativ-pronomen          dieser, jener
		abstractSynCat.put("PDAT", "PR"); //     attribuierendes Demonstrativ-pronomen           jener [Mensch]
		abstractSynCat.put("PIS", "PR"); //      substituierendes Indefinit-pronomen             keiner, viele, man, niemand
		abstractSynCat.put("PIAT", "PR"); //     attribuierendes Indefinit-pronomen ohne Determiner              kein [Mensch], irgendein [Glas]
		abstractSynCat.put("PIDAT", "PR"); //    attribuierendes Indefinit-pronomen mit Determiner                [ein] wenig [Wasser], [die] beiden [Brüder]
		abstractSynCat.put("PPER", "PR"); //     irreflexives Personalpronomen           ich, er, ihm, mich, dir
		abstractSynCat.put("PPOSS", "PR"); //    substituierendes Possessiv-pronomen             meins, deiner
		abstractSynCat.put("PPOSAT", "PR"); //   attribuierendes Possessivpronomen       mein [Buch], deine [Mutter]
		abstractSynCat.put("PRELS", "PR"); //   substituierendes Relativpronomen        [der Hund ,] der
		abstractSynCat.put("PRELAT", "PR"); //  attribuierendes Relativpronomen         [der Mann ,] dessen [Hund]
		abstractSynCat.put("PRF", "PR"); //     reflexives Personalpronomen             sich, einander, dich, mir
		abstractSynCat.put("PWS", "PR"); //     substituierendes Interrogativpronomen                       wer, was
		abstractSynCat.put("PWAT", "PR"); //    attribuierendes Interrogativpronomen                        welche [Farbe], wessen [Hut]
		abstractSynCat.put("PWAV", "PR"); //    adverbiales Interrogativ- oder Relativpronomen                warum, wo, wann, worüber, wobei
		abstractSynCat.put("PAV", ""); //     Pronominaladverb                        dafür, dabei, deswegen, trotzdem
		abstractSynCat.put("PTKZU", ""); //   ``zu'' vor Infinitiv                    zu [gehen]
		abstractSynCat.put("PTKNEG", ""); //  Negationspartikel                       nicht
		abstractSynCat.put("PTKVZ", ""); //   abgetrennter Verbzusatz                 [er kommt] an, [er fährt] rad
		abstractSynCat.put("PTKANT", ""); //  Antwortpartikel                         ja, nein, danke, bitte
		abstractSynCat.put("PTKA", ""); //    Partikel bei Adjektiv oder Adverb                  am [schönsten], zu [schnell]
		abstractSynCat.put("SGML", ""); //    SGML Markup
		abstractSynCat.put("SPELL", ""); //   Buchstabierfolge                        S-C-H-W-E-I-K-L
		abstractSynCat.put("TRUNC", ""); //   Kompositions-Erstglied                  An- [und Abreise]
		abstractSynCat.put("VVFIN", ""); //   finites Verb, voll                      [du] gehst, [wir] kommen [an]
		abstractSynCat.put("VVIMP", ""); //   Imperativ, voll                         komm [!]
		abstractSynCat.put("VVINF", ""); //   Infinitiv, voll                         gehen, ankommen
		abstractSynCat.put("VVIZU", ""); //   Infinitiv mit ``zu'', voll              anzukommen, loszulassen
		abstractSynCat.put("VVPP", ""); //    Partizip Perfekt, voll                  gegangen, angekommen
		abstractSynCat.put("VAFIN", ""); //   finites Verb, aux                       [du] bist, [wir] werden
		abstractSynCat.put("VAIMP", ""); //   Imperativ, aux                          sei [ruhig !]
		abstractSynCat.put("VAINF", ""); //   Infinitiv, aux                          werden, sein
		abstractSynCat.put("VAPP", ""); //    Partizip Perfekt, aux                   gewesen
		abstractSynCat.put("VMFIN", ""); //   finites Verb, modal                     dürfen
		abstractSynCat.put("VMINF", ""); //   Infinitiv, modal                        wollen
		abstractSynCat.put("VMPP", ""); //    Partizip Perfekt, modal                 gekonnt, [er hat gehen] können
		abstractSynCat.put("XY", ""); //      Nichtwort, Sonderzeichen enthaltend                3:7, H2O, D2XW3
		abstractSynCat.put("\\$,", ""); //     Komma                                   ,
		abstractSynCat.put("\\$.", ""); //     Satzbeendende Interpunktion             . ? ! ; :
		abstractSynCat.put("\\$(", ""); //     sonstige Satzzeichen; satzintern        - [,]()


	}


	public static FeatureTypes getFeatureTypes() {
		return featureTypes;
	}

	public static String getAbstractSynCat(String synCat) throws Exception {
		if (!abstractSynCat.containsKey(synCat))
			throw new Exception("synCat " + synCat + " is unknown");
		return abstractSynCat.get(synCat);
	}

	public static HeadRules getHeadRules() {
		return headRules;
	}

	public static String calculateHeadIDref(String parentCategory, Map<String, String> edges, Sentence sentence) throws IDrefNotInSentenceException {
		return headRules.chooseHeadChild(parentCategory, edges, sentence);
	}


	public void setSentence(Sentence s) {
		this.sentence = s;
		//dummy = "";
		//enrichInformation();
	}

	public FeatureVector extract(String idref)
			throws IDrefNotInSentenceException, RootNotInPathException, FeatureValueNotCalculatedException {
		FeatureVector fv = new FeatureVector();
		if (FeatureTypes.isUsedFeatureType("target"))
			fv.addFeature("target", extractTarget());
		if (FeatureTypes.isUsedFeatureType("synCat"))
			fv.addFeature("synCat", extractSyntacticalCategory(idref));
		if (FeatureTypes.isUsedFeatureType("position"))
			fv.addFeature("position", extractPosition(idref));
		//fv.addFeature("path", extractPath(idref));
		if (FeatureTypes.isUsedFeatureType("path") || FeatureTypes.isUsedFeatureType("funcPath"))
			extractPath(idref, fv);
		if (FeatureTypes.isUsedFeatureType("head"))
			fv.addFeature("head", extractHead(idref));
		if (FeatureTypes.isUsedFeatureType("terminal"))
			fv.addFeature("terminal", sentence.getNode(idref).isTerminal() ? "1" : "0");
		if (FeatureTypes.isUsedFeatureType("nextHead"))
			fv.addFeature("nextHead", extractNextHead(idref));

		return fv;
	}

	//private String extract

	private String extractHead(String idref) throws IDrefNotInSentenceException {
		String headIDref = sentence.getNode(idref).getHeadIDref();
		if (headIDref != null)
			return sentence.getNode(headIDref).getAttributes().get("lemma");
		return "NOHEAD";
	}

	private String extractNextHead(String idRef) throws IDrefNotInSentenceException {

		Node node = sentence.getNode(idRef);
		String headIDref = null;
		String result = "HEAD";
		int readyCount = 0;
		int i = 0;
		while (headIDref == null && readyCount < node.getPathsFromRoot().size()) {
			i++;
			for (String[] path : node.getPathsFromRoot()) {
				if (path.length >= i) {
					String curHeadIDref;
					curHeadIDref = sentence.getNode(path[path.length - i]).getHeadIDref();
					if (curHeadIDref != null && !curHeadIDref.equals(sentence.getNode(idRef).getHeadIDref())) {
						headIDref = curHeadIDref;
					}
				} else {
					readyCount++;
				}
			}
		}

		if (headIDref != null) {
			result = sentence.getNode(headIDref).getAttributes().get("lemma");
			//System.out.print(idRef + ": " + result + "\t[");
			//for(String s: sentence.getNode(headIDref).getPathsFromRoot().get(0)){
			//	System.out.print(s+":"+sentence.getNode(s).getHeadIDref()+", ");
			//}
			//System.out.println("]");
		}
		return result;

	}

	private void extractPath(String idref, FeatureVector fv)
			throws IDrefNotInSentenceException, RootNotInPathException {

		String path = "";
		String cat;
		String lastCat = "";
		String func = "";
		String lastFunc = "";
		//String grammaticalFunctionBig = "X";
		//String grammaticalFunctionSmall = "X";
		String functionPath = "";

		String targetHeadIDref = sentence.getTarget().getId();//sentence.getTarget().getHeadIDref(); //
		if (targetHeadIDref.equals(idref)) {
			path = "TARGET";
			functionPath = "~";
		} else {
			List<String> idRefs = new ArrayList<String>(2);
			idRefs.add(idref);
			idRefs.add(targetHeadIDref);

			int[] indices = sentence.calculateRootOfSubtree(idRefs);
			String[] ownIdPath = sentence.getNode(idref).getPathFromRoot(indices[1]);
			String[] targetIdPath = sentence.getNode(targetHeadIDref).getPathFromRoot(indices[2]);

			int i = indices[0];//0;

			String daughterIDref = idref;
			for (int j = ownIdPath.length - 1; j > i; j--) {
				Node node = sentence.getNode(ownIdPath[j]);
				cat = node.getCategory();
				if (!cat.startsWith("C") && !cat.equals(lastCat)) {
					path += cat + "+";
					lastCat = cat;
				}
				func = node.getEdges().get(daughterIDref);
				if (!func.startsWith("C") && !func.equals(lastFunc)) {
					functionPath += func + "+";
					lastFunc = func;
				}
				daughterIDref = ownIdPath[j];
			}

			String parentIDref;
			//try {

			if (i < targetIdPath.length) {
				path += sentence.getNode(targetIdPath[i]).getCategory();
				func = sentence.getNode(targetIdPath[i]).getEdges().get(daughterIDref);
				parentIDref = targetIdPath[i];
			} else {
				path += sentence.getNode(targetHeadIDref).getCategory();
				func = sentence.getNode(targetHeadIDref).getEdges().get(daughterIDref);
				parentIDref = targetHeadIDref;
			}

			if (func != null)
				functionPath += func;
			functionPath += "~";

			lastCat = "";
			lastFunc = "";
			for (int j = i + 1; j < targetIdPath.length; j++) {
				Node node = sentence.getNode(targetIdPath[j]);
				cat = node.getCategory();
				if (!cat.startsWith("C") && !cat.equals(lastCat)) {
					path += "-" + cat;
					lastCat = cat;
				}
				func = sentence.getNode(parentIDref).getEdges().get(targetIdPath[j]);
				if ((!func.startsWith("C") || j == i + 1) && !func.equals(lastFunc)) {
					functionPath += func + "-";
					lastFunc = func;
				}
				parentIDref = targetIdPath[j];
			}
			func = sentence.getNode(parentIDref).getEdges().get(targetHeadIDref);

			if (func != null && !func.equals(lastFunc))
				functionPath += func;
			else if (functionPath.endsWith("-"))
				functionPath = functionPath.substring(0, functionPath.length() - 1);
		}
		if (functionPath.equals(""))
			System.out.println("ERROR functionPath: " + idref);

		//return path;
		if (FeatureTypes.isUsedFeatureType("path"))
			fv.addFeature("path", path);
		if (FeatureTypes.isUsedFeatureType("funcPath"))
			fv.addFeature("funcPath", functionPath);
		//fv.addFeature("funcBig", grammaticalFunctionBig);
		//fv.addFeature("funcSmall", grammaticalFunctionSmall);
		//System.out.println(idref+": "+grammaticalFunctionSmall);
	}


	private String extractPosition(String idref)
			throws IDrefNotInSentenceException, FeatureValueNotCalculatedException {

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
		int ownPos = Helper.getPosFromID(sentence.getNode(idref).getHeadIDref());


		if (targetFirstPos != targetLastPos && ownPos <= targetFirstPos)
			return "0";
		if (targetFirstPos != targetLastPos && ownPos <= targetLastPos)
			return "1";
		if (targetFirstPos == targetLastPos && ownPos <= targetLastPos)
			return "2";
		if (ownPos >= targetLastPos)
			return "3";

		if (position.isEmpty()) {
			throw new FeatureValueNotCalculatedException("The position could not be determined!");
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
	private String extractSyntacticalCategory(String idref) throws IDrefNotInSentenceException {
		return sentence.getNode(idref).getCategory();
	}

	private String extractTarget() throws IDrefNotInSentenceException {

		String targetLemma = "";
		targetLemma = sentence.getNode(sentence.getTarget().getHeadIDref()).getAttributes().get("lemma");

		return targetLemma;
	}


}
