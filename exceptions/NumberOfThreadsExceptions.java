package exceptions;

import scheduler.TaskScheduler;

public class NumberOfThreadsExceptions extends Exception {
	private final static String NUMBER_OF_THREADS_ERROR = "Incorrect number of threads! Setting number of threads to maximum number: "+ TaskScheduler.MAX_THREADS+"!";
	
	public NumberOfThreadsExceptions() {  
		    super(NUMBER_OF_THREADS_ERROR);  
	}  
}
