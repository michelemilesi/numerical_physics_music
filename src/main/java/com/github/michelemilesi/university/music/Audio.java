package com.github.michelemilesi.university.music;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;
import java.text.*;


public class Audio extends JPanel implements ActionListener, ControlContext {

    private final int bufSize = 16384;      // usato per il playback
	private final int frameBuffer = 16384;  // num. di frame letti
	private final int ALLOFF = 0;
	private final int EFFON = 1;
	private final int ALLON = 2;
	private int statebefore = 0;
	private JPanel pan_onda_out;

	private FormatControls formatControls = new FormatControls();
	private Spetrometro spetrometro;
    private Capture capture = new Capture();
    private Playback playback = new Playback();
	private PlaybackEffectWave playbackeffectwave = new PlaybackEffectWave();

    private SamplingGraph samplingGraph, samplingGraphEffect;
	private Sound audioWavStream, audioEffectStream;
    private Effects effetti;
	private EffectsDialog finestra_dialogo;
	private Immagine animazione;
	private Mutex mutex = new Mutex();
			
    private JButton playB, captB, pausB, loadB;		// bottoni per l'onda d'input
	private JButton playB2, saveB2, pausB2;			// bottoni per l'onda modificata
	private JButton efftB, aplyE;					// bottoni per i filtri
	private JLabel label_file_info;

    private String fileName = "untitled";			// nome del file aperto
	private String audioDirectory;					// nome della audio-Directory
    private File file;								// file wav aperto
    private String errStr;
    private double duration;						// durata del brano in secondi
	private String file_format_description;			// descrizione del file aperto


	public Audio(String audioDirectory) {
		this(audioDirectory, null);
	}

    public Audio(String audioDirectory, Effects effetti) {
		this.audioDirectory = audioDirectory;
		this.effetti = effetti;
	    GridBagConstraints c;
	    JPanel pan_onda_input,pan_file;
	    //  Crea il bordo + esterno cornice
	    setBorder(new EmptyBorder(5,5,5,5));

	    //  Creo pannello tasti per onda di input
	    JPanel pan_bott = new JPanel();
	    pan_bott.setLayout(new GridBagLayout());
	    c = new GridBagConstraints();
	    c.fill = c.BOTH;
	    c.insets = new Insets(5,10,5,0);
	    c.gridwidth = c.REMAINDER;
	    playB = newButton("Play", false);
	    pan_bott.add(playB,c);
	    captB = newButton("Record", true);
	    pan_bott.add(captB,c);
	    pausB = newButton("Pause", false);
	    pan_bott.add(pausB,c);
	    loadB = newButton("Load...", true);
	    pan_bott.add(loadB,c);
	    efftB = newButton("Effetti", true);
	    pan_bott.add(efftB,c);
	    c.gridheight = c.REMAINDER;
	    aplyE = newButton("Apply", false);
	    pan_bott.add(aplyE,c);
	    //  Creo il pannello per la visualizzazione dell'onda input
	    pan_onda_input = new JPanel();
	    EmptyBorder eb = new EmptyBorder(0,0,0,0);
	    BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
	    CompoundBorder cb = new CompoundBorder(eb, bb);
	    pan_onda_input.setBorder(new CompoundBorder(cb, new EmptyBorder(5,5,5,5)));
	    pan_onda_input.setLayout(new GridBagLayout());
	    samplingGraph = new SamplingGraph("GraficoFileWave");
	    samplingGraph.setPreferredSize(new Dimension(500,220));
	    samplingGraph.setMinimumSize(new Dimension(500,220));
	    c = new GridBagConstraints();
	    c.fill = c.BOTH;
	    c.gridheight = c.REMAINDER;
	    pan_onda_input.add(samplingGraph,c);
	    c.gridwidth = c.REMAINDER;
	    pan_onda_input.add(pan_bott,c);

	    //  Creo pannello tasti per onda effetto
	    JPanel pan_bott2 = new JPanel();
	    pan_bott2.setLayout(new GridBagLayout());
	    c = new GridBagConstraints();
	    c.fill = c.BOTH;
	    c.insets = new Insets(5,10,5,0);
	    c.gridwidth = c.REMAINDER;
	    playB2 = newButton("Play", false);
	    pan_bott2.add(playB2,c);
	    pausB2 = newButton("Pause", false);
	    pan_bott2.add(pausB2,c);
	    c.gridheight = c.REMAINDER;
	    saveB2 = newButton("Save", false);
	    pan_bott2.add(saveB2,c);
	    //  Creo il pannello per la visualizzazione dell'onda effetti
	    pan_onda_out = new JPanel();
	    pan_onda_out.setBorder(new CompoundBorder(cb, new EmptyBorder(5,5,5,5)));
	    pan_onda_out.setLayout(new GridBagLayout());
	    samplingGraphEffect = new SamplingGraph("GraficoEffetti");
	    samplingGraphEffect.setPreferredSize(new Dimension(500,220));
	    samplingGraphEffect.setMinimumSize(new Dimension(500,220));
	    c = new GridBagConstraints();
	    c.fill = c.BOTH;
	    c.gridheight = c.REMAINDER;
	    pan_onda_out.add(samplingGraphEffect,c);
	    c.gridwidth = c.REMAINDER;
	    pan_onda_out.add(pan_bott2,c);

		setEffettiBase(effetti);

	    //  Creo pannello di visualizzazione spettro
		spetrometro = new Spetrometro("com.github.michelemilesi.university.music.Audio");
	    spetrometro.setBorder(new EtchedBorder());
		Dimension dim = spetrometro.getPreferredSize();
		dim.height = 100;
		spetrometro.setPreferredSize(dim);
	    //  Creo pannello info file
	    pan_file = new JPanel();
	    pan_file.setBorder(new EtchedBorder());
	    pan_file.setLayout(new GridBagLayout());
	    c = new GridBagConstraints();
	    c.gridx = 1; c.gridy = 1;
		
		String[] nomifile = { "image/dog_fr1.gif", "image/dog_fr2.gif", "image/dog_fr3.gif", "image/dog_fr4.gif" };
		animazione = new Immagine(nomifile,"com.github.michelemilesi.university.music.Immagine non trovata",116,62);
		pan_file.add(animazione);

	    //  Aggancio i componenti (come Jeeg)
	    JPanel psx = new JPanel();
	    JPanel pdx = new JPanel();
	    psx.setLayout(new BoxLayout(psx, BoxLayout.Y_AXIS));
	    pdx.setLayout(new BoxLayout(pdx, BoxLayout.Y_AXIS));
	    psx.setBorder(new EtchedBorder());
	    psx.add(formatControls);
	    psx.add(pan_file);
	    psx.add(spetrometro);
	    pdx.add(pan_onda_input);
	    pdx.add(pan_onda_out);
	    

	    Dimension d = formatControls.getPreferredSize();
	    d.height *= 2;	d.width += 10;
	    psx.setMaximumSize(d);
	    psx.setMinimumSize(d);
	    setLayout(new GridBagLayout());
	    c = new GridBagConstraints();
	    c.fill = c.BOTH;
	    c.weightx = 1;
	    c.weighty = 1;
	    c.gridheight = c.REMAINDER;
	    add(psx,c);
	    c.weightx = 53;
	    c.gridwidth = c.RELATIVE;
	    add(pdx,c);
    }


