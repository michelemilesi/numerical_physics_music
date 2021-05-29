/*
 * Equalizer.java
 *
 * Created on 16 aprile 2002, 12.35
 */

/**
 *
 * @author  michele
 * @version 
 */
public class Equalizer extends FrequencyFilter {
    public final static int FREQUENCIES = OVERLAP + 1;
    public final static int DB_VALUES = OVERLAP + 2;
    
    private double[] freqs;
    private double[] dBs;
    
    /** Creates new Equalizer */
    public Equalizer() {
        freqs = new double[0];
        dBs = new double[0];
    }

    public void setParameter(int param, double value) {
        super.setParameter(param, value);
    }
        
    protected Spectrum[] process(Spectrum[] spectre, double rate) {
        Spectrum[] result;
        double freq, ampl;
        double fA, fB, dBA, dBB, kA, kB, mA, mB;
                
        result = new Spectrum[spectre.length];
        result[0] = new Spectrum();
        result[0].amplitude = spectre[0].amplitude;
        result[0].frequency = spectre[0].frequency; //Sempre 0.0
        result[0].phase = 0.0;
        for (int i = 1; i <= spectre.length / 2; i++) {
            freq = spectre[i].frequency;
            ampl = spectre[i].amplitude;
            
            ampl = ampl;
            
            fA = Double.MIN_VALUE;
            dBA = 0.0;
            fB = freqs[0];
            dBB = dBs[0];
            for (int j = 0; (j < freqs.length - 1) && (freqs[j] < freq); j++) {
                fA = fB;
                dBA = dBB;
                fB = freqs[j + 1];
                dBB = dBs[j + 1];
            }
            
            if (freqs[freqs.length - 1] < freq) {
                fA = fB;
                dBA = dBB;
                dBB = 0;
                fB = rate;
            }
            
            mA = fromdB(dBA);
            mB = fromdB(dBB);
            
            kA = Math.pow(10.0, -Math.abs(log10(freq) - log10(fA)));
            kB = Math.pow(10.0, -Math.abs(log10(freq) - log10(fB)));
            
            if (kA > 0.1) {
                mA = mA * kA;
            } else {
                mA = 1.0;
            }
            
            if (kB > 0.1) {
                mB = mB * kB;
            } else {
                mB = 1.0;
            }
            
            ampl = ampl * mA * mB;
            
            result[i] = new Spectrum();
            result[i].phase = 0.0;
            result[i].frequency = freq;
            result[i].amplitude = ampl;
        }
        return result;
    }//end process(Spectrum[], double)
    
    public double getParameter(int param) {
        double retValue;
    
        retValue = super.getParameter(param);
        return retValue;
    }
    
    public void setParameter(int param, Object obj) {
	switch(param) {
	case FREQUENCIES :
	    if (obj instanceof double[]) {
		freqs = (double[]) obj;
	    } else {
		throw new RuntimeException("Le frequenze devono essere espresse come valori reali");
	    }
	    break;
	case DB_VALUES :
	    if (obj instanceof double[]) {
		dBs = (double[]) obj;
	    } else {
		throw new RuntimeException("I dB devono essere espresie come valori reali");
	    }
	    break;
	default :
	    super.setParameter(param, obj);
	}
	    
    }

    public Object getParameterAsObject(int param) {
	switch(param) {
	case FREQUENCIES :
	    return freqs;
	case DB_VALUES :
		return dBs;
	default :
	    return super.getParameterAsObject(param);
	}
	    
    }
    
    private double log10(double n) {
        return (Math.log(n) / Math.log(10.0));
    }
    
    private double todB(double n) {
        if (n != 0.0) {
            return 20.0 * log10(n);
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    private double fromdB(double db) {
        double n =  Math.pow(10, db / 20.0);
        return n;
    }
    
    public static void main(String[] args) {
        Sound test, result;
        
        test = Sound.getDescrictor("WAVE", 16, 32.0, false, false, true);
        test.setSize(11264);
        for (int i = 0; i < 11264; i++) {
            test.addValue(Math.sin(i * Math.PI / 16));
        }
        
        double[] freq = new double[4];
        double[] dB = new double[4];
        for (int i = 0; i < 4; i++) {
            freq[i] = 1.0 + i * 4.0;
            dB[i] = -6.0;
        }
        
        Equalizer equa = new Equalizer();
        equa.setParameter(equa.FREQUENCIES, freq);
        equa.setParameter(equa.DB_VALUES, dB);
        System.out.println("Inizio");
        result = equa.process(test);
        System.out.println("FIne");
        
        for (int i = 0; i < result.getSize(); i++) {
            System.out.println(test.getValue(i) +  ";" + result.getValue(i));
        }
    }
}
