package com.github.michelemilesi.university.music;

import java.awt.*;
import javax.swing.JPanel;
import java.io.File;

public class Immagine extends JPanel  implements Runnable {
	
	private File file;
	private Image[] immagini;
	private String commento;
	private Dimension dim;
	private Thread thread;
	private int width, height;
	private int img;
	

	public Immagine(String[] nomefile, String commento, int width, int height) {
	
		this.commento = commento;
		immagini = new Image[nomefile.length];
		for (int i=0; i<nomefile.length; i++) {
			file = new File(nomefile[i]);
			if (file.exists())
				immagini[i] = Toolkit.getDefaultToolkit().getImage(nomefile[i]);
			else {
				System.out.println("File: "+nomefile[i]+" inesistente!");
				System.out.println("- animazione non caricata -");
			}	
		}
		img = 0;
		this.width = width;
		this.height = height;
		dim = new Dimension(width ,height);
		setPreferredSize(dim);
	}
		
	public void start() {
	    thread = new Thread(this);
	    thread.start();
	}

	public void stop() {
	    if (thread != null) {
	        thread.interrupt();
	    }
	    thread = null;
	}
	
	public void run() {
		while (true) {
			try {
				thread.sleep(250);
			} catch (Exception e) { break; }
			img = (img + 1) % immagini.length;
			repaint();
		}
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(getBackground());
		g2.clearRect(0, 0, width, height);
		if (immagini[img]==null)
			g2.drawString(commento,10,10);
		else	
			g2.drawImage(immagini[img],0,0,this);
	}
	

	public void update(Graphics g) {
		paint(g);
	}
	
	
}
