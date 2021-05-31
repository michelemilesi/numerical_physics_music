package com.github.michelemilesi.university.music;

import com.github.michelemilesi.university.music.AbsPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;


public class EffectsDialog extends JDialog implements ActionListener {

	private String title = "Gestione Filtri";
	private String title_label = "PRECEDENZA FILTRI";
	private Vector lista_vis_filtri = new Vector();
	private JScrollPane scrollPane;
	private JList dataList;								//  JList dei filtri caricati
	private JLabel jlab;
	private JButton buttEXE, buttUP, buttDOWN;
	private JRadioButton jrbFFT, jrbDFT;
	private Vector elencofiltri;
	

	public EffectsDialog(Vector elencofiltri) {
		this.elencofiltri = elencofiltri;
		dataList = new JList();
		jlab = new JLabel(title_label);
		buttEXE = new JButton("OK");
		buttUP = new JButton("UP");
		buttDOWN = new JButton("DOWN");
		buttEXE.addActionListener(this);
		buttUP.addActionListener(this);
		buttDOWN.addActionListener(this);
		jrbFFT = new JRadioButton("FFT: Fast com.github.michelemilesi.university.music.Fourier Transform");
		jrbDFT = new JRadioButton("DFT: Discrete com.github.michelemilesi.university.music.Fourier Transform");
		jrbFFT.addActionListener(this);
		jrbDFT.addActionListener(this);
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(jrbFFT);
		radioGroup.add(jrbDFT);
		if (FrequencyFilter.isFFTActive())
			jrbFFT.setSelected(true);
		else  jrbDFT.setSelected(true);
		setModal(true);
		setSize(480,300);
		setLocation(400 - getWidth()/2, 300 - getHeight()/2);
		setResizable(false);
		setTitle(title);
		init();
	}


	public void gestisciFiltri(Vector filtri) {
		this.elencofiltri = filtri;
	}


	private JPanel getPanelFFT() {

		JPanel pannello = new JPanel();
		EmptyBorder eb = new EmptyBorder(10,0,0,0);
		CompoundBorder cb = new CompoundBorder(eb, new EtchedBorder());
		pannello.setBorder(new CompoundBorder(cb, new EmptyBorder(5,5,5,5)));
		pannello.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = c.BOTH;
		c.gridwidth = c.REMAINDER;
		c.gridheight = c.RELATIVE;
		pannello.add(jrbFFT,c);
		c.gridheight = c.REMAINDER;
		pannello.add(jrbDFT,c);

		return pannello;
	}


	private JPanel getPanelVisual() {

		JPanel pannello = new JPanel();
		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		CompoundBorder cb = new CompoundBorder(eb, new EtchedBorder());
		pannello.setBorder(new CompoundBorder(cb, new EmptyBorder(8,8,8,8)));
		pannello.setLayout(new GridBagLayout());
		JLabel titolo = new JLabel("FILTRI VISUALIZZATI");
		pannello.setPreferredSize(new Dimension(this.getWidth()/2, this.getHeight()/2));
		Box pan = new Box(BoxLayout.Y_AXIS);
		JScrollPane scrollPane = new JScrollPane(pan);
		scrollPane.getVerticalScrollBar().setUnitIncrement(25);
		pan.setBorder(new EmptyBorder(5,7,5,5));
		for (int i=0; i<elencofiltri.size(); i++) {
			AbsPanel filtro = (AbsPanel) elencofiltri.elementAt(i);
			String nome = filtro.getFilterName() + " : " + filtro.getDescription();
			JCheckBox jcb = new JCheckBox(nome);
			lista_vis_filtri.addElement(jcb);
			jcb.setSelected(filtro.isVisible());
			jcb.addActionListener(this);
			pan.add(jcb);
		}

		JPanel pan2 = new JPanel();
		pan2.setLayout(new BoxLayout(pan2, BoxLayout.Y_AXIS));
		pan2.add(scrollPane);
		pan2.add(getPanelFFT());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = c.VERTICAL;
		c.gridwidth = c.REMAINDER;
		c.gridheight = c.RELATIVE;
		pannello.add(titolo,c);
		c.gridheight = c.REMAINDER;
		c.weightx = 1; c.weighty = 2;
		c.insets = new Insets(20,0,0,0);
		pannello.add(pan2,c);
		
		return pannello;
	}


