package tasks;

import java.text.SimpleDateFormat; 
import java.util.Date;

public class Task extends MyTask   
{
    public Task(String name) {
		super(name);
	}
    
    public Task(String name, String startingTime) {
		super(name, startingTime);
	}
    
	public Task(String name, String startingDateTime, String deadLine) {
		super(name, startingDateTime, deadLine);
	}
	
	public Task(String name, int... values) {
		super(name, values);
	}   
	
	public Task(String name, String startingDateTime, int... values) {
		super(name, startingDateTime, values);
	}  
	
	public Task(String name, String startingDateTime, String deadLine, int... values) {
		super(name, startingDateTime, deadLine, values);
	} 
      
    // Prints task name and sleeps for 1s
    // This Whole process is repeated 5 times
    public void logic() {
    	if (i==6) {
        	System.out.println(name+" complete");
    		pause = true;
    		terminated = true;
    		return;
    	}
    	
    	if (i==0)
        {
//            Date d = new Date();
//            SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
            	//System.out.println("Initialization Time for "
            	//   + name +" = " +ft.format(d));   
            //prints the initialization time for every task 
        }
        else
        {
            Date d = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
            System.out.println("Executing Time for "+ name +" = " +ft.format(d));   
            // prints the execution time for every task 
        }
    }
}

