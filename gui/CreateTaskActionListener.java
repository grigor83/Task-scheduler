package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CreateTaskActionListener implements ActionListener {
	Frame frame;
	
	public CreateTaskActionListener(Frame frame) {
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		LinkedList<File> selectedImages = null;
		JButton button =  (JButton) e.getSource();
		if (button.getText().contains("histogram")) {
			selectedImages = chooseImages();
			if (selectedImages == null)
				return;
		}
		
		JTextField shouldStartTask = new JTextField();
		JTextField startingTime = new JTextField();
		JTextField deadLine = new JTextField();
		JTextField priority = new JTextField();
		JTextField totalExecutionTime = new JTextField();

		final JComponent[] inputs = new JComponent[] {
		        new JLabel("Start task immediately (true or false): "), shouldStartTask,
		        new JLabel("Starting date and time (17-07-2023 21:36:14): "), startingTime,
		        new JLabel("Deadline (17-07-2023 21:36:14): "), deadLine,
		        new JLabel("Priority: "), priority,
		        new JLabel("Total execution time (seconds): "), totalExecutionTime
		};
		int result = JOptionPane.showConfirmDialog(null, inputs, "Enter the arguments for the task:", JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION)
		    frame.createTask(shouldStartTask.getText().trim(), startingTime.getText().trim(), deadLine.getText().trim(), 
		    			priority.getText().trim(), totalExecutionTime.getText().trim(), selectedImages);
		else
		    System.out.println("User canceled / closed the dialog, result = " + result);
	}
	
	public LinkedList<File> chooseImages() {
		LinkedList<File> selectedFiles = null;
		JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // Enable multiple file selection
        chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(new File("C:\\"));
		chooser.setDialogTitle("Choose image to equalize");
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp");
        chooser.setFileFilter(imageFilter);
		
        while(true) {
        	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
        		if (selectedFiles == null)
        			selectedFiles = new LinkedList<>();
      
        		for (File file : chooser.getSelectedFiles()) {
                    System.out.println("Selected file: " + file.getAbsolutePath());
                    selectedFiles.add(file);
                }
    		}
        	else
        		break;
        	
        	int result = JOptionPane.showConfirmDialog(null, "Do you wish to choose more images?", null, JOptionPane.YES_NO_OPTION);
        	if (result == JOptionPane.NO_OPTION) 
        		break; 
        }
        
        return selectedFiles;
	}

}