	private JPanel getPanelPreced() {

		dataList.setBorder(new EmptyBorder(5,5,5,5));
		dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Vector lista_des_filtri = new Vector();
		for (int i=0; i<elencofiltri.size(); i++) {
			lista_des_filtri.addElement( ((AbsPanel)elencofiltri.elementAt(i)).getDescription() );
		}
		dataList.setListData(lista_des_filtri);
		scrollPane = new JScrollPane(dataList);
		JPanel pannello = new JPanel();
		pannello.setPreferredSize(new Dimension(this.getWidth()/2, this.getHeight()/2));		
		EmptyBorder eb = new EmptyBorder(5,5,5,5);
		CompoundBorder cb = new CompoundBorder(eb, new EtchedBorder());
		pannello.setBorder(new CompoundBorder(cb, new EmptyBorder(8,8,8,8)));
		pannello.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		c.fill = c.BOTH;
		c.gridwidth = c.REMAINDER;
		p.add(buttUP,c);
		c.insets = new Insets(40,0,0,0);
		c.gridheight = c.REMAINDER;
		p.add(buttDOWN,c);

		c = new GridBagConstraints();
		c.weightx = 2; c.weighty = 1;
		c.fill = c.BOTH;
		c.gridwidth = c.REMAINDER;
		pannello.add(jlab,c);
		c.weightx = 1; c.weighty = 2;
		c.gridwidth = c.RELATIVE;
		pannello.add(scrollPane,c);
		c.gridwidth = c.REMAINDER;
		pannello.add(p,c);
		c.gridheight = c.REMAINDER;
		c.weightx = 2; c.weighty = 1;
		c.insets = new Insets(10,0,0,0);
		pannello.add(buttEXE,c);
		return pannello;
	}
	

	private void init() {

		JPanel contenitore = new JPanel();
		contenitore.setLayout(new BoxLayout(contenitore, BoxLayout.X_AXIS));
		contenitore.add(getPanelVisual());
		contenitore.add(getPanelPreced());
		getContentPane().add(contenitore);
	}
	


	public void actionPerformed(ActionEvent e) {

		Object obj = e.getSource();
		if (obj instanceof JButton) {
			if (obj.equals(buttEXE)) {
				hide();
			} else if (obj.equals(buttUP)) {
				int index = dataList.getSelectedIndex();
				if ( index>0 ) {
					Object flt = elencofiltri.elementAt(index);
					elencofiltri.setElementAt( elencofiltri.elementAt(index-1), index);
					elencofiltri.setElementAt(flt,index-1);
					Vector lista_des_filtri = new Vector();
					for (int i=0; i<elencofiltri.size(); i++) {
						lista_des_filtri.addElement( ((AbsPanel)elencofiltri.elementAt(i)).getDescription() );
					}
					dataList.setListData(lista_des_filtri);
					dataList.setSelectedIndex(index-1);
					if ( (index-1) <= dataList.getFirstVisibleIndex() ) {
						JScrollBar jsb = scrollPane.getVerticalScrollBar();
						int pos = jsb.getValue();
						if (pos > jsb.getMinimum() )
							jsb.setValue( pos -	jsb.getUnitIncrement(-1) );
					}
				}
			} else if (obj.equals(buttDOWN)) {
				int index = dataList.getSelectedIndex();
				if ( (index < elencofiltri.size()-1) && (index >= 0) ) {
					Object flt = elencofiltri.elementAt(index);
					elencofiltri.setElementAt( elencofiltri.elementAt(index+1), index);
					elencofiltri.setElementAt(flt,index+1);
					Vector lista_des_filtri = new Vector();
					for (int i=0; i<elencofiltri.size(); i++) {
						lista_des_filtri.addElement( ((AbsPanel)elencofiltri.elementAt(i)).getDescription() );
					}
					dataList.setListData(lista_des_filtri);
					dataList.setSelectedIndex(index+1);
					if ( index+1 >= dataList.getLastVisibleIndex() ) {
						JScrollBar jsb = scrollPane.getVerticalScrollBar();
						int pos = jsb.getValue();
						if (pos < jsb.getMaximum() )
							jsb.setValue( pos +	jsb.getUnitIncrement(1) );
					}
				}
			} // end else
		} // end instanceof JButton
		else if (obj instanceof JCheckBox) {
				JCheckBox jcb = (JCheckBox) obj;
				String label = jcb.getText();
				String classe = label.substring(0,label.indexOf(" : "));
				String descrizione = label.substring(label.indexOf(":")+2);
				for (int i=0; i<elencofiltri.size(); i++) {
					AbsPanel absP = (AbsPanel) elencofiltri.elementAt(i);
					if (  (classe.compareTo(absP.getFilterName())==0) && 
					      (descrizione.compareTo(absP.getDescription())==0)  ) {
						absP.visualizza(!absP.isVisible());
						absP.doLayout();
						break;
					}
				}
			} // end instanceof JCheckBox
		else if (obj instanceof JRadioButton) {
				if (obj.equals(jrbFFT))
					FrequencyFilter.setFFTActive(true);
				else  FrequencyFilter.setFFTActive(false);
			} // end instanceof JRadioButton
	} // end actionPerformed


}