    public void open() { }


    public void close() {
        if (playback.thread != null) {
            playB.doClick(0);
        }
        if (capture.thread != null) {
            captB.doClick(0);
        }
        if (playbackeffectwave.thread != null) {
            playB2.doClick(0);
        }
    }


    private JButton newButton(String name, boolean state) {
        JButton b = new JButton(name);
        b.addActionListener(this);
        b.setEnabled(state);
        return b;
    }

	private void setButtonEffects(int value) {
		switch (value) {
			case ALLON :	playB2.setEnabled(true);
							pausB2.setEnabled(false);
							saveB2.setEnabled(true);
							aplyE.setEnabled(true);
							statebefore = ALLON;
						break;
			case ALLOFF :	playB2.setEnabled(false);
							pausB2.setEnabled(false);
							saveB2.setEnabled(false);
							aplyE.setEnabled(false);
						break;
			case EFFON :	playB2.setEnabled(false);
							pausB2.setEnabled(false);
							saveB2.setEnabled(false);
							aplyE.setEnabled(true);
							statebefore = EFFON;
						break;
		}
	}


    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
		if (obj.equals(aplyE)) {						//  Tasto applica Effetti
			Container f;
			f = this.getParent();
			while ( !(f instanceof Frame) )
				f = f.getParent();
			audioEffectStream = effetti.applyEffects(audioWavStream, (Frame) f);
			if (audioEffectStream != null) {
				// clipping
				audioEffectStream.rescale(true);
				samplingGraphEffect.createWaveForm(audioEffectStream);
				setButtonEffects(ALLON);
			}
		} else if (obj.equals(efftB)) {					//  Tasto gestione Effetti
			finestra_dialogo.show();
		} else if (obj.equals(playB)) {					//  Tasto Play wave
			playbackeffectwave.stop();
			samplingGraphEffect.stop();
            if (playB.getText().startsWith("Play")) {
				spetrometro.createSpectre(audioWavStream);
				mutex.stato = true;
                samplingGraph.start();     //  start grafico
                playback.start();          //  start audio
				spetrometro.start();       //  start spettro
				animazione.start();        //  start animazione
                captB.setEnabled(false);
				loadB.setEnabled(false);
                pausB.setEnabled(true);
                playB.setText("Stop");
				setButtonEffects(ALLOFF);
            } else {
                samplingGraph.stop();      //  stop grafico
				playback.line.stop();
				playback.line.flush();
		        playback.line.start();
				pausB.setText("Pause");
                playback.stop();           //  stop audio
				spetrometro.stop();        //  stop spettro
				spetrometro.clearSpectre();
				animazione.stop();         //  stop animazione
                captB.setEnabled(true);
				loadB.setEnabled(true);
                pausB.setEnabled(false);
                playB.setText("Play");
				setButtonEffects(statebefore);
            }
        } else if (obj.equals(captB)) {					//  Tasto Record
            if (captB.getText().startsWith("Record")) {
                file = null;
                capture.start();
                fileName = "untitled";
                samplingGraph.start();
                loadB.setEnabled(false);
                playB.setEnabled(false);
                pausB.setEnabled(true);
                captB.setText("Stop");
				setButtonEffects(ALLOFF);
            } else {
                samplingGraph.clearGraph();
                capture.stop();
                samplingGraph.stop();
                loadB.setEnabled(true);
                playB.setEnabled(true);
                pausB.setEnabled(false);
                captB.setText("Record");
				setButtonEffects(EFFON);
            }
        } else if (obj.equals(pausB)) {					//  Tasto Pause-wave
            if (pausB.getText().startsWith("Pause")) {
                if (capture.thread != null) {
                    capture.line.stop();
                } else {
                    if (playback.thread != null) {
                        playback.line.stop();
                    }
                }
                pausB.setText("Resume");
            } else {
                if (capture.thread != null) {
                    capture.line.start();
                } else {
                    if (playback.thread != null) {
                        playback.line.start();
                    }
                }
                pausB.setText("Pause");
            }
        } else if (obj.equals(loadB)) {					//  Tasto Load
            try {
                File file = new File(System.getProperty("user.dir")+"/"+audioDirectory);
                JFileChooser fc = new JFileChooser(file);
                fc.setFileFilter(new javax.swing.filechooser.FileFilter () {
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
	                    String name = f.getName().toLowerCase();
                        if (name.endsWith(".wav")) {
                            return true;
                        }
                        return false;
                    }
                    public String getDescription() {
                        return ".wav";
                    }
                });
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    createAudioInputStream(fc.getSelectedFile(), true);
					setButtonEffects(EFFON);
					samplingGraphEffect.clearGraph();
                }
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (obj.equals(playB2)) {				//  Tasto Play-Effetto
	        playback.stop();
	        samplingGraph.stop();
	        if (playB2.getText().startsWith("Play")) {
				spetrometro.createSpectre(audioEffectStream);
				mutex.stato = true;
	            samplingGraphEffect.start();				
	            playbackeffectwave.start();
				spetrometro.start();
				animazione.start();
				playB.setEnabled(false);
				captB.setEnabled(false);
				pausB.setEnabled(false);
				loadB.setEnabled(false);
				aplyE.setEnabled(false);
	            saveB2.setEnabled(false);
	            pausB2.setEnabled(true);
	            playB2.setText("Stop");
	        } else {
	            samplingGraphEffect.stop();
	            playbackeffectwave.line.stop();
	            playbackeffectwave.line.flush();
	            playbackeffectwave.line.start();
	            pausB2.setText("Pause");
	            playbackeffectwave.stop();
				spetrometro.stop();
				spetrometro.clearSpectre();
				animazione.stop();
	            playB.setEnabled(true);
	            captB.setEnabled(true);
	            pausB.setEnabled(false);
	            loadB.setEnabled(true);
	            aplyE.setEnabled(true);
	            saveB2.setEnabled(true);
	            pausB2.setEnabled(false);
	            playB2.setText("Play");
	        }
        } else if (obj.equals(pausB2)) {				//  Tasto Pause-Effetto
	        if (pausB2.getText().startsWith("Pause")) {
                if (playbackeffectwave.thread != null) {
                    playbackeffectwave.line.stop();
                }
	            pausB2.setText("Resume");
	        } else {
                if (playbackeffectwave.thread != null) {
                    playbackeffectwave.line.start();
                }
	            pausB2.setText("Pause");
	        }
        } else if (obj.equals(saveB2)) {				//  Tasto Save-Effetto
	        try {
	            File file = new File(System.getProperty("user.dir")+"/"+audioDirectory);
	            JFileChooser fc = new JFileChooser(file);
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
	            fc.setFileFilter(new javax.swing.filechooser.FileFilter () {
	                public boolean accept(File f) {
	                    if (f.isDirectory()) {
	                        return true;
	                    }
	                    String name = f.getName().toLowerCase();
	                    if (name.endsWith(".wav")) {
	                        return true;
	                    }
	                    return false;
	                }
	                public String getDescription() {
	                    return ".wav";
	                }
	            });
	            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String nome_file = fc.getSelectedFile().toString();
					if (!nome_file.toLowerCase().endsWith(".wav"))
						nome_file = nome_file.concat(".wav");
					File fsave = new File(nome_file);
					if (fsave.exists()) {
						String msg = "File esistente!\nVuoi sovvrascrivere?";
						int scelta = JOptionPane.showConfirmDialog(null, msg, "Avviso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (scelta == JOptionPane.NO_OPTION)
							return;
					}
		            saveToFile(fsave, audioEffectStream, AudioFileFormat.Type.WAVE);
	            }
	        } catch (SecurityException ex) {
	            ex.printStackTrace();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
        }
    }


