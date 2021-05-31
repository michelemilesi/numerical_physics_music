package com.github.michelemilesi.university.music;

public abstract class Sound {

	protected boolean stereo;
	protected int bit;
	protected boolean littleEndian;
	protected double rate;
	protected String type;
	protected int size;
	protected boolean signed;


	public static Sound getDescrictor(String type, int bit, double rate, boolean stereo, boolean le,
		boolean signed) {
		if (stereo) {
			return new StereoSound(type, bit, rate, le, signed);
		} else {
			return new MonoSound(type, bit, rate, le, signed);
		}
	}

	public abstract void setSize(int size);

	public int getSize() {
		return size;
	}

	public int getBit() {
		return bit;
	}

	public boolean isStereo() {
		return stereo;
	}

	public boolean isLittleEndian() {
		return littleEndian;
	}

	public double getRate() {
		return rate;
	}

	public String getType() {
		return type;
	}

	public boolean isSigned() {
		return signed;
	}

	public abstract double getValue();

	public abstract double getValue(int at);

	public abstract double[] getValues();

	public abstract double[] getValues(int from, int to);

	public abstract void rescale(boolean clip);

	public abstract byte[] getBytes();

	public abstract byte[] getByte();

	public abstract byte[] getByte(int at);

	public abstract byte[] getBytes(int nsamples);

	public abstract byte[] getBytes(int from, int to);

	public abstract void addValue(double value);

	public abstract void addValues(double[] values);

	public abstract void addByte(byte[] bytes);

	public abstract void addByte(byte curr);

	public abstract void reset();

	public abstract void moveToBegining();

	public abstract void moveTo(int pos);


	protected byte[] convert(double val, int nByte) {
		byte[] res;
		int ival;
		byte temp;

		res = new byte[nByte];

		// Rimappo da (-1,1] a [-2^(bit-1), 2^(bit-1)-1]
		ival = (int) Math.min( Math.round(val * Math.pow(2.0, bit - 1)),
		                       (int) Math.pow(2.0, bit -1) - 1 ) ;

		if (!signed) {
			ival = ival + (int) Math.round(Math.pow(2.0, bit - 1));
		}

		for (int i = 0; i < nByte; i++) {
			res[i] = (byte) (ival & 0xFF);
			ival = ival / 256;
		}

		if (!littleEndian) {
			for (int i = 0; i < (nByte / 2); i++) {
				temp = res[i];
				res[i] = res[nByte - i - 1];
				res[nByte - i - 1] = temp;
			}
		}

		return res;
	}//end convert(double)



	protected double convert(byte[] bytes) {
		double res;
		int ival;
		int sgn;
		int nByte = (int) Math.ceil(bit / 8.0);

		res = 0.0;  ival = 0;
		if (!signed) {
			if (!littleEndian) {
				for (int i = 0; i < bytes.length; i++) {
					ival = (ival * 256) + (bytes[i] & 0xFF);
				}
			} else {
				for (int i = bytes.length - 1; i >= 0; i--) {
					ival = (ival * 256) + (bytes[i] & 0xFF);
				}
			}
			res = ival;
			// mappo (0,+(2^bit)-1] -> (-1,1]
			res = (res - Math.pow(2.0, bit-1)) / Math.pow(2.0, bit-1);
		} else {
			if (!littleEndian) {
				sgn = (bytes[0] & 0x80) / 0x80;
				ival = bytes[0] & 0x7F;
				for (int i = 1; i < bytes.length; i++) {
					ival = (ival * 256) + (bytes[i] & 0xFF);
				}
			} else {
				sgn = (bytes[nByte - 1] & 0x80) / 0x80;
				ival = bytes[nByte - 1] & 0x7F;
				for (int i = bytes.length - 2; i >= 0; i--) {
					ival = (ival * 256) + (bytes[i] & 0xFF);
				}
			}
			res = ival;
			res = (sgn * - Math.pow(2.0, bit - 1)) + res;
			// mappo (-2^(bit-1),+2^(bit-1)-1) -> (-1,1]
			res = res / Math.pow(2.0, bit - 1);
		}
		return res;
	}  //end convert(byte[])

}
