package exceptions;

public class TimeFormatException extends Exception {
	private final static String TIME_ERROR = "Incorrect time format!";
	
	public TimeFormatException() {  
		    super(TIME_ERROR);  
	}  
	
}