    public void createAudioInputStream(File file, boolean updateComponents) {
        AudioInputStream audioInputStream;
        AudioFileFormat audioFileFormat;
        AudioFormat audioFormat;
		byte[] audioBytes;
		
        if (file != null && file.isFile()) {
            try {
                this.file = file;
                errStr = null;
                playB.setEnabled(true);
                fileName = file.getName();
				audioFileFormat = AudioSystem.getAudioFileFormat(file);
                audioInputStream = AudioSystem.getAudioInputStream(file);
				audioFormat = audioFileFormat.getFormat();
				//  Ottengo la descrizione del file wav
				file_format_description = audioFormat.toString();
				String type = audioFileFormat.getType().toString();  // "WAVE"
				int bit = audioFormat.getSampleSizeInBits();  // 8,16
				double rate = audioFormat.getFrameRate();  // 11kHz,22KHz,44KHz
				boolean le = !audioFormat.isBigEndian();
				boolean signed, stereo;
				if(audioFormat.getEncoding()==AudioFormat.Encoding.PCM_SIGNED)
					signed = true;
				else   signed = false;
				stereo = (audioFormat.getChannels()==2) ? true : false;
				audioWavStream = Sound.getDescrictor(type, bit, rate, stereo, le, signed);
				//  Determino la dimensione dello stream in memoria
				int num_byte_samples = (int) Math.ceil(audioWavStream.getBit()/8.0);
				int size = (int) audioInputStream.getFrameLength()*audioFormat.getFrameSize() / 
								(num_byte_samples*audioFormat.getChannels());
				audioWavStream.setSize(size);
				//  Determino la durata del brano audio in secondi
                duration = ((int)((audioInputStream.getFrameLength()*100) / rate)) / 100.0;
				//  Creo buffer di lettura frame/byte
				audioBytes = new byte[frameBuffer * audioFormat.getFrameSize()];
				try {
					int frame_size = audioFormat.getFrameSize();  // = canali*bytexsample
					int numBytesRead = 0;
					byte data[] = new byte[frame_size];					
					while ( (numBytesRead=audioInputStream.read(audioBytes)) != -1 ) {
						for (int i=0; (i<numBytesRead)&&(i<audioBytes.length); ) {
							System.arraycopy(audioBytes, i, data, 0, frame_size);
							i = i + frame_size;
							audioWavStream.addByte(data);	// scrittura di un frame
						}
					}
				} catch (IOException ex) { 
				}
                if (updateComponents) {
                    formatControls.setFormat(audioFormat);
                    samplingGraph.createWaveForm(audioWavStream);
                }
            } catch (Exception ex) { 
                reportStatus(ex.toString());
            }
        } else {
            reportStatus("com.github.michelemilesi.university.music.Audio file required.");
        }
    }


