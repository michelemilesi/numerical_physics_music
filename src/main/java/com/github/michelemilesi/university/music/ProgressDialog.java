package com.github.michelemilesi.university.music;

import java.awt.*;
import javax.swing.*;

public class ProgressDialog extends JDialog implements Monitor {

	public class Tasker implements Runnable {
		Filter tgt;

		public Tasker(Filter tgt) {
			this.tgt = tgt;
		}

		public void run() {
			String msg;
			int pos, max, val;
		
			msg = tgt.getMessage();
			if (msg != null) message.setText(msg);
			
			pos = 0;
			for (pos = 0; (pos < filters.length) && (!tgt.equals(filters[pos])); pos++) {}
			
			max = tgt.getMaximum();
			val = tgt.getCurrent();
			max = Math.max(max, val);
			
			if (max == 0) max = 100;
			
			taskPb.setValue(val * 100  / max);
			mainPb.setValue(100 * pos + (val * 100 / max));
		}
	}

	private JProgressBar mainPb;
	private JProgressBar taskPb;
	private JLabel message;
	private Filter[] filters;
	private JButton chiudiBtn;
		
	public ProgressDialog(Frame frame, Filter[] filters)  {
		super(frame, "Applicazione filtri in corso", true);
		
		for (int i = 0; i < filters.length; i++) {
			filters[i].setMonitor(this);
		}
		
		this.filters = filters;
		
		mainPb = new JProgressBar(0, 100 * filters.length);
		mainPb.setStringPainted(true);
		taskPb = new JProgressBar(0, 100);
		taskPb.setStringPainted(true);
		message = new JLabel("Applicazione dei filtri in corso");
			
		initGUI();
	}
	
	private void initGUI()  {
		Container contPane;
		
		contPane = getContentPane();
		
		contPane.setLayout(new BoxLayout(contPane, BoxLayout.Y_AXIS));
		
		contPane.add(message);
		contPane.add(mainPb);
		contPane.add(taskPb);
		
		pack();
	}
	
	/*
	 * @see Observer#update(Observable, Object)
	 */
	public void hasChanged(Filter tgt) {
		Tasker task = new Tasker(tgt);
		try {
			SwingUtilities.invokeLater(task);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void dispose() {
		for (int i = 0; i < filters.length; i++) {
			filters[i].removeMonitor();
		}
		super.dispose();
	}

	/*
	 * @see Component#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		Dimension d, sd;

		d = getSize();
		sd = Toolkit.getDefaultToolkit().getScreenSize();
		
		setLocation((sd.width - d.width) /2,
					(sd.height - d.height) / 2);
		
		super.setVisible(visible);
	}

}