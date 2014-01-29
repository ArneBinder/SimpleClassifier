package Classifier.bean;

/**
 * Created by Arne on 22.01.14.
 */
public class ValidateResult {

	public final static int idxSentenceCount = 0;
	public final static int idxFrameCount = 1;
	public final static int idxFrameElementCount = 2;
	public final static int idxFrameElementIDrefCount = 3;

	
	public final static int idxTruePositiveFrameElementIDrefCount = 4;
	public final static int idxClassyFrameElementIDrefCount = 5;
	public final static int idxGoldFrameElementIDrefCount = 6;

	public final static int idxTruePositiveFrameElementCount = 7;
	public final static int idxClassyFrameElementCount = 8;
	public final static int idxGoldFrameElementCount = 9;

	public final static int idxTruePositiveFrameElementIDrefCountNameIndependent = 10;
	
	public final static int idxUnclassifiedSentenceCount_NotFound = 11;
	public final static int idxUnclassifiedSentenceCount_NoAnnotation = 12;
	
	public final static int idxUnclassifiedFrameCount_SNotFound = 13;
	public final static int idxUnclassifiedFrameCount_SNoAnnotation = 14;
	public final static int idxUnclassifiedFrameCount_TargetNotFound = 15;
	
	public final static int idxUnclassifiedFrameElementCount_SNotFound = 16;
	public final static int idxUnclassifiedFrameElementCount_SNoAnnotation = 17;
	public final static int idxUnclassifiedFrameElementCount_FTargetNotFound = 18;
	
	public final static int idxUnclassifiedFrameElementIDrefCount_SNotFound = 19;
	public final static int idxUnclassifiedFrameElementIDrefCount_SNoAnnotation = 20;
	public final static int idxUnclassifiedFrameElementIDrefCount_FTargetNotFound = 21;
	
	public final static int idxIDrefsFMeasure = 0;
	public final static int idxFEsFMeasure = 3;
	public final static int idxIDrefsNIFMeasure = 6;
	
	public final static int resultSize = 22;
	public final static int statisticSize = 9;

	private long[] result;
	private double[] statistic;

	public ValidateResult() {
		result = new long[resultSize];
		statistic = new double[statisticSize];
	}

	public ValidateResult(long[] result) {
		this.result = new long[resultSize];
		statistic = new double[statisticSize];

		assert (result.length == resultSize);
		System.arraycopy(result, 0, this.result, 0, resultSize);

		calculateStatistic();
	}
	
	private void calculateStatistic(){
		System.arraycopy(calculateStatisticValues(idxTruePositiveFrameElementIDrefCount, idxClassyFrameElementIDrefCount, idxGoldFrameElementIDrefCount), 0, statistic, idxIDrefsFMeasure, 3);
		System.arraycopy(calculateStatisticValues(idxTruePositiveFrameElementCount, idxClassyFrameElementCount, idxGoldFrameElementCount), 0, statistic, idxFEsFMeasure, 3);
		System.arraycopy(calculateStatisticValues(idxTruePositiveFrameElementIDrefCountNameIndependent, idxClassyFrameElementIDrefCount, idxGoldFrameElementIDrefCount), 0, statistic, idxIDrefsNIFMeasure, 3);
		    
	}

	private double[] calculateStatisticValues(int tpCount, int classyCount, int origCount) {

		double precision = result[tpCount] / (double) result[classyCount];
		double recall = result[tpCount] / (double) result[origCount];
		double fmeasure = 2.0 * precision * recall / (precision + recall);

		return new double[]{fmeasure, precision, recall};
	}

	public void addResult(ValidateResult validateResult) {
		for (int i = 0; i < result.length; i++) {
			result[i] += validateResult.getResult()[i];
		}
		
		calculateStatistic();
	}

	public void normalize(int count) {
		for (int i = 0; i < result.length; i++) {
			result[i] /= count;
		}

		calculateStatistic();
	}

	// Getter
	public long[] getResult() {
		return result;
	}

	public double[] getStatistic(){
	    return statistic;
	}
	
	public double getFMeasure(int idx) {
		if (idx == 0){
		    return statistic[idxIDrefsFMeasure];
		}else if (idx == 1){
		    return statistic[idxFEsFMeasure];
		}else if (idx == 2){
		    return statistic[idxIDrefsNIFMeasure];
		}else{
		    return -1.0;
		}
	}
	
        /*
         * Output
         */
	
