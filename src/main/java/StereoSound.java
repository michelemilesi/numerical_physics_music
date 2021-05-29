import java.util.Arrays;

public class StereoSound extends Sound implements StereoSource {
    
    MonoSound dx;
    MonoSound sx;
    private int nByte;
    private int bytePos;
    private byte[] tempByte;
    private boolean dxChannel;

    public StereoSound(String type, int bit, double rate, boolean le, boolean signed) {

		this.dx = new MonoSound(type, bit, rate, le, signed);
		this.sx = new MonoSound(type, bit, rate, le, signed);

		this.type = type;
		this.bit = bit;
		this.rate = rate;
		this.littleEndian = le;
		this.signed = signed;
		this.stereo = true;
		this.size = 0;
	        
		nByte = (int) Math.ceil(bit / 8.0);
		dxChannel = false;
		bytePos = 0;
    }

    public double getValue() {
		return (dx.getValue() + sx.getValue()) / 2.0;
    }

    public double getValue(int at) {
		return (dx.getValue(at) + sx.getValue(at)) / 2.0;
    }
    
    public double[] getValues() {
		double[] dxa, sxa, res;
		
		dxa = dx.getValues();
		sxa = sx.getValues();

		res = new double[dxa.length];

		for (int i = 0; i < dxa.length; i++) {
		    res[i] = (dxa[i] + sxa[i]) /  2.0;
		}

		return res;
    }

    public double[] getValues(int from, int to) {
		double[] dxa, sxa, res;
		
		dxa = dx.getValues(from, to);
		sxa = sx.getValues(from, to);

		res = new double[dxa.length];

		for (int i = 0; i < dxa.length; i++) {
		    res[i] = (dxa[i] + sxa[i]) / 2.0;
		}

		return res;
    }

    public void rescale(boolean clip) {
		dx.rescale(clip);
		sx.rescale(clip);
    }

    public byte[] getBytes() {
		byte[] res;
		byte[] dxa, sxa;
		
		res = new byte[nByte * size * 2];

		dxa = dx.getBytes();
		sxa = sx.getBytes();

		for (int i = 0; i < size; i++) {
		    System.arraycopy(sxa, i * nByte, res, (2 * i) * nByte, nByte);
		    System.arraycopy(dxa, i * nByte, res, (2 * i + 1) * nByte, nByte);
		}

		return res;
    }

	public byte[] getBytes(int nsample) {
		byte[] res;
		byte[] dxa, sxa;
		
		res = new byte[nByte * nsample * 2];
		
		dxa = dx.getBytes(nsample);
		sxa = sx.getBytes(nsample);

		if ( (sxa != null)&&(dxa != null) )	{
			for (int i = 0; i < nsample; i++) {
				System.arraycopy(sxa, i * nByte, res, (2 * i) * nByte, nByte);
				System.arraycopy(dxa, i * nByte, res, (2 * i + 1) * nByte, nByte);
			}
			return res;
		} else
			return null;
	}

	public byte[] getBytes(int from, int to) {
		byte[] res;
		byte[] dxa, sxa;
		
		dxa = dx.getBytes(from, to);
		sxa = sx.getBytes(from, to);
		
		res = new byte[dxa.length * 2];

		for (int i = 0; i < dxa.length/nByte; i++) {
			System.arraycopy(sxa, i * nByte, res, (2 * i) * nByte, nByte);
			System.arraycopy(dxa, i * nByte, res, (2 * i + 1) * nByte, nByte);
		}
		
		return res;
	}

	public byte[] getByte() {
		byte[] res = new byte[nByte *2];

		System.arraycopy(sx.getByte(), 0, res, 0, nByte);
		System.arraycopy(dx.getByte(), 0, res, nByte, nByte);
		
		return res;
	}

	public byte[] getByte(int at) {
		byte[] res = new byte[nByte *2];

		System.arraycopy(sx.getByte(at), 0, res, 0, nByte);
		System.arraycopy(dx.getByte(at), 0, res, nByte, nByte);

		return res;
	}
	
    public void addValue(double value) {
		dx.addValue(value);
		sx.addValue(value);
    }

    public void addValues(double[] values) {
		for (int i = 0; i < values.length; i++) {
		    addValue(values[i]);
		}
    }

    public void addByte(byte[] bytes) {
		byte[] lw, hg;

		lw = new byte[bytes.length / 2];
		hg = new byte[bytes.length / 2];

		System.arraycopy(bytes, 0, lw, 0, lw.length);
		System.arraycopy(bytes, hg.length, hg, 0, hg.length);

		sx.addByte(lw);
		dx.addByte(hg);
    }

    public void setSize(int size) {
		
		sx.setSize(size);
		dx.setSize(size);

		this.size = size;
    }

    public double getDxValue() {
		return dx.getValue();
    }

    public double getSxValue() {
		return sx.getValue();
    }

    public double getDxValue(int at) {
		return dx.getValue(at);
    }

    public double getSxValue(int at) {
		return sx.getValue(at);
    }

    public double[] getDxValues() {
		return dx.getValues();
    }

    public double[] getSxValues() {
		return sx.getValues();
    }

    public double[] getDxValues(int from, int to) {
		return dx.getValues(from, to);
    }

    public double[] getSxValues(int from, int to) {
		return sx.getValues(from, to);
    }
    
    public void reset() {
        sx.reset();
        dx.reset();
    }
    
    public void moveToBegining() {
		sx.moveToBegining();
		dx.moveToBegining();
    }

    public void moveTo(int pos) {
		sx.moveTo(pos);
		dx.moveTo(pos);
    }

    public void addByte(byte curr) {
        tempByte[bytePos] = curr;
        bytePos++;
        
        if (bytePos == nByte) {
            if (dxChannel) dx.addByte(tempByte);
            else sx.addByte(tempByte);
            
            dxChannel = ! dxChannel;
            
            bytePos = 0;
            Arrays.fill(tempByte, (byte) 0);
        }
    }
    
    public void addDxValue(double val) {
        dx.addValue(val);
    }
    
    public void addSxValue(double val) {
        sx.addValue(val);
    }
    
    public void addDxValues(double[] vals) {
        dx.addValues(vals);
    }
    
    public void addSxValues(double[] vals) {
        sx.addValues(vals);
    }
    
    public Sound getChanel(int ch) {
        if (ch == 0) return sx;
        else return dx;
    }
    
}
