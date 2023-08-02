package test;

import scheduler.*;
import tasks.*;

import java.io.File;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import gui.*;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		testWithoutGUI();
		
		//testWithGUI();
	}
	
	private static void testWithGUI() {
		//example from the referenced java documentation
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
				Frame frame = new Frame();
		    }
		});
	}

	public static void testWithoutGUI() throws InterruptedException {
		// Second parameter is scheduling algorithm: false is for priority scheduling, true is for FIFO scheduling
		TaskScheduler scheduler = new TaskScheduler(15, true);
				
		// First string is name of the task, second string is the starting time and third is the end time when task should be completed or terminated.
		MyTask t1 = new Task("task 1", "26-07-2023 21:22:25", "26-07-2023 21:22:27"), t2 = new Task("task 2"), t3 = new Task("task 3");
		MyTask t4 = new Task("task 4", 5), t5 = new Task("task 5", 2, 3);
		
		scheduler.addTask(t1, false); scheduler.addTask(t2, false); scheduler.addTask(t3, true); // true for start immediately
		scheduler.addTask(t4, true); scheduler.addTask(t5, true);
		
		scheduler.start();
		
		Thread.sleep(2000);
		scheduler.addTask(new Task("task 6"), false);
		Thread.sleep(2000);
		scheduler.startTask("task 2");
		scheduler.pauseTask("task 4");
		scheduler.stopTask("task 3");
		Thread.sleep(30_000);
		scheduler.startTask("task 4");
		
		// Create histogram task
		LinkedList<File> images = new LinkedList<>();
		images.add(new File("1.jpeg")); images.add(new File("2.png")); images.add(new File("3.jfif")); images.add(new File("4.jfif"));
		images.add(new File("5.jfif"));
		MyTask histogramTask = new HistogramEqualizationTask("histogram task 7");
		scheduler.copyFiles(histogramTask, images);
		scheduler.addTask(histogramTask, true);
		
		Thread.sleep(30_000);
		scheduler.shutDown();
		
		scheduler.waitAllWorkerThreads();
	}

}
