package scheduler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import exceptions.NumberOfThreadsExceptions;
import tasks.MyTask;

public class TaskScheduler implements Serializable {
	public final static int MAX_THREADS = 10;
	public final static File ROOT_DIR = new File("C:/Users/Igor/Desktop/TASK_SCHEDULER"),
							INPUT_DIR = new File (ROOT_DIR + File.separator + "input"),
							OUTPUT_DIR = new File (ROOT_DIR + File.separator + "output"),
							SCHEDULER_FILE = new File (ROOT_DIR + File.separator + "scheduler.bin");
	public 	static int counter = 0;
	private Queue<MyTask> tasks, waitingTasks, completedTerminatedTasks;
	private WorkerThread[] threads; 
	private TimeThread timeThread;
	
	public TaskScheduler(int numThreads, boolean FIFO) {
		createFileSystem();
		try {
			checkNumberOfThreads(numThreads);
		}
		catch (NumberOfThreadsExceptions e) {
			System.out.println(e);
			numThreads = MAX_THREADS;
		}
		threads = new WorkerThread[numThreads];
		
		if (FIFO)
			tasks = new LinkedList<>();
		else
			tasks = new PriorityQueue<>();
		waitingTasks = new LinkedList<>();		
		completedTerminatedTasks = new LinkedList<>();
		timeThread = new TimeThread(this, waitingTasks);
	}
	
	private void createFileSystem() {
		if (!ROOT_DIR.exists())
			ROOT_DIR.mkdir();
		if (!INPUT_DIR.exists())
			INPUT_DIR.mkdir();
		if (!OUTPUT_DIR.exists())
			OUTPUT_DIR.mkdir();
	}
	
	private void checkNumberOfThreads(int numThreads) throws NumberOfThreadsExceptions {
		if (numThreads<1 || numThreads > MAX_THREADS)
			throw new NumberOfThreadsExceptions();
	}

	public void start() {
		System.out.println("\t Threadpool is starting!");
		for (int i = 0; i < threads.length; i++) {
			if (threads[i] == null) // If user load scheduler from GUI, shoudln't create the new thread.
				threads[i] = new WorkerThread(i+1, tasks, completedTerminatedTasks);
            threads[i].start();
        }
		timeThread.start();
	}
	
	public void shutDown() {
		// Before shutdown, wait for all tasks to be pooled from the queue by workers thread. Only when the queue is empty, start shutdown.  
		synchronized (tasks) {
			while(!tasks.isEmpty())
				try {
					tasks.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		System.out.println("\n\t Starting shutdown of threadpool at "+ TimeThread.getCurrentTime()+ " . . .");
		// Then stop all threads.
		for(WorkerThread t : threads)
			t.stopThread();	
		timeThread.stopTimeThread();
	}
	
	public void serialize() {
		//createFile(SCHEDULER_FILE);
		try (FileOutputStream fileOut = new FileOutputStream(SCHEDULER_FILE);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
	        objectOut.writeObject(this);
	        System.out.println("Scheduler has been serialized.");
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	 }
	 
	public static TaskScheduler deserialize() {
	        try (FileInputStream fileIn = new FileInputStream(SCHEDULER_FILE);
	            ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
	            TaskScheduler scheduler = (TaskScheduler) objectIn.readObject();
	            System.out.println("Scheduler has been deserialized.");
	            return scheduler;
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	            return null;
	        }
	}

	public void waitAllWorkerThreads() throws InterruptedException {
		for(WorkerThread t : threads)
			t.join();
		timeThread.join();
		System.out.println("\t PROGRAM IS OVER!");		
	}
	

	public void addTask(MyTask task, boolean start) {
		task.setPause(!start);
		if(start) {
			synchronized (tasks) {
	            tasks.offer(task);
	            tasks.notifyAll(); // Notify the waiting worker thread that there is new task in the queue. 
	        }
		}
		else {
			synchronized (waitingTasks) {
				waitingTasks.offer(task);
			}
		}
	}
	
	public boolean findTerminatedTask(MyTask task) {
		//This method is only used in gui application, when trying to remove the terminated task from the table (not from scheduler).
		synchronized (completedTerminatedTasks) {
			if (completedTerminatedTasks.contains(task))
				return true;
			else
				return false;
		}
	}
	
	public MyTask startTask(String name) {  // Also for resuming paused task
		synchronized (waitingTasks) {
			Iterator<MyTask> iter = waitingTasks.iterator();
			while (iter.hasNext()) {
				MyTask t = iter.next();
				if (t.getName().equals(name)) {
					t.setPause(false);
					synchronized (tasks) {
						tasks.add(t);
						tasks.notifyAll();
					}
					iter.remove();
					System.out.println("\t "+t.getName()+" is just started/resumed!");
					return t;
				}
			}
		}
		System.out.println("\t "+ name + " cann't start/resume because it is not paused!");
		return null;
	}
	
	public MyTask pauseTask (String name) {
		for(WorkerThread thread : threads) {
			MyTask task = thread.findTask(name);
			if (task != null) {
				synchronized (waitingTasks) {
	        		task.setPause(true);
					waitingTasks.offer(task);
					System.out.println("\t "+task.getName()+" is paused!");
					return task;
				}
			}
		}
		System.out.println("\t "+name+" cann't be paused because it is not in the running state!");
		return null;
	}
	
	public boolean stopTask (String name) {  // Task must be in the running state. After stopping, the task cannot be continued.
		MyTask task = pauseTask(name);
		if (task != null) {
			synchronized (waitingTasks) {
				if (waitingTasks.remove(task)) {
					System.out.println("\t "+name+" has been terminated!");
					synchronized (completedTerminatedTasks) {
						completedTerminatedTasks.offer(task);
					}
					if (task.getTableModel() != null)
						task.getTableModel().fireTableDataChanged();
					return true;
				}
			}
		}
		System.out.println("\t "+name+" cann't be stopped bacuse it is not in the running state!");
		return false;
	}
	
	public LinkedList<MyTask> getAllTasks(){
		LinkedList<MyTask> list = new LinkedList<>();
		synchronized (tasks) {
			list.addAll(tasks);
		}
		synchronized (waitingTasks) {
			list.addAll(waitingTasks);
		}
		synchronized (completedTerminatedTasks) {
			list.addAll(completedTerminatedTasks);
		}
		for(WorkerThread thread : threads) {
			MyTask task = thread.getTask();
			if (task != null) 
				list.add(task);
		}
		
		return list;
	}
	
	public void deleteCompletedTerminatedTask(MyTask task) {
		synchronized (completedTerminatedTasks) {
			completedTerminatedTasks.remove(task);
		}
	}

	public void copyFiles(MyTask task, LinkedList<File> selectedImages) {
		// This method copies all selected images to input folder of the task scheduler.
		LinkedList<File> copies = new LinkedList<>();
		File folder = new File (INPUT_DIR+ File.separator + task.getName());
		folder.mkdir();
		
		for(File source : selectedImages) {
			File dest = new File(folder + File.separator + source.getName());
			try {
				Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				copies.add(dest);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		task.setInputFiles(copies);
		// Then, create folder with task name in output folder
		File output = new File (OUTPUT_DIR+ File.separator + task.getName());
		output.mkdir();
		task.setOutputFolder(output);
	}
	
}
