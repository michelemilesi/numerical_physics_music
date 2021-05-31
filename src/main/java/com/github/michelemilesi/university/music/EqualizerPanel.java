package com.github.michelemilesi.university.music;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class EqualizerPanel extends AbsPanel implements ActionListener, ChangeListener {
	private JCheckBox effetto_check;
	private JButton reset;
	private JSlider[] vjslider;
	private JSlider stepJS;
	private JPanel pan_slider;
	private int elements_number = 6;
	private int[] vfreq = { 63, 250, 1000, 4000, 8000, 16000 };  // Hz
	private double[] vmisure;
	private double decimali = 10;   //  = 1.0
	private int min = -7;	//  dB
	private int max = 7;	//  dB
	private String step_str = "Frame Step";
	private int default_step = 2048;
	private int step;

	public EqualizerPanel() {

		//  inizializza
		step = default_step;
		vmisure = new double[elements_number];     //  inizializzato a 0.0
		
		attiva(false); visualizza(false);
		reset = new JButton("Reset");
		reset.setForeground(new Color(128,128,192));
		reset.addActionListener(this);
		reset.addActionListener(this);
		effetto_check = new JCheckBox("com.github.michelemilesi.university.music.Equalizer",false);
		effetto_check.setForeground(new Color(128,0,255));
		effetto_check.addChangeListener(this);
		stepJS = new JSlider(JSlider.HORIZONTAL, 512, 4096, default_step);
		vjslider = new JSlider[elements_number];
		for (int i=0; i<vjslider.length; i++) {
			vjslider[i] = new JSlider(JSlider.HORIZONTAL, (int)(min*decimali), (int)(max*decimali), (int)(vmisure[i]*decimali));
		}

		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		CompoundBorder cb = new CompoundBorder(eb, new EtchedBorder());
		setBorder(new CompoundBorder(cb, new EmptyBorder(8,8,8,8)));

		pan_slider = new JPanel();
		pan_slider.setLayout(new GridLayout(0,1));;
		for (int i=0; i<vjslider.length; i++) {
			addSlider(Integer.toString(vfreq[i])+" Hz", vjslider[i], pan_slider, this, decimali);
		    TitledBorder tb = (TitledBorder) vjslider[i].getBorder();
			tb.setTitleJustification(tb.RIGHT);
		}

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

		c.gridheight = c.REMAINDER;
		c.weightx = 2;
		c.weighty = 5;
		c.fill = c.BOTH;
		add(pan_slider,c);
	}

	protected void init() {
		double[] freq = new double[vfreq.length];
		for (int i=0;i<vfreq.length ;i++ ) {
			freq[i] = vfreq[i];
		}
		filtro.setParameter(Equalizer.FREQUENCIES, freq);
		filtro.setParameter(Equalizer.DB_VALUES, vmisure);
		filtro.setParameter(PitchShifter.STEP, step);
	}

	public void loadFilter(String description, String nome_filtro) throws Exception {
		try {
			Class cls = Class.forName(nome_filtro);
			Filter f = (Filter) cls.newInstance();
			if (f instanceof Equalizer) {
				setFilter(f);
				setFilterName(f.getClass().getName());
				setDescription(description);
				visualizza(true);
			} else throw new InstantiationException();
		} catch (Exception e) {
			System.out.println("Errore di caricamento/istanziazione filtro: com.github.michelemilesi.university.music.Equalizer");
			throw e;
		}
	}
	
	public void setParameter(String[] elenco) throws Exception {
		int[] freqs = new int[elenco.length];
		try {
			for (int i=0; i<elenco.length; i++)
				freqs[i] = Integer.valueOf(elenco[i]).intValue();
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException("Verificare le frequenze dell'equalizzatore!");
		}
		elements_number = freqs.length;
		vjslider = new JSlider[elements_number];
		vmisure = new double[elements_number];     //  inizializzato a 0.0
		vfreq = freqs;
		step = default_step;
		for (int i=0; i<vjslider.length; i++)
			vjslider[i] = new JSlider(JSlider.HORIZONTAL, (int)(min*decimali), (int)(max*decimali), (int)(vmisure[i]*decimali));
		pan_slider.removeAll();
		for (int i=0; i<vjslider.length; i++) {
			addSlider(Integer.toString(vfreq[i])+" Hz", vjslider[i], pan_slider, this, decimali);
		    TitledBorder tb = (TitledBorder) vjslider[i].getBorder();
			tb.setTitleJustification(tb.RIGHT);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(reset)) {
			for (int i=0; i<vjslider.length; i++) {
				vmisure[i] = 0.0;
				vjslider[i].setValue((int)(vmisure[i]*decimali));
			}
			step = default_step;
			stepJS.setValue((int)(default_step));
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JSlider) {
		    JSlider slider = (JSlider) obj;
		    int value = slider.getValue();
		    TitledBorder tb = (TitledBorder) slider.getBorder();
		    String s = tb.getTitle();
			if ( slider.equals(stepJS) ) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value));
				step = value;
		    } else {
			    int i = 0;
			    while (!slider.equals(vjslider[i]) && i<vjslider.length)
			    	i++;
			    if (i<vfreq.length) {
			    	tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value / decimali) + " dB");
			    	vmisure[i] = value / decimali;
			    }
		    }
		    slider.repaint();
		} else if (obj instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) obj;
			if ( jcb.equals(effetto_check) ) {
				attiva(jcb.isSelected());
			}
		}
	}

	private void addSlider(String name, JSlider slider, JPanel p, ChangeListener listener, double divisore) {
	    slider.addChangeListener(listener);
	    TitledBorder tb = new TitledBorder(new EtchedBorder());
	    tb.setTitle(name + " = "+ (slider.getValue() / divisore) + " dB");
	    slider.setBorder(tb);
	    p.add(slider);
	}
	
	
}  // end Equalizzatore


