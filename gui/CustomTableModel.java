package gui;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import scheduler.TaskScheduler;
import tasks.MyTask;

public class CustomTableModel extends AbstractTableModel {
	private String[] columnNames = {"Task Name", "Progress (%)"};
    private List<Object[]> data = new ArrayList<>();
    private TaskScheduler scheduler;
    
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row)[col];
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    // Custom method to add a new row to the table
    public int addRow(Object[] rowData) {
        if (rowData.length != getColumnCount()) {
            throw new IllegalArgumentException("Row data length must match the number of columns.");
        }

        data.add(rowData);
        int newRow = data.size() - 1;
        fireTableRowsInserted(newRow, newRow);
        return newRow;
    }
    
    // Custom method to delete a row from the table
    public void deleteRow(int row) {
    	if (row < 0 || row >= data.size()) {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }

        data.remove(row);
        for(int i=0; i<data.size(); i++) {
        	MyTask task = (MyTask) data.get(i)[0];
        	task.setRow(i);
        }
        fireTableRowsDeleted(row, row);
    }
    
    // Custom method to update progress for a row
    public void updateProgress(int row, int progress) {
        if (row < 0 || row >= data.size()) {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }

        data.get(row)[1] = progress;
        fireTableCellUpdated(row, 1); // Notify the table that the cell at row, column 1 has been updated
    }
    
    public void setScheduler(TaskScheduler scheduler) {
    	this.scheduler = scheduler;
    }
    
    public TaskScheduler getScheduler() {
    	return scheduler;
    }

}

class ProgressBarRenderer extends DefaultTableCellRenderer implements Serializable {
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public ProgressBarRenderer() {
        super();
        progressBar.setStringPainted(true);
        progressBar.setBackground(Color.orange);
        progressBar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int progress = (int) value;
        progressBar.setValue(progress);
        CustomTableModel tableModel = (CustomTableModel) table.getModel();
    	MyTask task = (MyTask) table.getValueAt(row, 0);
        
        if (progress >= 100)
        	progressBar.setForeground(Color.gray);
        else if (tableModel.getScheduler().findTerminatedTask(task))
        	progressBar.setForeground(Color.red);
        else
            progressBar.setForeground(Color.blue);
        return progressBar;
    }
}