    public void saveToFile(File file, Sound audio, AudioFileFormat.Type fileType) {

        if (audio == null) {
            reportStatus("Non c'� nulla da salvare.");
            return;
        }
        // reset all'inizio dello stream audio
        audio.reset();
        // Crea il specifico formato audio per l'ascolto
        AudioFormat format = new AudioFormat( (float)audioWavStream.getRate(), 
        									audioWavStream.getBit(), (audioWavStream.isStereo() ? 2 : 1), 
        									audioWavStream.isSigned(), !audioWavStream.isLittleEndian() );
		// carica i bytes nell'audio input stream
		byte[] audioBytes = audio.getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
		AudioInputStream ais = new AudioInputStream(bais, format, audioWavStream.getSize());
		
        try {
            if (AudioSystem.write(ais, fileType, file) == -1) {
                throw new IOException("Errore nella scrittura su file.");
            }
        } catch (Exception ex) { reportStatus(ex.toString()); }
        samplingGraphEffect.repaint();
    }
        

    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.out.println(errStr);
			samplingGraph.repaint();
			samplingGraphEffect.repaint();
        }
    }


    /**
     * Esegue il playback dello stream audio
     */
    public class Playback implements Runnable {

        SourceDataLine line;
        Thread thread;
		int delay;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Playback");
			thread.setPriority(Thread.NORM_PRIORITY+2);
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
				spetrometro.stop();
				animazione.stop();
                captB.setEnabled(true);
                loadB.setEnabled(true);
                pausB.setEnabled(false);
                playB.setText("Play");
				setButtonEffects(statebefore);
            } 
        }

        public void run() {

            // Si assicura che ci sia qualcosa da suonare
            if (audioWavStream == null) {
                shutDown("Non � stato caricato nessun file audio per la riproduzione.");
                return;
            }
            // reset all'inizio dello stream
            audioWavStream.reset();

            // Crea il specifico formato audio per l'ascolto
			AudioFormat format = new AudioFormat( (float)audioWavStream.getRate(), 
												audioWavStream.getBit(), (audioWavStream.isStereo() ? 2 : 1), 
												audioWavStream.isSigned(), !audioWavStream.isLittleEndian() );
            // Definisce gli attributi richiesti per l'out su linea
            // e si assicura la compatibilit�.
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Tipo di Line-out " + info + " non supportata.");
                return;
            }
            // Ottiene e apre un data line per il playback.
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) { 
                shutDown("Impossibile aprire il line-out: " + ex);
                return;
            }
            // play back dei data audio
			int frameSizeInByte = format.getFrameSize();
			double freq_campionamento =  format.getSampleRate();
            int bufferLengthInFrames = line.getBufferSize() / frameSizeInByte;
			byte[] data;
            // inizia la riproduzione audio
            line.start();
            while (thread != null) {
	            synchronized(mutex) {
	            	if (mutex.stato) {
		                try {
							if ( (data = audioWavStream.getBytes(bufferLengthInFrames)) == null ) {
							    break;
							}
							int numBytesRemaining = data.length;
							while (numBytesRemaining > 0 ) {
							    numBytesRemaining -= line.write(data, 0, numBytesRemaining);
								// nota: delay � sempre 0
								delay = (int) ( (numBytesRemaining / frameSizeInByte) / freq_campionamento * 1000 );
							}
						} catch (Exception e) {
							shutDown("Errore durante la riproduzione audio: " + e);
							break;
						}
						mutex.stato = false;
					}
				}
				try {
					thread.sleep(delay);
				} catch (Exception e) { break; }
            }
			// Raggiunta la fine dello stream. stop play e chiusura della line
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    } // End class Playback
        

    /**
     * Esegue il playback dello stream audio sul quale � stao applicato l'effetto
     */
    public class PlaybackEffectWave implements Runnable {

        SourceDataLine line;
        Thread thread;
		int delay;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("PlaybackEffectWave");
			thread.setPriority(Thread.NORM_PRIORITY+2);
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraphEffect.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraphEffect.stop();
				spetrometro.stop();
				animazione.stop();
                playB.setEnabled(true);
                captB.setEnabled(true);
                pausB.setEnabled(false);
                loadB.setEnabled(true);
                aplyE.setEnabled(true);
                saveB2.setEnabled(true);
                pausB2.setEnabled(false);
                playB2.setText("Play");
            } 
        }

        public void run() {

            // Si assicura che ci sia qualcosa da suonare
            if (audioEffectStream == null) {
                shutDown("Non � stato caricato nessun file audio per la riproduzione.");
                return;
            }
            // reset all'inizio dello stream
            audioEffectStream.reset();

            // Ottiene un AudioInputStream del formato desiderato per l'ascolto
    		AudioFormat format = new AudioFormat( (float)audioEffectStream.getRate(), 
    											audioEffectStream.getBit(), (audioEffectStream.isStereo() ? 2 : 1), 
    											audioEffectStream.isSigned(), !audioEffectStream.isLittleEndian() );
            // Definisce gli attributi richiesti per l'out su linea
            // e si assicura la compatibilit�.
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Tipo di Line-out " + info + " non supportata.");
                return;
            }
            // Ottiene e apre un data line per il playback.
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) { 
                shutDown("Impossibile aprire il line-out: " + ex);
                return;
            }
            // play back dei data audio
            int frameSizeInByte = format.getFrameSize();
            double freq_campionamento =  format.getSampleRate();
            int bufferLengthInFrames = line.getBufferSize() / frameSizeInByte;
    		byte[] data;
            // inizia la riproduzione audio
            line.start();
            while (thread != null) {
	            synchronized(mutex) {
	            	if (mutex.stato) {
		                try {
		                    if ( (data = audioEffectStream.getBytes(bufferLengthInFrames)) == null ) {
		                        break;
		                    }
		                    int numBytesRemaining = data.length;
		                    while (numBytesRemaining > 0 ) {
		                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
		                        // nota: delay � sempre 0
		                        delay = (int) ( (numBytesRemaining / frameSizeInByte) / freq_campionamento * 1000 );
		                    }
		                } catch (Exception e) {
		                    shutDown("Errore durante la riproduzione audio: " + e);
		                    break;
		                }
	            	}
					mutex.stato = false;
	            }
	            try {
	            	thread.sleep(delay);
	            } catch (Exception e) { break; }
            }
    		// Raggiunta la fine dello stream. stop play e chiusura della line
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    } // End class PlaybackEffectWave
        

    /** 
     * Reads data from the input channel and writes to the output stream
     */
    public class Capture implements Runnable {

        TargetDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                samplingGraph.stop();
                loadB.setEnabled(true);
                playB.setEnabled(true);
                pausB.setEnabled(false);
                captB.setText("Record");
                System.err.println(errStr);
                samplingGraph.repaint();
            }
        }

        public void run() {

            duration = 0;
            audioWavStream = null;

			// verifica che sia possibile la registrazione
            AudioFormat format = formatControls.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line audio tipo " + info + " non supportato.");
                return;
            }

            // Apre una line per la cattura audio
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) { 
                shutDown("Impossibile aprire la line: " + ex);
                return;
            } catch (SecurityException ex) { 
                shutDown(ex.toString());
                String msg = "Mancano i privilegi di apertura line audio!";
                JOptionPane.showConfirmDialog(null, msg, "Avviso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                return;
            } catch (Exception ex) { 
                shutDown(ex.toString());
                return;
            }

/*            // play back the captured audio data
            int frameSizeInBytes = format.getFrameSize();
            byte[] data = new byte[frameSizeInBytes];
            int numBytesRead;
            line.start();
		
            while (thread != null) {
				//  leggo un frame alla volta
                if((numBytesRead = line.read(data, 0, frameSizeInBytes)) == -1) {
                    break;
                }
				
				audioWavStream.setSize(audioWavStream.getSize()+1);
				audioWavStream.addByte(data);
            }
            // raggiunta la fine dello stream.  stop e chiudi la line.
            line.stop();
            line.close();
            line = null;
*/

/*            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            byte[] frame = new byte[frameSizeInBytes];
            int numBytesRead;
			
            line.start();

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
				for (int i=0; i<numBytesRead; ) {
					System.arraycopy(data, i, frame, 0, frameSizeInBytes);
					i = i + frameSizeInBytes;
					audioWavStream.setSize(audioWavStream.getSize()+1);
            		audioWavStream.addByte(frame);	// scrittura di un frame
				}
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;
*/

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / format.getFrameSize();
            byte[] data = new byte[bufferLengthInFrames];
            int numBytesRead;
            
            line.start();

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInFrames)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;

            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // load bytes into the audio input stream for playback

            byte audioBytes[] = out.toByteArray();

            //  Ottengo la descrizione del file wav
            String type = "WAVE";  // WAVE
            int bit = format.getSampleSizeInBits();  // 8,16
			int nByte = (int) Math.ceil(bit / 8.0);
            double rate = format.getFrameRate();  // 11kHz,22KHz,44KHz
            boolean le = !format.isBigEndian();
            boolean signed, stereo;
            if(format.getEncoding()==AudioFormat.Encoding.PCM_SIGNED)
            	signed = true;
            else   signed = false;
            stereo = (format.getChannels()==2) ? true : false;
            audioWavStream = Sound.getDescrictor(type, bit, rate, stereo, le, signed);

			byte[] frame_buff = new byte[frameSizeInBytes];
			audioWavStream.setSize( audioBytes.length / frameSizeInBytes );
        	for (int i=0; i<audioBytes.length; ) {
        		System.arraycopy(audioBytes, i, frame_buff, 0, frameSizeInBytes);
        		i = i + frameSizeInBytes;
        		audioWavStream.addByte(frame_buff);	// scrittura di un frame
        	}

        	//  Determino la durata del brano audio in secondi
        	duration = ((int)(( audioBytes.length/(format.getChannels()*nByte)*100) / rate)) / 100.0;

			samplingGraph.createWaveForm(audioWavStream);
        }
    } // End class Capture
 

    /**
     * Controls for the AudioFormat.
     */
    public class FormatControls extends JPanel {
    
        Vector groups = new Vector();
		JCheckBox rate8B, rate11B, rate16B, rate22B, rate44B;
        JCheckBox size8B, size16B, monoB, sterB;
	    GridBagConstraints c;
		
        public FormatControls() {

            EmptyBorder eb = new EmptyBorder(0,0,0,0);
            BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
            CompoundBorder cb = new CompoundBorder(eb, bb);
            setBorder(new CompoundBorder(cb, new EmptyBorder(8,5,5,5)));

            JPanel p2 = new JPanel();
			p2.setBorder(new EtchedBorder());
			p2.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = GridBagConstraints.REMAINDER;
            ButtonGroup sampleRateGroup = new ButtonGroup();
			c.insets = new Insets(0,5,-3,0);
            rate8B = addJCheckBox(p2, sampleRateGroup, c, "8000", false);
			c.insets = new Insets(-3,5,-3,0);
            rate11B = addJCheckBox(p2, sampleRateGroup, c, "11025", false);
            rate16B = addJCheckBox(p2, sampleRateGroup, c, "16000", false);
            rate22B = addJCheckBox(p2, sampleRateGroup, c, "22050", false);
			c.insets = new Insets(-3,5,0,0);			
            rate44B = addJCheckBox(p2, sampleRateGroup, c, "44100", true);
            groups.addElement(sampleRateGroup);

            JPanel p3 = new JPanel();
			p3.setBorder(new EtchedBorder());
			p3.setLayout(new GridBagLayout());
            ButtonGroup sampleSizeInBitsGroup = new ButtonGroup();
            c.insets = new Insets(0,5,-3,0);
            size8B = addJCheckBox(p3, sampleSizeInBitsGroup, c, "8", false);
            c.insets = new Insets(-3,5,7,0);			
            size16B = addJCheckBox(p3, sampleSizeInBitsGroup, c, "16", true);
            groups.addElement(sampleSizeInBitsGroup);
            ButtonGroup channelsGroup = new ButtonGroup();
            c.insets = new Insets(6,5,-3,0);
            monoB = addJCheckBox(p3, channelsGroup, c, "mono", false);
			c.insets = new Insets(-3,5,0,0);
            sterB = addJCheckBox(p3, channelsGroup, c, "stereo", true);
            groups.addElement(channelsGroup);

			setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel("FORMATO FILE AUDIO"),c);
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(p2,c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(p3,c);
        }

    
        private JCheckBox addJCheckBox(JPanel p, ButtonGroup g, GridBagConstraints c, String name, boolean state) {
            JCheckBox b = new JCheckBox(name, state);
            p.add(b,c);
            g.add(b);
            return b;
        }

		//  restituisce il formato audio, scelto per il campionamento
        public AudioFormat getFormat() {

			//  crea il vettore con le etichette dei bottoni scelti
            Vector v = new Vector(groups.size());
            for (int i = 0; i < groups.size(); i++) {
                ButtonGroup g = (ButtonGroup) groups.get(i);
                for (Enumeration e = g.getElements(); e.hasMoreElements(); ) {
                    AbstractButton b = (AbstractButton) e.nextElement();
                    if (b.isSelected()) {
                        v.add(b.getText());
                        break;
                    }
                }
            }
            String encString = "linear";
			//  8,10,16,22,44 khz
            float rate = Float.valueOf((String) v.get(0)).floatValue();
			//  8,16 bit
            int sampleSize = Integer.valueOf((String) v.get(1)).intValue();
			String signedString = "signed";
			boolean bigEndian = false;
            int channels = ((String) v.get(2)).equals("mono") ? 1 : 2;
			AudioFormat.Encoding encoding;
            if (encString.equals("linear")) {
                if (signedString.equals("signed")) {
                    encoding = AudioFormat.Encoding.PCM_SIGNED;
                } else {
                    encoding = AudioFormat.Encoding.PCM_UNSIGNED;
                }
            } else if (encString.equals("alaw")) {
                encoding = AudioFormat.Encoding.ALAW;
            } else  encoding = AudioFormat.Encoding.ULAW;

            return(new AudioFormat(encoding, rate, sampleSize, 
                          channels, (sampleSize/8)*channels, rate, bigEndian));
        }


        public void setFormat(AudioFormat format) {
		
            float rate = format.getFrameRate();
            if (rate == 8000) {
				rate8B.setSelected(true);
            } else if (rate == 11025) {
				rate11B.setSelected(true);
            } else if (rate == 16000) {
				rate16B.setSelected(true);				
            } else if (rate == 22050) {
				rate22B.setSelected(true);
            } else if (rate == 44100) {
				rate44B.setSelected(true);
            }
            switch (format.getSampleSizeInBits()) {
                case 8  : size8B.setSelected(true); break;
                case 16 : size16B.setSelected(true); break;
            }
            if (format.getChannels() == 1) {
                monoB.setSelected(true); 
            } else { 
                sterB.setSelected(true);
            }
        }
    } // End class FormatControls


    /**
     * Render a WaveForm.
     */
    public class SamplingGraph extends JPanel implements Runnable {

        private Thread thread;
		private String nome;
        private Font font10 = new Font("serif", Font.PLAIN, 10);
        private Font font12 = new Font("serif", Font.PLAIN, 12);
		private double seconds;					// istante di playback
	    private double durata;					// durata del brano in secondi
	    private Vector lines, canaleSXlines, canaleDXlines; // vett. delle linee da disegnare
		private Sound audio;
		private final int INFOPAD = 15;
        Color jfcBlue = new Color(204, 204, 255);
        Color pink = new Color(255, 175, 175);
 

        public SamplingGraph(String nome) {
			this.nome = nome;
			durata = 0.0;
			lines = new Vector();
			canaleSXlines = new Vector();
			canaleDXlines = new Vector();
            setBackground(new Color(20, 20, 20));
        }

		public void clearGraph() {
			lines.removeAllElements();  // clear the old vector
			canaleSXlines.removeAllElements();
			canaleDXlines.removeAllElements();
			durata = 0.0;
			repaint();
		}

        public void createWaveForm(Sound audio) {

			this.durata = duration;
			// clear the old vector
            lines.removeAllElements();
            canaleSXlines.removeAllElements();
            canaleDXlines.removeAllElements();
            if (audio == null) {
            	System.out.println("Impossibile visualizzare il grafico dell'onda!");
				return;
            }
			audio.reset();
			this.audio = audio;
            Dimension d = getSize();
            int w = d.width;
            int h = d.height-INFOPAD;
			int cornice = 10;

			if (audio.isStereo()) {
				int cornice_canale = 10;
				double y_sx_last = 0;
				double y_dx_last = 0;
				double y_sx_new, y_dx_new;
				int idx;
				int h_canale = h / 2 - cornice_canale;
				double[] audioSXData = ((StereoSound)audio).getSxValues();
				double[] audioDXData = ((StereoSound)audio).getDxValues();
				int frames_per_pixel = audio.getSize() / w;
				int k = ((h_canale-cornice_canale) / 2);
				int c = (cornice_canale/2) + (cornice/2);
				int c2 = (cornice/2) + h_canale + cornice_canale;
				for (double x=0; x<w; x++) {
					idx = (int) (frames_per_pixel * x);
					y_sx_new = (audioSXData[idx] + 1) * k + c;
					canaleSXlines.add(new Line2D.Double(x, y_sx_last, x, y_sx_new));
					y_sx_last = y_sx_new;
					y_dx_new = (audioDXData[idx] + 1) * k + c2;
					canaleDXlines.add(new Line2D.Double(x, y_dx_last, x, y_dx_new));
					y_dx_last = y_dx_new;
				}
			} else {
				double y_last = 0;
				double y_new;
				int idx;
				double[] audioData = audio.getValues();
				int frames_per_pixel = audioData.length / w;
				int k = ((h-cornice) / 2);
				int c = (cornice/2);
				for (double x=0; x<w; x++) {
	                idx = (int) (frames_per_pixel * x);
					y_new = (audioData[idx] + 1) * k + c;
					lines.add(new Line2D.Double(x, y_last, x, y_new));
					y_last = y_new;
				}
			}
            repaint();
        }


        public void paint(Graphics g) {

            Dimension d = getSize();
            int w = d.width;
            int h = d.height;

            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
            g2.setColor(Color.white);
            g2.fillRect(0, h-INFOPAD, w, INFOPAD);

            if (errStr != null) {
                g2.setColor(jfcBlue);
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.drawString("ERROR", 5, 20);
                AttributedString as = new AttributedString(errStr);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                AttributedCharacterIterator aci = as.getIterator();
                FontRenderContext frc = g2.getFontRenderContext();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    TextLayout tl = lbm.nextLayout(w-x-5);
                    if (!tl.isLeftToRight()) {
                        x = w - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (capture.thread != null) {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Length: " + String.valueOf(seconds), 3, h-4);
            } else {
                g2.setColor(Color.black);
                g2.setFont(font12);
				if ( nome.compareToIgnoreCase("GraficoEffetti") == 0 )
					g2.drawString("Length: " + String.valueOf(durata) + "  Position: " + String.valueOf(seconds), 3, h-4);
				else
					g2.drawString("File: " + fileName + "  Length: " + String.valueOf(duration) + "  Position: " + String.valueOf(seconds), 3, h-4);
                if (audio != null) {
                    // .. render sampling graph ..
					if (audio.isStereo()) {
						for (int i = 1; i < canaleSXlines.size(); i++) {
							g2.setColor(Color.blue);
						    g2.draw((Line2D) canaleSXlines.get(i));
						    g2.setColor(Color.red);
						    g2.draw((Line2D) canaleDXlines.get(i));
						}
					} else {
						g2.setColor(Color.blue);
						for (int i = 1; i < lines.size(); i++) {
						    g2.draw((Line2D) lines.get(i));
						}
					}
                    // .. draw current position ..
                    if (seconds != 0) {
                        double loc = seconds/duration*w;
                        g2.setColor(pink);
                        g2.setStroke(new BasicStroke(2));
                        g2.draw(new Line2D.Double(loc, 0, loc, h-INFOPAD-2));
                    }
                }
            }
        }
    
        public void start() {
            thread = new Thread(this);
            thread.setName(nome);
            thread.start();
            seconds = 0;
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
            seconds = 0;
            while (thread != null) {
                if ((playback.line != null) && (playback.line.isOpen()) ) {

                    long milliseconds = (long)(playback.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                } else if ( (capture.line != null) && (capture.line.isActive()) ) {

                    long milliseconds = (long)(capture.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                } else if ((playbackeffectwave.line != null) && (playbackeffectwave.line.isOpen()) ) {

                    long milliseconds = (long)(playbackeffectwave.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                }

                try { thread.sleep(100); } catch (Exception e) { break; }

                repaint();
                                
                while ((capture.line != null && !capture.line.isActive()) ||
                       (playback.line != null && !playback.line.isOpen()) ||
					   (playbackeffectwave.line != null && !playbackeffectwave.line.isOpen()) ) 
                {
                    try { thread.sleep(10); } catch (Exception e) { break; }
                }
            }
            seconds = 0;
            repaint();
        }
    } // End class SamplingGraph


    /**
     *  Visualizza lo spettro
     */
    public class Spetrometro extends JPanel implements Runnable {
	    private final double refresh_monitor = 75.0;
	    private final double ampiezza_max = 1;
	    private final int margine_w = 14;
	    private final int margine_h = 15;
        private final int n_campioni = 32;		// per la FFT (usare potenze di 2)
		private	int lines_start, lines_stop;
		private int delay;	
		private Thread thread;
    	private String nome;
        private Vector lines, lines_bar;        // vett. delle linee da disegnare
	   	private Sound audio;
		private Color azzurro_met = new Color(77, 204, 253);

        public Spetrometro(String nome) {
    		this.nome = nome;
    		lines = new Vector();
			lines_bar = new Vector();
            setBackground(Color.black);
        }

    	public void clearSpectre() {
    		repaint();
    	}

    	public void resetSpectre() {
    		lines.removeAllElements();  // clear the old vector
            lines_bar.removeAllElements();
    		repaint();
    	}

        public void createSpectre(Sound source) {

			Spectrum[] spettro;
	        Sample[] sampleList, toFrame;
	        Dimension d = getSize();
			double fc,ff,tcamp;
	        int w = d.width-margine_w;
	        int h = d.height-margine_h;
			int n_media;				//  n. di campioni da mediare
			int n_freq;					//  n. di freq. campionate da ff a fc/2

    		// svuota i vecchi dati se ci sono...
			resetSpectre();
            if (source == null) {
            	System.out.println("Impossibile visualizzare lo spettro!");
    			return;
            }
			this.audio = source;
			//  Ricava lo spettro...
			source.reset();
			fc = source.getRate();         //  freq. di campioinamento
			ff = fc / n_campioni;          //  freq. fondamentale
			tcamp = n_campioni / fc;       //  durata dei campioni in sec.
			n_media = (int) Math.round((1.0/refresh_monitor)/tcamp);
			n_freq = (int) ((fc / 2) / ff);
			//  gestione grafica
			int base = w/n_freq - 1;
			if (base<1)  base = 1;
			delay = (int) (1.0 / refresh_monitor * 1000);
			sampleList = new Sample[n_campioni];

			ArrayList arl = new ArrayList();
			
			int start, end;
			
			start = 0;
			
			while (start<source.getSize()) {
				end = start + n_campioni;
				if (end > source.getSize()) {
					end = source.getSize();
					start = end - n_campioni;
				}
				for (int i=0; i<n_campioni; i++) {
					sampleList[i] = new Sample();
					sampleList[i].real = source.getValue(start+i);
				}
				toFrame = Fourier.fft(sampleList, false);
				spettro = Fourier.getSpectrum(toFrame, fc);
				arl.add(spettro);
				start = end;
			}
			
			int n = (int) Math.ceil((double)arl.size() / n_media);
			Spectrum[] spt_media;

			for (int i=0; i<n; i++) {
				int j;
				spt_media = new Spectrum[n_campioni];
				for (j=0; j<n_campioni; j++)
					spt_media[j] = new Spectrum();
				for (j=0; (i*n_media+j < arl.size()) && (j<n_media); j++ ) {
					spettro = (Spectrum[]) arl.get(i*n_media+j);
					for (int k=0; k<n_campioni; k++)
						spt_media[k].amplitude += spettro[k].amplitude;
				}
				for (int k=0; k<n_campioni; k++)
					spt_media[k].amplitude /= j;

				//  conversione coordinate grafiche grafiche
				double x = margine_w/2 +1;
				double y0, altezza;
				for (int k=1; k<=(spt_media.length/2); k++) {
					if (spt_media[k].amplitude < ampiezza_max)
						altezza = ( h * spt_media[k].amplitude ) / ampiezza_max;
					else  altezza = h;
					y0 = h - altezza + margine_h/2 +2;
					lines.add(new Rectangle2D.Double(x, y0, base, altezza));
					lines_bar.add(new Line2D.Double(x, y0, x+base, y0));
					x += base + 1;
				}
			}
            repaint();
        }


        public void paint(Graphics g) {

            Dimension d = getSize();
            int w = d.width;
            int h = d.height;

            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
			g2.setColor(Color.white);
			g2.drawRect(2,2,d.width-5,d.height-5);
			g2.drawRect(3,3,d.width-7,d.height-7);
            if ( (errStr == null) && ( (playback.line != null) && playback.line.isOpen() ) ||
			                         ( (playbackeffectwave.line != null) && playbackeffectwave.line.isOpen() ) ) {
	            // .. disegna spettro ..
				for (int i = lines_start; (i<lines_stop)&&(i<lines.size()); i++) {
					g2.setColor(azzurro_met);
				    g2.fill( (Rectangle2D) lines.elementAt(i) );
					g2.setColor(Color.white);
					g2.draw( (Line2D) lines_bar.elementAt(i));
				}
            }
        }
    
		
        public void start() {
            thread = new Thread(this);
            thread.setName(nome);
            thread.start();
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
	        lines_start = 0;
	        lines_stop = n_campioni/2;
            while (thread != null) {
				if ( ( (playback.line != null) && playback.line.isOpen() ) ||
			         ( (playbackeffectwave.line != null) && playbackeffectwave.line.isOpen() ) ) {
					synchronized(mutex) {
						if (!mutex.stato) {
							lines_start = lines_stop;
							lines_stop += n_campioni/2;
							mutex.stato = true;
						}
					}
					repaint();
				}					
                try {
					thread.sleep(delay);
				} catch (Exception e) { break; }
            }
			clearSpectre();
            repaint();
        }
    } // End class Spetrometro


	public void setEffettiBase(Effects effetti) {
		this.effetti = effetti;
		if (effetti == null) {
			pan_onda_out.setVisible(false);
			efftB.setVisible(false);
			aplyE.setVisible(false);
		} else {
			pan_onda_out.setVisible(true);
			efftB.setVisible(true);
			aplyE.setVisible(true);
			finestra_dialogo = new EffectsDialog(effetti.getEffetti());
		}
	}


	public class Mutex {

		public boolean stato;

		public Mutex() {
			stato = true;
		}
	}


	
    public static void main(String s[]) {
        Audio capturePlayback = new Audio("sound");
        capturePlayback.open();
        JFrame f = new JFrame("Record/Play");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
        });
        f.getContentPane().add("Center", capturePlayback);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 750;
        int h = 500;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.show();
    }
} 
