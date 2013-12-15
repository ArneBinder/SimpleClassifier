package Classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import Classifier.bean.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import Classifier.bean.Frame;
import Classifier.bean.Sentence;

public class XMLParser extends DefaultHandler {

	private Stack<String> elements = new Stack<String>();
	private List<Sentence> sentences = new ArrayList<Sentence>();

	private Sentence curSentence = null;
	private Node curNt = null;
	private Frame curFrame = null;
	private String curFEName = "";

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

			if (localName.equals("t") && topElement.equals("graph")) {

				Node t = new Node(attributes.getValue("id"));
				Map<String, String> attrMap = t.getAttributes();

				for (int i = 0; i < attributes.getLength(); i++) {
					attrMap.put(attributes.getLocalName(i),
							attributes.getValue(i));
				}

				t.setAttributes(attrMap);

				curSentence.addTerminal(t);
			}

			if (localName.equals("nt") && topElement.equals("graph")) {
				elements.push(localName);
				curNt = new Node(attributes.getValue("id"),
						attributes.getValue("cat"));
			}

			if (localName.equals("edge") && topElement.equals("nt")) {
				curNt.addEdge(attributes.getValue("idref"),
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
				getSentences().add(curSentence);
				curSentence = null;
			}

			if (localName.equals("nt")) {
				curSentence.addNonterminal(curNt);
				curNt = null;
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
