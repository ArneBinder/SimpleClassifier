package Classifier.bean;

/**
 * Created by Arne on 22.01.14.
 */
public class ValidateResult {

	public final static int idxSentenceCount = 0;

	//private final static int idxFrameCount = 1;


	//private final static int idxCorrectSentenceCount = 3;
	//private final static int idxCorrectFrameCount = 4;
	public final static int idxTruePositiveFrameElementIDrefCount = 1;
	public final static int idxTruePositiveFrameElementIDrefCount_check = 2;
	public final static int idxClassyFrameElementIDrefCount = 3;
	public final static int idxGoldFrameElementIDrefCount = 4;

	public final static int idxTruePositiveFrameElementCount = 5;
	public final static int idxTruePositiveFrameElementIDrefCountNameIndependent = 6;
	public final static int idxClassyFrameElementCount = 7;
	public final static int idxGoldFrameElementCount = 8;
	public final static int idxUnclassifiedSentenceCount = 9;
	public final static int idxIDrefsFMeasure = 10;
	public final static int idxFEsFMeasure = 13;
	public final static int idxIDrefsNIFMeasure = 16;

	private long[] result;
	private final static double roundFactor = 1000000.0;

	private final static int resultSize = 19;

	public ValidateResult() {
		result = new long[resultSize];
	}

	public ValidateResult(long[] result) {
		this.result = new long[resultSize];
		assert (result.length == 10);
		//int pos = 0;
		System.arraycopy(result, 0, this.result, 0, result.length);
		//pos += result.length;
		System.arraycopy(getStats(idxTruePositiveFrameElementIDrefCount, idxClassyFrameElementIDrefCount, idxGoldFrameElementIDrefCount), 0, this.result, idxIDrefsFMeasure, 3);
		//pos += 3;
		System.arraycopy(getStats(idxTruePositiveFrameElementCount, idxClassyFrameElementCount, idxGoldFrameElementCount), 0, this.result, idxFEsFMeasure, 3);
		//pos += 3;
		System.arraycopy(getStats(idxTruePositiveFrameElementIDrefCountNameIndependent, idxClassyFrameElementIDrefCount, idxGoldFrameElementIDrefCount), 0, this.result, idxIDrefsNIFMeasure, 3);
	}

	public long[] getResult() {
		return result;
	}

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
		System.out.println("F-Measure (IDrefs): " + result[idxIDrefsFMeasure] / roundFactor);
		System.out.println("F-Measure (FEs): " + result[idxFEsFMeasure] / roundFactor);
		System.out.println("F-Measure (IDrefsNI): " + result[idxIDrefsNIFMeasure] / roundFactor);
	}

	@Override
	public String toString() {
		String temp = Long.toString(result[0]);
		for (int i = 1; i < result.length; i++) {
			if (i < idxIDrefsFMeasure)
				temp += Const.splitOutValStats + Long.toString(result[i]);
			else
				temp += Const.splitOutValStats + Double.toString(((double) result[i]) / roundFactor);
		}
		return temp;
	}

	public static String getCaption() {
		return "SentenceCount"
				+ Const.splitOutValStats + "TruePositiveFrameElementIDrefCount"
				+ Const.splitOutValStats + "TruePositiveFrameElementIDrefCount_check"
				+ Const.splitOutValStats + "ClassyFrameElementIDrefCount"
				+ Const.splitOutValStats + "GoldFrameElementIDrefCount"
				+ Const.splitOutValStats + "TruePositiveFrameElementCount"
				+ Const.splitOutValStats + "TruePositiveFrameElementIDrefCountNameIndependent"
				+ Const.splitOutValStats + "ClassyFrameElementCount"
				+ Const.splitOutValStats + "GoldFrameElementCount"
				+ Const.splitOutValStats + "UnclassifiedSentenceCount"
				+ Const.splitOutValStats + "IDrefsFMeasure"
				+ Const.splitOutValStats + "IDrefsPrecision"
				+ Const.splitOutValStats + "IDrefsRecall"
				+ Const.splitOutValStats + "FEsFMeasure"
				+ Const.splitOutValStats + "FEsPrecision"
				+ Const.splitOutValStats + "FEsRecall"
				+ Const.splitOutValStats + "IDrefsNIFMeasure"
				+ Const.splitOutValStats + "IDrefsNIPrecision"
				+ Const.splitOutValStats + "IDrefsNIRecall";
	}


	private long[] getStats(int tpCount, int classyCount, int origCount) {

		double precision = result[tpCount] / (double) result[classyCount];
		double recall = result[tpCount] / (double) result[origCount];
		double fmeasure = 2.0 * precision * recall / (precision + recall);

		return new long[]{(long) (fmeasure * roundFactor), (long) (precision * roundFactor), (long) (recall * roundFactor)};
	}

	public double getFMeasure(int idx) {
		if (idx == 0)
			return ((double) result[idxIDrefsFMeasure]) / roundFactor;
		if (idx == 1)
			return ((double) result[idxFEsFMeasure]) / roundFactor;
		if (idx == 2)
			return ((double) result[idxIDrefsNIFMeasure]) / roundFactor;
		return -1.0;
	}

	public void addResult(ValidateResult validateResult) {
		for (int i = 0; i < result.length; i++) {
			result[i] += validateResult.getResult()[i];
		}
	}

	public void normalize(int count) {
		for (int i = 0; i < result.length; i++) {
			result[i] /= count;
		}

	}
}
