package com.github.michelemilesi.university.music;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


public class PitchShifterPanel extends AbsPanel implements ActionListener, ChangeListener {

	private JSlider scaleJS, stepJS, overlapJS;
	private String scale_str = "Scala %";
	private String step_str = "Frame Step";
	//private String overlap_str = "Overlap";
	private JCheckBox effetto_check;
	private JButton reset;
	private double default_scale = 1;   //  from 0.50 to 2.00
	private double scale;	
	private double decimali = 100;         //  = 1.00
	private double min = 0.5;
	private double max = 2;
	private int default_step = 2048;
	private int step;
	//private double default_overlap = 0.25;
	//private double overlap;

	public PitchShifterPanel() {
		attiva(false); visualizza(false);
		scale = default_scale;
		step = default_step;
		reset = new JButton("Reset");
		reset.setForeground(new Color(128,128,192));
		reset.addActionListener(this);
		effetto_check = new JCheckBox("Pitch Shifter",false);
		effetto_check.setForeground(new Color(128,0,255));
		effetto_check.addChangeListener(this);
		scaleJS = new JSlider(JSlider.HORIZONTAL, (int)(min*decimali), (int)(max*decimali), (int)(default_scale*decimali));
		stepJS = new JSlider(JSlider.HORIZONTAL, 512, 4096, default_step);
		//overlapJS = new JSlider(JSlider.HORIZONTAL, (int)(0*decimali), (int)(0.99*decimali), (int)(default_overlap*decimali));

		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		CompoundBorder cb = new CompoundBorder(eb, new EtchedBorder());
		setBorder(new CompoundBorder(cb, new EmptyBorder(8,8,8,8)));

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.gridwidth = c.RELATIVE;
		add(effetto_check,c);
		c.gridwidth = c.REMAINDER;
		add(reset,c);
		
		stepJS.addChangeListener(this);
		TitledBorder tb = new TitledBorder(new EtchedBorder());
		tb.setTitle(step_str + " = "+ stepJS.getValue());
		tb.setTitleColor(new Color(64,128,128));
		stepJS.setBorder(tb);
		add(stepJS,c);
		
		c.weighty = 2;
		c.fill = c.HORIZONTAL;
		c.gridheight = c.REMAINDER;

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0,1));
		addSlider(scale_str, scaleJS, p, this);

		add(p,c);
	}

	protected void init() {
		filtro.setParameter(PitchShifter.SCALE, scale);
		filtro.setParameter(PitchShifter.STEP, step);
		//filtro.setParameter(com.github.michelemilesi.university.music.PitchShifter.OVERLAP, overlap);
	}

	public void loadFilter(String description, String nome_filtro) throws Exception {
		try {
			Class cls = Class.forName(nome_filtro);
			Filter f = (Filter) cls.newInstance();
			if (f instanceof PitchShifter) {
				setFilter(f);
				setFilterName(f.getClass().getName());
				setDescription(description);
				visualizza(true);
			} else throw new InstantiationException();
		} catch (Exception e) {
			System.out.println("Errore di caricamento/istanziazione filtro: com.github.michelemilesi.university.music.PitchShifter");
			throw e;
		}
	}

	public void setParameter(String[] elenco) throws Exception {
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(reset)) {
			scale = default_scale;
			scaleJS.setValue((int)(default_scale*decimali));
			step = default_step;
			stepJS.setValue((int)(default_step));
			//overlap = default_overlap;
			//overlapJS.setValue((int)(default_overlap*decimali));
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JSlider) {
		    JSlider slider = (JSlider) obj;
		    int value = slider.getValue();
		    TitledBorder tb = (TitledBorder) slider.getBorder();
		    String s = tb.getTitle();
		    if ( slider.equals(scaleJS) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value));
				scale = value / decimali;
		    } else if ( slider.equals(stepJS) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value));
				step = value;
		    }
			/*else if ( obj.equals(overlapJS) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value / decimali));
				overlap = value / decimali;
		    }*/
		    slider.repaint();
		} else if (obj instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) obj;
			if ( jcb.equals(effetto_check) ) {
				attiva(jcb.isSelected());
			}
		}
	}

	private void addSlider(String name, JSlider slider, JPanel p, ChangeListener listener) {
	    slider.addChangeListener(listener);
	    TitledBorder tb = new TitledBorder(new EtchedBorder());
	    tb.setTitle(name + " = "+ slider.getValue());
	    slider.setBorder(tb);
	    p.add(slider);
	}
	
	private void addSlider(String name, JSlider slider, JPanel p, ChangeListener listener, double divisore) {
	    slider.addChangeListener(listener);
	    TitledBorder tb = new TitledBorder(new EtchedBorder());
	    tb.setTitle(name + " = "+ (slider.getValue() / divisore));
	    slider.setBorder(tb);
	    p.add(slider);
	}
	
}  // end com.github.michelemilesi.university.music.PitchShifter
