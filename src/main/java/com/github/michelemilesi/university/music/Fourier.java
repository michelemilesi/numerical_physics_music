package com.github.michelemilesi.university.music;

public class Fourier {

	public final static Sample[] fft(Sample[] in, boolean inverse)  {
		if (!inverse)  {
			return fft(in, 2 * Math.PI / in.length);
		} else  {
			double factor;
			
			Sample[] smp = fft(in, -2 * Math.PI / in.length);
			
			factor = 1f / in.length;
			for (int i = 0; i < smp.length; i++)  {
				smp[i].real = smp[i].real * factor;
				smp[i].img = smp[i].img * factor;
			}
			
			return smp;
		}
	}
	
	private final static Sample[] fft(Sample[] in, double wexp) {
		int n, sgn;
		double abswexp, wr, wi;
		Sample[] a, b, ar, br;
		Sample[] out;
		
		if (in.length == 1)  { //caso base
			out = new Sample[1];
			out[0] = new Sample(in[0]);			
		} else  {
			if (!isPowerOfTwo(in.length))  {
				throw new IllegalArgumentException("La lunghezza del sample deve "+
												   "essere una potenza di due.");	
			}
		
			n = in.length / 2;
		
			a = new Sample[n];
			b = new Sample[n];
			
			for (int i = 0; i < n; i++)  {
				a[i] = new Sample(in[i * 2]);
				b[i] = new Sample(in[i * 2 + 1]);
			}
			
			ar = fft(a, wexp * 2);
			br = fft(b, wexp * 2);
			
			out = new Sample[in.length];
			abswexp = Math.abs(wexp);
			sgn = - (int) Math.round(wexp / abswexp);
			 
			for (int i = 0; i < n; i++) {
				out[i] = new Sample();
				out[i + n] = new Sample();
				
				wr = Math.cos(abswexp * i);
				wi = sgn * Math.sin(abswexp * i);
				out[i].real = ar[i].real + wr * br[i].real - wi * br[i].img;
				out[i].img = ar[i].img + wi * br[i].real + wr * br[i].img;
				
				out[i + n].real = ar[i].real - (wr * br[i].real - wi * br[i].img);
				out[i + n].img = ar[i].img - (wi * br[i].real + wr * br[i].img);
			}
			
		}
		
		return out;
	}

	public final static Sample[] dft(Sample[] in, boolean inverse) {
		double angle, sgn, wr, wi, real, img;
		int n;

		Sample[] out;

		angle = 2.0 * Math.PI / in.length;

		if (inverse) {
			sgn = 1.0;
		} else {
			sgn = -1.0;
		}

		out = new Sample[in.length];

		n = in.length;

		for (int k = 0; k < out.length; k++) {
			out[k] = new Sample();

			for (int j = 0; j < n; j++) {
				wr = Math.cos(k * j * angle);
				wi = sgn * Math.sin(k * j * angle);

				real = wr * in[j].real + wi * in[j].img;
				img = wr * in[j].img + wi * in[j].real;

				out[k].real = out[k].real + real;
				out[k].img = out[k].img + img;
			}
		}

		if (inverse) {
			for (int i = 0; i < out.length; i++) {
				out[i].real = out[i].real / out.length;
				out[i].img = out[i].img / out.length;
			}

			for (int i = 1; i < n / 2; i++) {
				wr = out[i].real;
				wi = out[i].img;

				out[i].real = out[n - i].real;
				out[i].img = out[n - i].img;

				out[n - i].real = wr;
				out[n - i].img = wi;
			}
		}

		return out;
	}

	public final static Spectrum[] getSpectrum(Sample[] data, double rate) {
		Spectrum[] res;
		int n;
		double basef;

		n = data.length;
		res = new Spectrum[n];

		basef = rate / n;

		for (int i = 0; i <= n / 2; i++) {
			res[i] = new Spectrum();

			res[i].frequency = basef * i;

			res[i].amplitude = Math.sqrt(Math.pow(data[i].real, 2.0) + Math.pow(data[i].img, 2.0));

			res[i].phase = Math.atan2(data[i].img, data[i].real);
		}

		for (int i = (n / 2) + 1; i < n; i++) {
			res[i] = new Spectrum();
		}

		return res;
	} 

