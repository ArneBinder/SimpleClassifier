package Classifier.bean;

/**
 * Created by Arne on 22.01.14.
 */
public class Exceptions {
    public static class SRLException extends Exception {
	private static final long serialVersionUID = -8114236520060873531L;

	SRLException(String message) {
	    super(message);
	}
    }

    public static class IDrefNotInSentenceException extends SRLException {
	private static final long serialVersionUID = 7278986165517473377L;

	IDrefNotInSentenceException(String message) {
	    super(message);
	}
    }

    public static class FeatureTypeNotFoundException extends SRLException {
	private static final long serialVersionUID = 8542858231316697802L;

	FeatureTypeNotFoundException(String message) {
	    super(message);
	}
    }

    public static class RootNotInPathException extends SRLException {
	private static final long serialVersionUID = -2633669124549081061L;

	RootNotInPathException(String message) {
	    super(message);
	}
    }

    public static class FeatureValueNotCalculatedException extends SRLException {
	private static final long serialVersionUID = -4119063161178541984L;

	public FeatureValueNotCalculatedException(String message) {
	    super(message);
	}
    }

    public static class NoTargetsException extends SRLException {
	private static final long serialVersionUID = -7578337315534309826L;

	NoTargetsException(String message) {
	    super(message);
	}
    }

}
