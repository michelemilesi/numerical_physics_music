import java.util.Arrays;

public class MonoSound extends Sound {
    private double[] values;

    private int pos;
    private int bytePos;
    private int nByte;
    private byte[] tempByte;
	private int insPnt;

    public MonoSound(String type, int bit, double rate, boolean le, boolean signed) {
		this.type = type;
		this.bit = bit;
		this.rate = rate;
		this.littleEndian = le;
		this.signed = signed;
		this.stereo = false;
		this.size = 0;

		pos = 0;
		bytePos = 0;
		nByte = (int) (Math.ceil(bit / 8.0));
		values = new double[size];
	        tempByte = new byte[nByte];
		insPnt = 0;
    }

    public double getValue() {
		if (pos < size) {
		    return values[pos++];
		} else {
		    return 0.0;
		}
    }

    public double getValue(int at) {
		if (at < size) {
		    return values[at];
		} else {
		    return 0.0;
		}
    }
    
    public double[] getValues() {
		double[] res;

		res = new double[size];
		System.arraycopy(values, 0, res, 0, size);

		return res;
    }

    public double[] getValues(int from, int to) {
		double[] res;
		int dim;

		dim = to - from;

		res = new double[dim];

		if (to <= size) {
		    System.arraycopy(values, from, res, 0, dim);
		} else {
		    System.arraycopy(values, from, res, 0, size - from);

		    for (int i = size; i < to; i++) res[i] = 0.0;
		}

		return res;
    }

    public void rescale(boolean clip) {
		double max, factor;

		if (clip) {
		    for (int i = 0; i < size; i++) {
			if (values[i] > 1.0) {
			    values[i] = 1.0;
			} else if (values[i] < -1.0) {
			    values[i] = -1.0;
			}
		    }
		} else {
		    max = 0.0;

		    for (int i = 0; i < size; i++) {
			if (Math.abs(values[i]) > max) max = Math.abs(values[i]);
		    }

		    if (max > 1.0) {
			factor = 1.0 / max;
			for (int i = 0; i < size; i++) {
			    values[i] = values[i] * factor;
			}
		    }
		}
    }

    public byte[] getBytes() {
		byte[] res;
		byte[] temp;

		res = new byte[nByte * size];

		for (int i = 0; i < size; i++) {
		    temp = convert(values[i], nByte);
		    
		    System.arraycopy(temp, 0, res, i * nByte, nByte);
		}

		return res;
    }

	public byte[] getBytes(int nsample) {
		byte[] res;
		byte[] temp;
		
		if (pos<size) {
			res = new byte[nsample * nByte];
			for(int i=0; i<nsample; i++) {
				temp = convert(getValue(),nByte);
				System.arraycopy(temp, 0, res, i*nByte, nByte);
			}
			return res;
		}
		else  return null;
	}

	public byte[] getBytes(int from, int to) {
		byte[] res;
		byte[] temp;
		int end;

		end = Math.min(to,size);

		res = new byte[Math.max(end-from,0) * nByte];
		for(int i=from; i<end; i++) {
			temp = convert(getValue(i),nByte);
			System.arraycopy(temp, 0, res, i*nByte, nByte);
		}
		return res;
	}

	public byte[] getByte() {
		return ( convert(getValue(),nByte) );
	}

	public byte[] getByte(int at) {
		return ( convert(getValue(at),nByte) );
	}
	
	public void addValue(double value) {
		if (insPnt < size) {
		    values[insPnt] = value;
		    insPnt++;
		} else {
		    throw new RuntimeException("Impossibile inserire i campioni oltre la dimensione stabilita.");
		}
    }

    public void addValues(double[] vals) {
		if (insPnt + vals.length <= size) {
		    System.arraycopy(vals, 0, values, insPnt, vals.length);
		    insPnt = insPnt + vals.length;
		} else {
		    throw new RuntimeException("Impossibile inserire i campioni oltre la dimensione stabilita.");
		}
    }

    public void addByte(byte[] bytes) {
		if (insPnt < size) {
		    values[insPnt] = convert(bytes);
		    insPnt++;
		} else {
		    throw new RuntimeException("Impossibile inserire i campioni oltre la dimensione stabilita.");
		}
    }

    public void setSize(int size) {
		double[] res;

		res = new double[size];

		if (size < this.size) {
		    System.arraycopy(values, 0, res, 0, size);
		} else {
		    System.arraycopy(values, 0, res, 0, this.size);

		    for (int i = this.size; i < size; i++) {
			res[i] = 0.0;
		    }
		}

		this.size = size;
		this.values = res;
    }
    
    public void reset() {
        this.pos = 0;
        this.insPnt = 0;
    }

    public void moveToBegining() {
	this.pos = 0;
    }

    public void moveTo(int pos) {
	this.pos = Math.min(pos, size);
    }
    
    public void addByte(byte curr) {
        tempByte[bytePos] = curr;
        bytePos++;
        
        if (bytePos == nByte) {
            addByte(tempByte);
            bytePos = 0;
            Arrays.fill(tempByte, (byte) 0);
        }
    }
}
