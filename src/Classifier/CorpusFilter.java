package Classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Arne on 13.01.14.
 */
public class CorpusFilter {
	public static void main(String[] args) throws Exception {
		if (args.length < 3)
			System.out.println("Wrong arguments: <sourceCorpusFile> <allowedFrameElementsFile> <resultCorpusFile>");
		else {
			System.out.println("--- Read corpus data from "+args[0]+" ---");
			Corpus corpus = ClassifierNB.readCorpusData(new File(args[0]));
			System.out.println("--- Read filter data from "+args[1]+" ---");
			List<String> allowedFrameElements = new LinkedList<String>();
			BufferedReader in = new BufferedReader(new FileReader(args[1]));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				allowedFrameElements.add(zeile);
				System.out.println(zeile);
			}
			in.close();
			
			System.out.println("--- Filter corpus data ---");
			corpus.filter(allowedFrameElements);
			System.out.println("--- Write corpus data to " + args[2] + " ---");
			corpus.writeCorpusToFile(args[2]);
		}
	}
}
