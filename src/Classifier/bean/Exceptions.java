package Classifier.bean;

/**
 * Created by Arne on 22.01.14.
 */
public class Exceptions {
	public static class SRLException extends Exception {
		SRLException(String message){
			super(message);
		}
	}

	public static class IDrefNotInSentenceException extends SRLException {
		IDrefNotInSentenceException(String message){
			super(message);
		}
	}

	public static class FeatureTypeNotFoundException extends SRLException {
		FeatureTypeNotFoundException(String message){
			super(message);
		}
	}

	public static class RootNotInPathException extends SRLException {
		RootNotInPathException(String message){
			super(message);
		}
	}

	public static class FeatureValueNotCalculatedException extends SRLException {
		public FeatureValueNotCalculatedException(String message){
			super(message);
		}
	}

	public static class NoTargetsException extends SRLException {
		NoTargetsException(String message){
			super(message);
		}
	}

}