	public final static Sample[] getSample(Spectrum[] spec) {
		Sample[] data;
		int n;

		n = spec.length;
		data = new Sample[n];

		for (int i = 0; i <= n / 2; i++) {
			data[i] = new Sample();
			data[i].real = spec[i].amplitude * Math.cos(spec[i].phase);
			data[i].img = spec[i].amplitude * Math.sin(spec[i].phase);
		}

		for (int i = (n / 2) + 1; i < n; i++) {
			data[i] = new Sample();
			data[i].real = data[n - i].real;
			data[i].img = -1.0 * data[n - i].img;
		}

		return data;
	}
	
	public final static Sample[] dct(Spectrum[] spec, double rate) {
		Sample[] res;
		int n;
		double tgt, xrate;

		n = spec.length;
		res = new Sample[n];

		xrate = 2 * Math.PI / rate;

		for (int x = 0; x < n; x++) {
			res[x] = new Sample();

			//res[x].real = spec[0].amplitude * Math.sin(spec[0].phase);
			res[x].real = spec[0].amplitude * Math.cos(spec[0].phase);
			for (int k = 1; k < n/2 ; k++) {
				tgt = x * spec[k].frequency * xrate;
				//res[x].real += spec[k].amplitude * Math.cos(tgt + spec[k].phase);
				//res[x].real += 2.0 * spec[k].amplitude * Math.sin(tgt + spec[k].phase);
				res[x].real += 2.0 * spec[k].amplitude * Math.cos(tgt + spec[k].phase);
			}
			tgt = (n * spec[n/2].frequency * xrate) / 2;
			//res[x].real += spec[n / 2].amplitude * Math.sin(tgt + spec[n/2].phase);
			res[x].real += spec[n / 2].amplitude * Math.cos(tgt + spec[n/2].phase);

			res[x].real = res[x].real / n;
		}

		return res;
	}

	public final static Spectrum[] getTrueFrequencies(Spectrum[] in, double rate) {
		Spectrum[] out;
		double basef;

		out = new Spectrum[in.length];

		basef = rate / in.length;

		for (int i = 0; i < in.length; i++) {
			out[i] = new Spectrum();
			out[i].amplitude = in[i].amplitude;
			out[i].frequency = in[i].frequency + basef * (in[i].phase / (2 * Math.PI));
			out[i].phase = 0.0;
		}

		return out;
	}

	public final static Spectrum[] getPhasedFrequencies(Spectrum[] in, double rate) {
		Spectrum[] out;
		double basef, kf;

		out = new Spectrum[in.length];

		basef = rate / in.length;
		kf = 2 * Math.PI / basef;

		for (int i = 0; i <= in.length / 2; i++) {
			out[i] = new Spectrum();
			out[i].amplitude = in[i].amplitude;
			out[i].frequency = basef * i;
			out[i].phase = kf * (in[i].frequency - out[i].frequency);
		} 

		for (int i = (in.length / 2) + 1; i < in.length; i++) {
			out[i] = new Spectrum();
		}

		return out;
	}

	private final static boolean isPowerOfTwo(int n)  {
		int i;
		boolean isPower;
		
		i = n;
		isPower = true;
		
		while ((i > 1) && (isPower))  {
			isPower = (i % 2) == 0 ? true: false;
			i = i / 2;
		}
		return isPower;
	}

	public static void main(String[] args) {
		Sample[] test, rd, rf, rid, rif;
		int n;

		n = 16;
		test = new Sample[n];
		for (int i = 0; i < n; i++) {
			test[i] = new Sample();
			test[i].real = 0.5 * Math.sin(i * 2 * Math.PI / n) + 0.25 *
				Math.sin(i * 6 * Math.PI / n) + 0.25;
			//test[i].real = Math.random();
		}

		System.out.print("DFT");
		rd = Fourier.dft(test, false);
		System.out.println("fatto");

		System.out.print("iDFT");
		rid = Fourier.dft(rd, true);
		System.out.println("fatto");
		
		System.out.print("FFT");
		rf = Fourier.fft(test, false);
		System.out.println("fatto");

		System.out.print("iFFT");
		rif = Fourier.fft(rf, true);
		System.out.println("fatto");
		
		for (int i = 0; i < n; i++)  {
			System.out.println("" + test[i] + "\t" + rid[i] + "\t" + rif[i]); 
		}
		
		for (int i = 0; i < n; i++)  {
			System.out.println("" + rd[i] + "\t" + rf[i]); 
		}
	}
}////:~
