import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class EchoPanel extends AbsPanel implements ActionListener, ChangeListener {
	private int tempo, default_tempo = 250;         //  in millisecondi da 1 a 1000
	private double volume, default_volume = 0.50;   //  from 0.00 to 1.00
	private JSlider tempoJS, volumeJS;
	private String tempo_str = "Durata";
	private String volume_str = "Volume";
	private JCheckBox effetto_check;
	private JButton reset;

	public EchoPanel() {
		attiva(false); visualizza(false);
		tempo = default_tempo;
		volume = default_volume;
		reset = new JButton("Reset");
		reset.addActionListener(this);
		effetto_check = new JCheckBox("Echo",false);
		effetto_check.addChangeListener(this);
		tempoJS = new JSlider(JSlider.HORIZONTAL, 1, 1000, tempo);
		volumeJS = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(volume*100));

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
		p.setLayout(new GridLayout(2,1));
		addSlider(tempo_str, tempoJS, p, this);
		addSlider(volume_str, volumeJS, p, this, 100.0);			
		add(p,c);
	}

	protected void init() {
		filtro.setParameter(Echo.DELAY, tempo);
		filtro.setParameter(Echo.VOLUME, volume);
	}

	public void loadFilter(String description, String nome_filtro) throws Exception {
		try {
			Class cls = Class.forName(nome_filtro);
			Filter f = (Filter) cls.newInstance();
			if (f instanceof Echo) {
				setFilter(f);
				setFilterName(f.getClass().getName());
				setDescription(description);
				visualizza(true);
			} else throw new InstantiationException();
		} catch (Exception e) {
			System.out.println("Errore di caricamento/istanziazione filtro: Echo");
			throw e;
		}
	}

	public void setParameter(String[] elenco)  throws Exception {
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(reset)) {
			tempo = default_tempo;
			volume = default_volume;
			tempoJS.setValue(tempo);
			volumeJS.setValue((int)(volume*100));
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JSlider) {
		    JSlider slider = (JSlider) obj;
		    int value = slider.getValue();
		    TitledBorder tb = (TitledBorder) slider.getBorder();
		    String s = tb.getTitle();
		    if (s.startsWith(volume_str)) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value / 100.0));
		        volume = value / 100.0;
		    } else if (s.startsWith(tempo_str)) {
			    tb.setTitle(s.substring(0, s.indexOf('=')+2) + s.valueOf(value) + " msec.");
				tempo = value;
		    }
		    slider.repaint();
		} else if (obj instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) obj;
			attiva(jcb.isSelected());
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
	
}  // end Echo
