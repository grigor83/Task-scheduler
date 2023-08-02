package scheduler;

import java.io.Serializable;
import java.util.Queue;

import tasks.MyTask;

public class WorkerThread extends Thread implements Serializable {
	private String name;
	private boolean running = true;
	private Queue<MyTask> tasks, completedTerminatedTasks;
	private MyTask task;
	 // This lock is used to allow the task to complete; the main thread cann't interrupt worker thread while executing current task.
	private MyObject taskLock;
	
	public WorkerThread(int i, Queue<MyTask> tasks, Queue<MyTask> completedTerminatedTasks) {
		name = i + " ";
		this.tasks = tasks;
		this.completedTerminatedTasks = completedTerminatedTasks;
		taskLock = new MyObject();
	}
	
	public void run() {
		while(running) {
			if (task == null) {	// this line is added because GUI application, when trying to load scheduler, how current task could be continued.
				synchronized (tasks) {
					while(tasks.isEmpty())
						try {
							tasks.wait();				// Wait for a task to be available
						} catch (InterruptedException e) {
							System.out.println("Thread "+name+" interrupted in the worker thread!");
							if (!running)
	                        	break;
						}
					// Waiting is over.
					synchronized (taskLock) {
	                    task = tasks.poll();
	                	if (task != null) {
	                    	System.out.println("\t Thread "+ name + " pooled out "+task.getName());
	                    	tasks.notifyAll(); 						// Notify the main thread to check if queue is empty after polling current task.
	                	}
					}
				}
			}
			//////  End of synchronization block by tasks queue  ////////////////
            // Start of execution the current task
            if (task != null) {
            	task.run();
            	if (task.getTerminated()) {
            		synchronized (completedTerminatedTasks) {
						completedTerminatedTasks.offer(task);
					}
            		if (task.getTableModel() != null)
            			task.getTableModel().fireTableDataChanged();
            	}
            	synchronized (taskLock) {
                	task=null;
				}
            }		
		}
		// The main thread called shutdown and this worker thread is break while loop because running flag set to false. 
				System.out.println("Thread "+name+" stopped!");
	}
	
	public MyTask findTask(String name) {
		synchronized (taskLock) {
			if (task != null && task.getName().equals(name))
				return task;
			else
				return null;
		}
	}
	
	public MyTask getTask() {
		return task;
	}

	public void stopThread() {
	    // This method set running flag to false and thread will stop working AFTER executing current task. 
		running = false;
		// To stopping threads which are in waiting state, we call interrupt method, BUT only if the thread is not executing task.
	    // If thread is working on the task, interrupt() would not be called. Thread will be stopped after executing task, when she checks running flag. 
		// This mechanism enables current task to complete completely.
		synchronized (taskLock) {
			if (task == null)
				interrupt();
		}
	}
	
	private class MyObject extends Object implements Serializable {
		
	}
}
