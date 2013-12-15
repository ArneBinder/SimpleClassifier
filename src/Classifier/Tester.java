package Classifier;

import java.io.File;

/**
 * Created by Arne on 11.12.13.
 */
public class Tester {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("4 arguments needed.");
            return;
        }
        Corpus corpus = new Corpus();
        System.out.println("read corpus from files in folder: " + args[0]);
        File f = new File(args[0]);
        File[] fileArray = f.listFiles();
        for (File file : fileArray) {
            System.out.println("file: " + file.getName());
            corpus.parseFile(file.getAbsolutePath());
        }
        System.out.println("done. "+corpus.getSentenceCount()+" sentences added.");

        //System.out.println("write corpus to file: " + args[1]);
        //corpus.writeCorpusToFile(args[1]);
        //System.out.println("done.");

        System.out.println("train model");
        Model model = corpus.trainModel();
        System.out.println("done.");

        System.out.println("write model to file: " + args[2]);
        model.writeModelToFile(args[2]);
        System.out.println("done.");

//        System.out.println("read model from file: " + args[2]);
//        Model model2 = new Model(corpus.getFeatureExtractor());
//        model2.readModelFromFile(args[2]);
//        System.out.println("done.");
//
//        Corpus corpus2 = new Corpus();
//        System.out.println("read corpus from file: " + args[3]);
//        corpus2.parseFile(args[3]);
//        System.out.println("done.");
//
//        System.out.println("delete annotation from corpus");
//        corpus2.deleteAnnotation();
//        System.out.println("done.");
//
//        System.out.println("annotate corpus");
//        corpus2.annotateCorpus(model2);
//        System.out.println("done.");
//
//        System.out.println("write corpus to file: " + args[1]);
//        corpus2.writeCorpusToFile(args[1]);
//        System.out.println("done.");

    }

}
