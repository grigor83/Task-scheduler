package scheduler;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;

import tasks.MyTask;

import java.util.*;

public class TimeThread extends Thread implements Serializable {
	private TaskScheduler scheduler;
	private Queue<MyTask> waitingTasks;
	private boolean running;
	
	public TimeThread(TaskScheduler scheduler, Queue<MyTask> waitingTasks) {
		this.scheduler = scheduler;
		this.waitingTasks = waitingTasks;
		running = true;
	}
	
	public void run() {
		while(running) {
			startTasksByTime(getCurrentTime());
			
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				System.out.println("\t TimeThread interrupted!");
			}
		}
		
		System.out.println("\t TimeThread stopped!");
	}
	
	private void startTasksByTime(String time) {
		List<MyTask> startingTasks = new LinkedList<>();

		synchronized (waitingTasks) {
			for(MyTask task : waitingTasks) {
				String taskTime = task.getStartingTime();
				if (taskTime != null && taskTime.equals(time))
					startingTasks.add(task);
			}				
		}
		startingTasks.stream().forEach(task -> scheduler.startTask(task.getName()));
		startingTasks.clear();
	}
	
	
	public static String getCurrentTime() {
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	    String formattedDate = formatter.format(date);
		return formattedDate.trim();
	}
	
	public void stopTimeThread() {
		running = false;
		this.interrupt();
	}
	
}