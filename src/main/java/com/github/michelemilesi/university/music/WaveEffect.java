package com.github.michelemilesi.university.music; /**
 * com.github.michelemilesi.university.music.WaveEffect: applica effetti audio a file sonori di tipo wave
 *
 * @version 1.00
 * @author Alberto Bettinelli & Michele Milesi
 */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.util.Vector;
import javax.sound.sampled.*;


public class WaveEffect extends JPanel implements ChangeListener, Runnable {

	private static boolean music = false;

    Vector demos = new Vector(2);				//  elementi TabbedPane
    JTabbedPane tabPane = new JTabbedPane();
    int width = 800, height = 600;				//  dimensioni finestra
    int index;									//  indice del TabbedPane attivo
	String[] nomifile = { "image/cassa.gif" };
	Image[] immagini = new Image[1];
	String[] filemidi;
	FilenameFilter fnf;
	AudioClip clip;
	String startupSong = "audio/startup.wav";


    public WaveEffect(String audioDirectory) {
		this(audioDirectory, false);
    }

	public WaveEffect(String audioDirectory, boolean musica) {
	
		music = musica;
		setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = (JMenu) menuBar.add(new JMenu("File"));
		JMenuItem item = (JMenuItem) fileMenu.add(new JMenuItem("Exit"));
		item.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) { System.exit(0); }
		});
		JMenu options = (JMenu) menuBar.add(new JMenu("Options"));
		item = (JMenuItem) options.add(new JMenuItem("About"));
		item.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) { showInfoDialog(); }
		});
		add(menuBar, BorderLayout.NORTH);

		tabPane.addChangeListener(this);

		EmptyBorder eb = new EmptyBorder(2,2,2,2);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder cb = new CompoundBorder(eb,bb);
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new CompoundBorder(cb,new EmptyBorder(3,3,3,3)));
		Audio audiopane = new Audio(audioDirectory);
		p.add(audiopane);
		demos.add(audiopane);
		tabPane.addTab("Play/Rec.",p);
		
		new Thread(this).start();

		add(tabPane, BorderLayout.CENTER);
		
		// caricamento immagini
		for (int i=0; i<nomifile.length; i++) {
			File file = new File(nomifile[i]);
			if (file.exists())
				immagini[i] = Toolkit.getDefaultToolkit().getImage(nomifile[i]);
			else {
				System.out.println("File: "+nomifile[i]+" inesistente!");
				System.out.println("- animazione non caricata -");
			}
		}
		
		// caricamento dei file midi
		File file = new File("audio");
		filemidi = file.list(new FindFile(".mid"));
		if (music) {
			try {
				clip = Applet.newAudioClip(new URL("file","",startupSong));
				clip.play();
			} catch (MalformedURLException murle) {
				System.out.println("File: "+startupSong+" non trovato!");
			}
		}
	} // end Costruttore


	public static boolean musicIsEnabled() {
		return music;
	}
	
	public static void enableMusic(boolean value) {
		music = value;
	}
	

    public void stateChanged(ChangeEvent e) {
        close();		//  chiude tutti gli stream del pannello attivo
        System.gc();
        index = tabPane.getSelectedIndex();
        open();
    }


    public void close() {
        ((ControlContext) demos.get(index)).close();
    }


    public void open() {
        ((ControlContext) demos.get(index)).open();
    }


    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }


    public void showInfoDialog() {
		if (filemidi.length>0) {
			try {
				String nome = filemidi[(int)((Math.random()*10*filemidi.length)%filemidi.length)];
				clip = Applet.newAudioClip(new URL("file","","audio/"+nome));
				clip.loop();
			} catch (MalformedURLException murle) {
				System.out.println("File midi non caricato!");
			}
		}
		final String msg = "\n\ncom.github.michelemilesi.university.music.WaveEffect v.1.00 - (c)2002\n" +
		                   "scritto da:\n\n" + 
						   "Alberto Bettinelli & Michele Milesi\n\n\n";
		JOptionPane jop = new JOptionPane(msg);
		jop.setIcon(new ImageIcon(immagini[0]));
		JDialog dialog = jop.createDialog(this, "About");
		dialog.show();
		if (filemidi.length>0)  clip.stop();
    }


    public void run() {
        EmptyBorder eb = new EmptyBorder(2,2,2,2);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb,new EmptyBorder(3,3,3,3)));
        Effects effetti = new Effects();
        demos.add(effetti);
		((Audio)demos.elementAt(0)).setEffettiBase(effetti);
        p.add(effetti);
        tabPane.addTab("Effetti", p);
    }


    public static void main(String[] args) {

        try { 
			if (AudioSystem.getMixer(null) == null) {
                System.err.println("AudioSystem non disponibile, termine programma!");
                System.exit(1);
            }
        } catch (Exception ex) { ex.printStackTrace(); System.exit(1); }

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Errore durante il caricamento dello stile Look & Feel di default della macchina!!!");
			System.out.println(e.getMessage());
		}

        String mediaDir = ".";
		boolean musica = false;
		for (int i=0; i<args.length && i<2; i++) {
			if (args[i].compareToIgnoreCase("-m")==0)
				musica = true;
			else {
				File file = new File(args[i]);
				if (file == null || !file.isDirectory()) {
					System.out.println("uso: java com.github.michelemilesi.university.music.WaveEffect -m audioDirectory");
					System.out.println("\n-m : abilita gli effetti sonori nel programma \n");
				} else {
				    mediaDir = args[i];
				}
				
			} // end else
		} // end for

        final WaveEffect prg = new WaveEffect(mediaDir, musica);
        JFrame f = new JFrame("WaveEffect");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
            public void windowDeiconified(WindowEvent e) { prg.open(); }
            public void windowIconified(WindowEvent e) { prg.close(); }
        });
        f.getContentPane().add("Center", prg);
        f.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(d.width/2 - prg.width/2, d.height/2 - prg.height/2);
        f.setSize(new Dimension(prg.width, prg.height));
        f.setVisible(true);
		f.setResizable(false);
    }
}