import java.text.*;

public class Sample {
    /**
     * Parte reale del campione
     */
    public double real = 0.0;

    /**
     * Parte immaginaria del campione
     */
    public double img = 0.0;

    public Sample() {
	real = 0.0;
	img = 0.0;
    }

    public Sample(Sample from) {
	real = from.real;
	img = from.img;
    }

    public String toString() {
	NumberFormat nf = new DecimalFormat("#0.000");

	return new String("(" + nf.format(real) + ";" + nf.format(img) + ")");
    }
	
}////:~
