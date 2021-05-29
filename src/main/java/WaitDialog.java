import java.awt.*;
import javax.swing.*;
import java.io.File;


public class WaitDialog extends JDialog implements Runnable {
	
	private Image immagine;
	private String commento, titolo;
	private Dimension dim;
	private Thread thread;
	private int width, height;
	private int ritardo;
	

	public WaitDialog(Frame owner, String nomeImmagine, String titolo, String commento, int width, int height, int ritardo) {

		File file = new File(nomeImmagine);
		if (file.exists())
			immagine = Toolkit.getDefaultToolkit().getImage(nomeImmagine);
		else {
			System.out.println("File: "+nomeImmagine+" inesistente!");
			System.out.println("- animazione non caricata -");
		}	
		this.titolo = titolo;
		this.commento = commento;
		this.width = width;
		this.height = height;
		dim = new Dimension(width ,height);
//		setPreferredSize(dim);
		setSize(dim);
		this.ritardo = ritardo;
		setTitle(titolo);
		setResizable(false);
//		setModal(true);
	}
	
	public WaitDialog(Frame owner, String nomeImmagine, String commento, int width, int height) {
		this(owner, nomeImmagine, "", commento, width, height, 0);
	}	
	
	public WaitDialog(Frame owner, String nomeImmagine, String titolo, String commento, int width, int height) {
		this(owner, nomeImmagine, titolo, commento, width, height, 0);
	}	

	public WaitDialog(Frame owner, String nomeImmagine, String commento, int width, int height, int ritardo) {
		this(owner, nomeImmagine, "", commento, width, height, ritardo);
	}	

	public void start() {
	    thread = new Thread(this);
//		thread.setPriority(thread.MAX_PRIORITY-1);
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
				thread.sleep(ritardo);
			} catch (Exception e) { break; }
			repaint();
		}
	}
	
	public void paint(Graphics g) {
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setBackground(getBackground());
//		g2.clearRect(0, 0, width, height);
		if (immagine==null)
			g.drawString(commento,10,10);
		else	
			g.drawImage(immagine,0,0,this);
	}
	

//	public void update(Graphics g) {
//		paint(g);
//	}
	
	
}
