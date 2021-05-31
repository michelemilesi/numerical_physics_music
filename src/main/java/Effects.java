import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.io.*;
import java.util.*;
import java.applet.*;
import java.net.*;


public class Effects extends JScrollPane implements ControlContext, Runnable {

	private Vector elenco_effetti = new Vector();
	private BufferedReader setup;
	private String nomeSound = "audio/wait.wav";
	private String nomeSoundEndClip = "audio/shutdown.wav";
	private AudioClip clip, endClip;
	private Thread thread;
	private Sound audioNonProcessato, audioProcessato;
	private ProgressDialog pd;

	public Effects() {
		
		if (WaveEffect.musicIsEnabled()) {
			try {
				clip = Applet.newAudioClip(new URL("file","",nomeSound));
				endClip = Applet.newAudioClip(new URL("file","",nomeSoundEndClip));
			} catch (MalformedURLException murle) {
				System.out.println("File: "+nomeSound+" non trovato!");
				clip = null;
			}
		}
		try {
			setup = new BufferedReader(new FileReader("effetti.ini"));
		} catch (FileNotFoundException fnfe) {
			System.out.println("File di setup mancante: effetti.ini");
			System.exit(1);
		}
		try {
			while (setup.ready()) {
				String linesetup = setup.readLine();
				if ( linesetup.startsWith("//") || (linesetup.length()==0) )
					continue;
				try {
					StringTokenizer strToken = new StringTokenizer(linesetup,":");
					if (strToken.countTokens()>=3) {
						String descrizione = strToken.nextToken();
						String nome_filtro = strToken.nextToken();
						String nome_pannello = strToken.nextToken();
						Class cls = Class.forName(nome_pannello);
						AbsPanel abspan = (AbsPanel) cls.newInstance();
						abspan.loadFilter(descrizione,nome_filtro);
						if (strToken.countTokens() == 1) {
							strToken = new StringTokenizer(strToken.nextToken(),",");
							String[] parametri = new String[strToken.countTokens()];
							for (int i=0; i<parametri.length; i++) {
								parametri[i] = strToken.nextToken();
							}
							abspan.setParameter(parametri);
						}
						elenco_effetti.addElement(abspan);
					} else throw new NoSuchElementException();
				} catch (NoSuchElementException nsee) {
					System.out.println("Parametri di caricamento mancanti: ");
					System.out.println("-> "+linesetup);
				} catch (NullPointerException npe) {
					System.out.println("Errore sintattico nella riga di setup: ");
					System.out.println("-> "+linesetup);
				} catch (Exception e) {
					System.out.println(e.toString());
					System.out.println("Errore nel caricamento:");
					System.out.println("  effetti.ini -> "+linesetup);
				} // end try-catch
			} // end while
		} catch (IOException ioe) {
			System.out.println("Errore nella lettura del file: effetti.ini");
			System.exit(1);
		} // end try-catch
		
		JPanel pannello = new JPanel();
		pannello.setLayout(new GridLayout(0,3));
		for (int i=0; i<elenco_effetti.size(); i++) {
			pannello.add( (AbsPanel) elenco_effetti.elementAt(i) );
		}
		setViewportView(pannello);		

	} // end Effects()


	public void open() {
	}

	public void close() {
	}

	public Vector getEffetti() {
		return elenco_effetti;
	}

	public Sound applyEffects(Sound audio, Frame parent) {

		audioNonProcessato = audio;

		Filter[] vettFilter = new Filter[elenco_effetti.size()];
		for (int i=0; i<elenco_effetti.size(); i++) 
			vettFilter[i] = ((AbsPanel)elenco_effetti.elementAt(i)).getFilter();
		
		pd = new ProgressDialog(parent,vettFilter);
				
		start();  // processa l'audio
	
		pd.setVisible(true);
		//pd.show();  // mostra la proggress bar

		//stop();  // uccide il thread se fosse ancora vivo
	
		return audioProcessato;
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

		Sound audio = audioNonProcessato;
		audioProcessato = null;

		//  attesa necessaria solo per aspettare che venga aperta prima la progressdialog
		//try {
		//	thread.sleep(50);
		//} catch (Exception e) { }

		
		if (WaveEffect.musicIsEnabled())  clip.loop();
		// ora applica i filtri
		for (int i=0; i<elenco_effetti.size(); i++) {
			AbsPanel eff = (AbsPanel) elenco_effetti.elementAt(i);
			if (eff.isSelected())  {
				audioProcessato = eff.applica(audio);  // viene restituito un nuovo oggetto
				audio = audioProcessato;
			}
		}

		
		pd.dispose();

		if (clip!=null)  clip.stop();
		if (endClip!=null && audioProcessato!=null) endClip.play();
	}


}  // end Effects