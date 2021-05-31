package com.github.michelemilesi.university.music;

import com.github.michelemilesi.university.music.AbsPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


public class EchoStereoPanel extends AbsPanel implements ActionListener, ChangeListener {

	private JSlider tempoSXJS, volumeSXJS, tempoDXJS, volumeDXJS;
	private String tempo_str = "Durata";
	private String volume_str = "Volume";
	private JCheckBox effetto_check, linkedJCB;
	private JButton reset;
	private Color blu_chiaro = new Color(66,66,255);
	private Color rosso_chiaro = new Color(255,66,66);
	private int default_tempo = 250;        //  in millisecondi da 1 a 1000
	private double default_volume = 0.50;   //  from 0.00 to 1.00
	private int tempoSx, tempoDx;
	private double volumeSx, volumeDx;	
	private double decimali = 100;         //  = 1.00
	private int min_volume = 0;				//  valori visibili sulla slider
	private int max_volume = 1;				//  valori visibili sulla slider
	private int min_tempo = 1;				//  valori visibili sulla slider
	private int max_tempo = 1000;			//  valori visibili sulla slider


	public EchoStereoPanel() {
		attiva(false); visualizza(false);
		tempoSx = tempoDx = default_tempo;
		volumeSx = volumeDx = default_volume;
		reset = new JButton("Reset");
		reset.setForeground(new Color(128,128,192));
		reset.addActionListener(this);
		effetto_check = new JCheckBox("com.github.michelemilesi.university.music.Echo",false);
		effetto_check.setForeground(new Color(128,0,255));
		effetto_check.addChangeListener(this);
		linkedJCB = new JCheckBox("Linked", false);
		linkedJCB.setForeground(new Color(0,128,64));
		linkedJCB.addChangeListener(this);
		tempoSXJS = new JSlider(JSlider.HORIZONTAL, min_tempo, max_tempo, default_tempo);
		volumeSXJS = new JSlider(JSlider.HORIZONTAL, (int)(min_volume*decimali),
		                         (int)(max_volume*decimali), (int)(default_volume*decimali));
		tempoDXJS = new JSlider(JSlider.HORIZONTAL, min_tempo, max_tempo, default_tempo);
		volumeDXJS = new JSlider(JSlider.HORIZONTAL, (int)(min_volume*decimali),
		                         (int)(max_volume*decimali), (int)(default_volume*decimali));

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

		TitledBorder tb;		
		JLabel label;
		
		label = new JLabel("CANALE SX (audio mono)");
		label.setForeground(blu_chiaro);
		p.add(label);
		addSlider(tempo_str, tempoSXJS, p, this);
		tb = (TitledBorder) tempoSXJS.getBorder();
		tb.setTitleColor(blu_chiaro);
	    tb.setTitle(tempo_str + " = "+ default_tempo + " msec.");
		addSlider(volume_str, volumeSXJS, p, this, decimali);
		tb = (TitledBorder) volumeSXJS.getBorder();
		tb.setTitleColor(blu_chiaro);
		
		label = new JLabel("CANALE DX");
		label.setForeground(rosso_chiaro);
		p.add(label);
		addSlider(tempo_str, tempoDXJS, p, this);
		tb = (TitledBorder) tempoDXJS.getBorder();
		tb.setTitleColor(rosso_chiaro);
	    tb.setTitle(tempo_str + " = "+ default_tempo + " msec.");
		addSlider(volume_str, volumeDXJS, p, this, decimali);
		tb = (TitledBorder) volumeDXJS.getBorder();
		tb.setTitleColor(rosso_chiaro);
		p.add(linkedJCB);

		add(p,c);
	}

	protected void init() {
		filtro.setParameter(StereoEcho.DELAY_SX, tempoSx);
		filtro.setParameter(StereoEcho.VOLUME_SX, volumeSx);
		filtro.setParameter(StereoEcho.DELAY_DX, tempoDx);
		filtro.setParameter(StereoEcho.VOLUME_DX, volumeDx);
		filtro.setParameter(StereoEcho.LINKED, linkedJCB.isSelected() ? 1 : 0);
	}

	public void loadFilter(String description, String nome_filtro) throws Exception {
		try {
			Class cls = Class.forName(nome_filtro);
			Filter f = (Filter) cls.newInstance();
			if (f instanceof StereoEcho) {
				setFilter(f);
				setFilterName(f.getClass().getName());
				setDescription(description);
				visualizza(true);
			} else throw new InstantiationException();
		} catch (Exception e) {
			System.out.println("Errore di caricamento/istanziazione filtro: com.github.michelemilesi.university.music.StereoEcho");
			throw e;
		}
	}

	public void setParameter(String[] elenco) throws Exception {
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(reset)) {
			tempoSx = tempoDx = default_tempo;
			volumeSx = volumeDx = default_volume;
			tempoSXJS.setValue(default_tempo);
			volumeSXJS.setValue((int)(default_volume*decimali));
			tempoDXJS.setValue(default_tempo);
			volumeDXJS.setValue((int)(default_volume*decimali));
			linkedJCB.setSelected(false);
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JSlider) {
		    JSlider slider = (JSlider) obj;
		    int value = slider.getValue();
		    TitledBorder tb = (TitledBorder) slider.getBorder();
		    String s = tb.getTitle();
		    if ( s.startsWith(volume_str) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value / decimali));
				if ( slider.equals(volumeSXJS) )
					volumeSx = value / decimali;
				else  volumeDx = value / decimali;
		    } else if ( s.startsWith(tempo_str) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value) + " msec.");
			    if ( slider.equals(tempoSXJS) )
			    	tempoSx = value;
			    else  tempoDx = value;
		    }
		    slider.repaint();
		} else if (obj instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) obj;
			if ( jcb.equals(effetto_check) ) {
				attiva(jcb.isSelected());
			} else if ( jcb.equals(linkedJCB) ) {
				filtro.setParameter(StereoEcho.LINKED, linkedJCB.isSelected() ? 1 : 0);
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
	
}  // end com.github.michelemilesi.university.music.Echo Stereo
