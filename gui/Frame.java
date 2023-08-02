package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import scheduler.TaskScheduler;
import tasks.*;

public class Frame extends JFrame implements MouseListener {
	private TaskScheduler scheduler;
	private JPanel southPanel;
	private JButton b1, loadButton, createTaskButton, createHistogramTaskButton, startButton, shutdownButton, saveButton;
	private JScrollPane scroll;
	private JTable table;
	private CustomTableModel tableModel;
	boolean started = false;
	
	public Frame() {
		super("TASK SCHEDULER");		
		createCentralPanel();	
		b1 = new JButton("CREATE SCHEDULER");
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] options = new String[] {"FIFO", "Priority scheduling"};
			    int response = JOptionPane.showOptionDialog(null, "Choose scheduling alghoritm:", "Creating task scheduler",
			        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
			        null, options, options[0]);
			    
			    if(response == -1)
			    	return;
			    
			    if(response == 0) 
			    	createScheduler(true);
			    else
			    	createScheduler(false);
			    
			    if(scheduler != null) {
			    	b1.removeActionListener(b1.getActionListeners()[0]);
			    	createTaskButtons();
			    	createSouthPanel();
			    }
			}
		});
		loadButton = new JButton("LOAD SCHEDULER");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scheduler = TaskScheduler.deserialize();
				tableModel.setScheduler(scheduler);
				createTaskButtons();
				createSouthPanel();
				LinkedList<MyTask> allTasks = scheduler.getAllTasks();
				allTasks.sort(Comparator.comparing(MyTask::getName));
				for(MyTask task : allTasks) {
					task.setTableModel(tableModel);
					task.setRow(tableModel.addRow(new Object[] {task, task.getProgress()}));
				}
				TaskScheduler.counter = Integer.parseInt(allTasks.getLast().getName().split(" ")[1]);
			}
		});
		
		southPanel = new JPanel(new GridLayout(0,2));
		southPanel.add(b1); southPanel.add(loadButton);
		add(BorderLayout.SOUTH, southPanel);
		
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void createTaskButtons() {
		CreateTaskActionListener taskListener = new CreateTaskActionListener(this);
		createTaskButton=new JButton("Create simple task");		
		createTaskButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		createTaskButton.setBackground(Color.ORANGE);
		createTaskButton.setFont(new Font("SansSerif", Font.PLAIN, 20));
		createTaskButton.addActionListener(taskListener);
		
		createHistogramTaskButton =new JButton("Create histogram task");		
		createHistogramTaskButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		createHistogramTaskButton.setBackground(Color.ORANGE);
		createHistogramTaskButton.setFont(new Font("SansSerif", Font.PLAIN, 20));
		createHistogramTaskButton.addActionListener(taskListener);
		
		JPanel northPanel = new JPanel(new GridLayout(0,2));
		northPanel.add(createTaskButton); northPanel.add(createHistogramTaskButton);
		add(BorderLayout.NORTH, northPanel);
	}
	
	private void createCentralPanel() {
		tableModel = new CustomTableModel();
		table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setCellRenderer(new ProgressBarRenderer());
        table.getTableHeader().setBackground(Color.green);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.black,2));
        table.addMouseListener(this);
		
		scroll=new JScrollPane();
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	
		scroll.setViewportView(table);
		scroll.setBorder(BorderFactory.createLineBorder(Color.black,3));
		
		add(BorderLayout.CENTER, scroll);			
	}
	
	private void createSouthPanel() {
		remove(b1); remove(loadButton); remove(southPanel);
		
		southPanel = new JPanel(new GridLayout(0,3));
		southPanel.add(startButton=new JButton("Start scheduler")); southPanel.add(shutdownButton=new JButton("Shutdown scheduler")); 
		southPanel.add(saveButton=new JButton("Save scheduler"));
		startButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		startButton.setBackground(Color.green);
		startButton.setFont(new Font("SansSerif",Font.PLAIN, 15));
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (started)
					return;
				scheduler.start();
				started = true;
			}
		});
		shutdownButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		shutdownButton.setBackground(Color.red);
		shutdownButton.setFont(new Font("SansSerif",Font.PLAIN, 15));
		shutdownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!started)
					return;
				started = false;
				createTaskButton.setEnabled(false);
				createHistogramTaskButton.setEnabled(false);
				remove(startButton); remove(shutdownButton); remove(saveButton); remove(southPanel);
				b1.setText("SCHEDULER IS SHUTDOWN!");
				b1.setBackground(Color.gray);
				if (b1.getActionListeners().length > 0)
					b1.removeActionListener(b1.getActionListeners()[0]);
				add(BorderLayout.SOUTH, b1);
				validate();
				new Thread() {
				    public void run() {
				    	scheduler.shutDown();
						scheduler = null;
				    }
				}.start();
		    	JOptionPane.showMessageDialog(table, "The scheduler is shutdown!");
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!started)
					return;
				scheduler.serialize();
		    	JOptionPane.showMessageDialog(table, "The scheduler is saved!");
			}
		});
		saveButton.setFont(new Font("SansSerif",Font.PLAIN, 15));
		
		add(BorderLayout.SOUTH, southPanel);
		validate();
	}
	
	private void createScheduler(boolean FIFO) {
		int n;
		try {
			n = Integer.parseInt(JOptionPane.showInputDialog("Enter number of threads:"));
		}
		catch (NumberFormatException e) {
			return;
		}
		
		scheduler = new TaskScheduler(n, FIFO);	
		tableModel.setScheduler(scheduler);
	}
	
	public void createTask(String start, String startingTime, String deadLine, String priority, String totalExecutionTime, LinkedList<File> selectedImages) {
		int prior, totalTimeOfExecution;
		try {
			prior = Integer.parseInt(priority);
		}
		catch (NumberFormatException e) {
			prior = 0;
		}
		try {
			totalTimeOfExecution = Integer.parseInt(totalExecutionTime);
		}
		catch (NumberFormatException e) {
			totalTimeOfExecution = 0 ;
		}
				
		MyTask task;
		if (selectedImages != null) {
			task = new HistogramEqualizationTask("Histogram task "+(++TaskScheduler.counter), startingTime, deadLine, prior, totalTimeOfExecution);
			scheduler.copyFiles(task, selectedImages);
		}
		else
			task = new Task("Task "+(++TaskScheduler.counter), startingTime, deadLine, prior, totalTimeOfExecution);		
		
		if (!startingTime.equals(""))
			scheduler.addTask(task, false);
		else if (start.equals("") || start.equals("true"))
			scheduler.addTask(task, true);
		else 
			scheduler.addTask(task, false);
		
		task.setTableModel(tableModel);
		task.setRow(tableModel.addRow(new Object[] {task, task.getProgress()}));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row >= 0 && col >= 0) {
            // Handle the action here, for example, display a message with the selected cell's data
        	MyTask task = (MyTask) table.getValueAt(row, 0);
        	String[] options = new String[] {"START/RESUME TASK", "PAUSE TASK", "STOP TASK", "REMOVE TASK"};
		    int response = JOptionPane.showOptionDialog(null, "Choose action:", "Managing " +task.getName(),
		        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		    if (response == -1)
		    	return;
		    if (!started) {
		    	JOptionPane.showMessageDialog(table, "This option is unavailable because scheduler is not started!");
		    	return;
		    }
		    
		    if (response == 0) {
		    	if (scheduler.startTask(task.getName()) == null)
		    		JOptionPane.showMessageDialog(table, task.getName() + " cann't start/resume because it is not paused!");
		    }
		    else if (response == 1) {
		    	if (scheduler.pauseTask(task.getName()) == null)
		    		JOptionPane.showMessageDialog(table, task.getName() + " cann't be paused because it is not in the running state!");
		    }
		    else if (response == 2) {
		    	if (!scheduler.stopTask(task.getName()))
		    		JOptionPane.showMessageDialog(table, task.getName() + " cann't be stopped because it is not in the running state!");    
		    }
		    else if (task.getProgress() == 100 || scheduler.findTerminatedTask(task)) {
		    	tableModel.deleteRow(row);
		    	scheduler.deleteCompletedTerminatedTask(task);
		    }
		    else
	    		JOptionPane.showMessageDialog(table, task.getName() + " cann't be removed from the table because it is not completed/terminated!");    		   
        }
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	@Override
	public void dispose() {
		if (scheduler != null && started) {
			scheduler.serialize();
	    	JOptionPane.showMessageDialog(table, "Scheduler is saved!");
		}
	    super.dispose();
	    System.exit(0);
	}
}
