package tasks;
import scheduler.*;
import gui.*;

import java.util.Date;
import java.util.LinkedList;
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import exceptions.TimeFormatException;

public abstract class MyTask implements Runnable, Serializable, Comparable<MyTask> {
	protected String name, startingDateTime, deadLine;		// 17-07-2023 21:36:14;
	protected boolean pause, terminated;
    private int priority = 0, totalExecutionTime = 0; 		// totalExecutionTime is in seconds
    private transient CustomTableModel tableModel;
    private int progress = 0, row;
    private long startingTime = 0;
	private LinkedList<File> inputFiles = new LinkedList<>();
	protected File outputFolder;
	// variables for progress bar
	protected int i = 0, total = 6;
	
	public MyTask(String name) {
		this.name = name;
	}
	
	public MyTask(String name, String startingDateTime) {
		this.name = name;
		try {
			if (checkTimeFormat(startingDateTime))
				this.startingDateTime = startingDateTime.trim();
		} catch (TimeFormatException e) {
			System.out.println(e);
		}
	}
	
	public MyTask(String name, String startingDateTime, String deadLine) {
		this.name = name;
		try {
			if (checkTimeFormat(startingDateTime))
				this.startingDateTime = startingDateTime.trim();
			if (checkTimeFormat(deadLine))
				this.deadLine = deadLine;
		} catch (TimeFormatException e) {
			System.out.println(e);
		}
	}
	
	public MyTask(String name, int... values) {
		this.name = name;
		if (values.length == 1) 
    		priority = values[0];
    	else if (values.length == 2) {
    		priority = values[0];
    		totalExecutionTime = values[1];
    	}
	}
	
	public MyTask(String name, String startingDateTime, int... values) {
		this.name = name;
		try {
			if (checkTimeFormat(startingDateTime))
				this.startingDateTime = startingDateTime.trim();
		} catch (TimeFormatException e) {
			System.out.println(e);
		}
		
		if (values.length == 1) 
    		priority = values[0];
    	else if (values.length == 2) {
    		priority = values[0];
    		totalExecutionTime = values[1];
    	}
	}
	
	public MyTask(String name, String startingDateTime, String deadLine, int... values) {
		this.name = name;
		try {
			if (checkTimeFormat(startingDateTime))
				this.startingDateTime = startingDateTime.trim();
			if (checkTimeFormat(deadLine))
				this.deadLine = deadLine.trim();
		} catch (TimeFormatException e) {
			System.out.println(e);
		}
		
		if (values.length == 1) 
    		priority = values[0];
    	else if (values.length == 2) {
    		priority = values[0];
    		totalExecutionTime = values[1];
    	}
	}
	
	public void setPause(boolean p) {
		pause = p;
	}
	
	public boolean getTerminated() {
		return terminated;
	}
	
	public String getStartingTime() {
		return startingDateTime;
	}
	
	public String getName() {
		return name;
	}
	
	public LinkedList<File> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(LinkedList<File> selectedImages) { 
		inputFiles = selectedImages;
		total = inputFiles.size();
	}
	
	public void setOutputFolder(File output) {
		outputFolder = output;
	}
	
	public CustomTableModel getTableModel() {
		return tableModel;
	}
	
	public void setRow(int row) {
		this.row = row;
	}

	public int getProgress() {
		return progress;
	}
	
	protected void updateProgress() {
    	progress = ((int) (i*(100f/total)));
		tableModel.updateProgress(row, progress);
	}
	
	private boolean checkTimeFormat(String s) throws TimeFormatException {
		if (s.equals("")) {
			s = null;
			return false;
		}
		
    	String[] parts = s.split(" ");
    	if (parts.length < 2)
    		throw new TimeFormatException();
    	
    	String date = parts[0], time = parts[1];
    	parts = date.split("-");
    	if (parts.length < 3)
    		throw new TimeFormatException();
    	if (Integer.parseInt(parts[0]) < 1 || Integer.parseInt(parts[0]) > 31)
    		throw new TimeFormatException();
    	if (parts[1].length() < 2 || Integer.parseInt(parts[1]) < 1 || Integer.parseInt(parts[1]) > 12)
    		throw new TimeFormatException();
    	if (Integer.parseInt(parts[2]) < 2023)
    		throw new TimeFormatException();
    	
    	parts = time.split(":");
    	if (parts.length < 3)
    		throw new TimeFormatException();
    	if (Integer.parseInt(parts[0]) < 0 || Integer.parseInt(parts[0]) > 23)
    		throw new TimeFormatException(); 
    	if (Integer.parseInt(parts[1]) < 0 || Integer.parseInt(parts[1]) > 59)
    		throw new TimeFormatException();
    	if (Integer.parseInt(parts[2]) < 0 || Integer.parseInt(parts[2]) > 59)
    		throw new TimeFormatException();
    	
    	if (compareTime(s))
			throw new TimeFormatException();
    	
    	return true;
	}
	
	@Override
	public void run() {
        startingTime = System.currentTimeMillis();

		while(true) {
			checkTotalExecutionTime();
			checkDeadLine();
			if (pause)
				break;
			
			try {
				logic();
				i++;
		    	updateProgress();
		    	Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int compareTo(MyTask t) {
		if (this.priority < t.priority)
			return 1;
		if (this.priority > t.priority)
			return -1;
		return 0;
	}
	
	private void checkTotalExecutionTime() {
		if (totalExecutionTime > 0 && (System.currentTimeMillis() - startingTime)/1000 > totalExecutionTime) {
    		System.out.println("\t "+name+" is terminated because time is run out!");
    		terminated = true;
    		pause = true;
    	}
	}

	private void checkDeadLine() {
		if (deadLine != null)
			if (compareTime(deadLine)) {
				System.out.println("\t "+name+" is terminated because reached dead line!");
				terminated = true;
	    		pause = true;
			}		
	}
	
	private boolean compareTime(String string) {
		// This method return true if passed time is expired (equal or less then current time).
		SimpleDateFormat sdf =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
		try {
			Date currentTime =  sdf.parse(TimeThread.getCurrentTime());
			Date time = sdf.parse(string);
			if(time.compareTo(currentTime) <= 0) // expired
				return true;
			else 
				return false;
		} 
		catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String toString() {
		return name;
	}
	
	public void setTableModel(CustomTableModel model) {
		tableModel = model;
	}
	
	public abstract void logic();

}
