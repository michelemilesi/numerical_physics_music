package com.github.michelemilesi.university.music;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


public class NormalizerPanel extends AbsPanel implements ActionListener, ChangeListener {

	private final double default_value = 0.8;   //  from 0.0 to 1.0
	private final double decimali = 100;      //  = 1.00
	private final double min = 0.01;
	private final double max = 1.00;
	private double value;
	private JSlider valueJS;
	private String value_str = "Valore";
	private JCheckBox effetto_check;
	private JButton reset;

	public NormalizerPanel() {
		attiva(false); visualizza(false);
		value = default_value;
		reset = new JButton("Reset");
		reset.setForeground(new Color(128,128,192));
		reset.addActionListener(this);
		effetto_check = new JCheckBox("com.github.michelemilesi.university.music.Normalizer",false);
		effetto_check.setForeground(new Color(128,0,255));
		effetto_check.addChangeListener(this);
		valueJS = new JSlider(JSlider.HORIZONTAL, (int)(min*decimali), (int)(max*decimali), (int)(default_value*decimali));

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
		
		c.weighty = 2;
		c.fill = c.HORIZONTAL;
		c.gridheight = c.REMAINDER;

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0,1));
		addSlider(value_str, valueJS, p, this, decimali);

		add(p,c);
	}

	protected void init() {
		filtro.setParameter(Normalizer.MAX_VALUE, value);
	}

	public void loadFilter(String description, String nome_filtro) throws Exception {
		try {
			Class cls = Class.forName(nome_filtro);
			Filter f = (Filter) cls.newInstance();
			if (f instanceof Normalizer) {
				setFilter(f);
				setFilterName(f.getClass().getName());
				setDescription(description);
				visualizza(true);
			} else throw new InstantiationException();
		} catch (Exception e) {
			System.out.println("Errore di caricamento/istanziazione filtro: com.github.michelemilesi.university.music.Normalizer");
			throw e;
		}
	}

	public void setParameter(String[] elenco) throws Exception {
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(reset)) {
			value = default_value;
			valueJS.setValue((int)(default_value*decimali));
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JSlider) {
		    JSlider slider = (JSlider) obj;
		    int ris = slider.getValue();
		    TitledBorder tb = (TitledBorder) slider.getBorder();
		    String s = tb.getTitle();
		    if ( slider.equals(valueJS) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(ris / decimali));
				value = ris / decimali;
		    }
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
	
}  // end com.github.michelemilesi.university.music.Normalizer