	public void printResult() {
		System.out.println("SentenceCount: \t" + result[idxSentenceCount]);
		System.out.println("TruePositiveFrameElementIDrefCount: \t" + result[idxTruePositiveFrameElementIDrefCount]);
		//System.out.println("TruePositiveFrameElementIDrefCount_check: \t" + resultValues[idxTruePositiveFrameElementIDrefCount_check]);
		System.out.println("GoldFrameElementIDrefCount: \t" + result[idxGoldFrameElementIDrefCount]);
		System.out.println("ClassyFrameElementIDrefCount: \t" + result[idxClassyFrameElementIDrefCount]);
		System.out.println();
		System.out.println("TruePositiveFrameElementCount: \t" + result[idxTruePositiveFrameElementCount]);
		//System.out.println("TruePositiveFrameElementCount_check: \t" + resultValues[idxTruePositiveFrameElementCount_check]);
		System.out.println("GoldFrameElementCount: \t" + result[idxGoldFrameElementCount]);
		System.out.println("ClassyFrameElementCount: \t" + result[idxClassyFrameElementCount]);
		System.out.println();
		System.out.println("TruePositiveFrameElementIDrefCountNameIndependent: \t" + result[idxTruePositiveFrameElementIDrefCountNameIndependent]);
		System.out.println();
		System.out.println("F-Measure (IDrefs): " + statistic[idxIDrefsFMeasure]);
		System.out.println("F-Measure (FEs): " + statistic[idxFEsFMeasure]);
		System.out.println("F-Measure (IDrefsNI): " + statistic[idxIDrefsNIFMeasure]);
	}

	public static String getCaption() {
	    
		return "Sentence Count (overall)"
			+ Const.splitOutValStats + "Frame Count (overall)"
			+ Const.splitOutValStats + "FrameElement Count (overall)"
			+ Const.splitOutValStats + "FrameElementIDref Count (oveall)"
			
			+ Const.splitOutValStats + "TruePositive FrameElement IDref Count"
			+ Const.splitOutValStats + "Classy FrameElement IDref Count"
			+ Const.splitOutValStats + "GoldStandard FrameElement IDref Count"
			
			+ Const.splitOutValStats + "TruePositive FrameElement Count"
			+ Const.splitOutValStats + "Classy FrameElement Count"
			+ Const.splitOutValStats + "GoldStandard FrameElement Count"
			
			+ Const.splitOutValStats + "TruePositive FrameElement IDref NameIndependent Count"
			
			+ Const.splitOutValStats + "Unclassified Sentence Count (Not found)"
			+ Const.splitOutValStats + "Unclassified Sentence Count (No annotation)"
			
                        + Const.splitOutValStats + "Unclassified Frame Count (Sentence not found)"
                        + Const.splitOutValStats + "Unclassified Frame Count (Sentence no annotation)"
                        + Const.splitOutValStats + "Unclassified Frame Count (Targetlemma not found)"
                        
                        + Const.splitOutValStats + "Unclassified FrameElement Count (Sentence not found)"
			+ Const.splitOutValStats + "Unclassified FrameElement Count (Sentence no annotation)"
			+ Const.splitOutValStats + "Unclassified FrameElement Count (Targetlemma not found)"
			
			+ Const.splitOutValStats + "Unclassified FrameElement IDref Count (Sentence not found)"
			+ Const.splitOutValStats + "Unclassified FrameElement IDref Count (Sentence no annotation)"
			+ Const.splitOutValStats + "Unclassified FrameElement IDref Count (Targetlemma not found)"
			
			+ Const.splitOutValStats + "IDrefs FMeasure"
			+ Const.splitOutValStats + "IDrefs Precision"
			+ Const.splitOutValStats + "IDrefs Recall"
				
			+ Const.splitOutValStats + "FEs FMeasure"
			+ Const.splitOutValStats + "FEs Precision"
			+ Const.splitOutValStats + "FEs Recall"
				
			+ Const.splitOutValStats + "IDrefs NIFMeasure"
			+ Const.splitOutValStats + "IDrefs NIPrecision"
			+ Const.splitOutValStats + "IDrefs NIRecall";
	}

	@Override
	public String toString() {
		String temp = Long.toString(result[0]);
		
		for (int i = 1; i < result.length; i++) {
				temp += Const.splitOutValStats + Long.toString(result[i]);
		}

		for (int i = 0; i < statistic.length; i++) {
		    temp += Const.splitOutValStats + statistic[i];
		}
		
		return temp;
	}
}
