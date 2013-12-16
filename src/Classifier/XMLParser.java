package Classifier;

import java.util.*;

import Classifier.bean.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import Classifier.bean.Frame;
import Classifier.bean.Sentence;

public class XMLParser extends DefaultHandler {

	private Stack<String> elements = new Stack<String>();
	private List<Sentence> sentences = new ArrayList<Sentence>();
    // [parent, label, child]
    private List<String[]> secedges = new LinkedList<String[]>();

	private Sentence curSentence = null;
	private Node curNode = null;
	private Frame curFrame = null;
	private String curFEName = "";
    //private Node curTerminal = null;

	@Override
	public void startElement(String uri, String localName, String qName,
							 Attributes attributes) throws SAXException {

		String topElement = "";
		if (elements.size() > 0) {
			topElement = elements.peek();
		}

		if (localName.equals("body")) {
			elements.push(localName);
		}

		if (topElement.equals("s") && localName.equals("graph")) {
			curSentence.setRootID(attributes.getValue("root"));
			elements.push(localName);
		}

		if (elements.size() > 0 && elements.get(0).equals("body")) {
			if (localName.equals("s")) {
				elements.push(localName);
				curSentence = new Sentence(attributes.getValue("id"));
			}

            if(localName.equals("secedge") && (topElement.equals("t") || topElement.equals("nt"))){
                String[] secedge = {attributes.getValue("idref"), attributes.getValue("label"), curNode.getId()};
                secedges.add(secedge);
            }

			if (localName.equals("t") && topElement.equals("graph")) {
                elements.push(localName);
                curNode = new Node(attributes.getValue("id"));
                //TODO: warum? sind doch noch nicht gesetzt...
				Map<String, String> attrMap = curNode.getAttributes();

				for (int i = 0; i < attributes.getLength(); i++) {
					attrMap.put(attributes.getLocalName(i),
							attributes.getValue(i));
				}

                curNode.setAttributes(attrMap);

				//curSentence.addTerminal(curTerminal);
			}



			if (localName.equals("nt") && topElement.equals("graph")) {
				elements.push(localName);
				curNode = new Node(attributes.getValue("id"),	attributes.getValue("cat"));
			}

			if (localName.equals("edge") && topElement.equals("nt")) {
				curNode.addEdge(attributes.getValue("idref"),
                        attributes.getValue("label"));
			}

			if (localName.equals("frame") && topElement.equals("s")) {
				elements.push(localName);
				curFrame = new Frame(attributes.getValue("id"),
						attributes.getValue("name"));
			}

			if (localName.equals("target") && topElement.equals("frame")) {
				elements.push(localName);
				curFrame.setTarget(attributes.getValue("lemma"));
			}

			if (localName.equals("fe") && topElement.equals("frame")) {
				elements.push(localName);
				curFEName = attributes.getValue("name");
				curFrame.addFrameElement(curFEName);
			}

			if (localName.equals("fenode") && topElement.equals("target")) {
				curFrame.addTargetID(attributes.getValue("idref"));
			}

			if (localName.equals("fenode") && topElement.equals("fe")) {
				curFrame.addFrameElementWithIDRef(curFEName,
						attributes.getValue("idref"));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		String topElement = "";
		if (elements.size() > 0) {
			topElement = elements.peek();
		}
		if (localName.equals(topElement)) {
			elements.pop();

			if (localName.equals("s")) {
                for(String[] secEdge: secedges){
				    curSentence.addSecEdge(secEdge);
                }
                secedges = new LinkedList<String[]>();
                getSentences().add(curSentence);
				curSentence = null;
			}

			if (localName.equals("nt")) {
				curSentence.addNonterminal(curNode);
				curNode = null;
			}

            if (localName.equals("t")) {
                curSentence.addTerminal(curNode);
                curNode = null;
            }

			if (localName.equals("frame")) {
				curSentence.addFrame(curFrame);
				curFrame = null;
			}

			if (localName.equals("fe")) {
				curFEName = "";
			}
		}
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
}
