package com.github.michelemilesi.university.music;/*
 * com.github.michelemilesi.university.music.FrequencyFilter.java
 *
 * Created on 15 aprile 2002, 16.58
 */

import java.util.*;

/**
 *
 * @author  michele
 * @version
 */
public abstract class FrequencyFilter extends Filter {

	public final static int STEP    = 0;
	public final static int OVERLAP = 1;

	protected static boolean fftActive = true;
	
	
	public final static void setFFTActive(boolean value)  {
		fftActive = value;
	}
	
	public final static boolean isFFTActive()  {
		return fftActive;
	}
	protected int step_size = 2048;
	
	protected double overlap = 0.25; //Sovrapposizone del 25%

	public Sound process(Sound source) {
		Sound res;
		Sound[] target, result;
		Sample[][] frameList, resList;
		Sample[] frame, foFrame, newFrame, newFoFrame;
		Spectrum[] spectre, newSpectre, realSpectre, newRealSpectre;
		double rate;
		int n;
		
		if (fftActive)  {
			if (step_size <= 1024) step_size = 1024;
			else if (step_size <= 2048) step_size = 2048;
			else if (step_size <= 4096) step_size = 4096;
			else if (step_size <= 8192) step_size = 8192;
			else if (step_size <= 16384) step_size = 16384;
			else step_size = 32768;
		}
		
		rate = source.getRate();
		res = Sound.getDescrictor(source.getType(), source.getBit(),
			source.getRate(), source.isStereo(),
			source.isLittleEndian(), source.isSigned());
		res.setSize(source.getSize());

		if (source.isStereo()) {
			target = new Sound[2];
			target[0] = ((StereoSource) source).getChanel(0);
			target[1] = ((StereoSource) source).getChanel(1);
			result = new Sound[2];

		} else {
			target = new Sound[1];
			result = new Sound[1];
			target[0] = source;
		}
		n = 0;
		for (int i = 0; i < target.length; i++) {
			frameList = split(target[i]);
			resList = new Sample[frameList.length][];
			
			if (n == 0)  {
				n = (3 + frameList.length * 7) * target.length;
				setNOperations(n);
			}
			
			nextOp();
			
			for (int j = 0; j < frameList.length; j++) {
				frame = frameList[j];

				if (fftActive)  {
					foFrame = Fourier.fft(frame, false);
				} else  {
					foFrame = Fourier.dft(frame, false);
				}
				nextOp();
						
				spectre = Fourier.getSpectrum(foFrame, rate);
				nextOp();

				realSpectre = Fourier.getTrueFrequencies(spectre, rate);
				nextOp();
				
				newRealSpectre = process(realSpectre, rate);
				nextOp();
				
				newSpectre = Fourier.getPhasedFrequencies(newRealSpectre, rate);
				nextOp();
				
				newFoFrame = Fourier.getSample(newSpectre);
				nextOp();
				
				if (fftActive)  {
					newFrame = Fourier.fft(newFoFrame, true);
				} else  {
					newFrame = Fourier.dft(newFoFrame, true);
				}
				nextOp();

				resList[j] = newFrame;
			}

			result[i] = merge(resList, source);
			nextOp();
			
			result[i] = postprocess(source, result[i]);
			nextOp();
		}//end for

		if (source.isStereo()) {
			((StereoSource) res).addSxValues(result[0].getValues());
			((StereoSource) res).addDxValues(result[1].getValues());
		} else {
			res = result[0];
		}

		return res;
	}

	protected abstract Spectrum[] process(Spectrum[] spectre, double rate);

	protected Sound postprocess(Sound source, Sound result) {
		return result;
	}

	public double getParameter(int param) {
		if (param == OVERLAP) return overlap;
		else throw new RuntimeException("Parametro insistente");
	}

	public Object getParameterAsObject(int param) {
		if (param == OVERLAP) return new Double(overlap);
		else throw new RuntimeException("Parametro insistente");
	}

	public void setParameter(int param, double value) {
		if (param == OVERLAP) {
			if ((value >= 0.0) && (value < 1.0)) {
				this.overlap = value;
			} else {
				throw new RuntimeException("L'indice di sovrapposizione deve essere " +
					"compreso nell'intervallo [0,1[");
			}
		} else if (param == STEP) {
			if (value >= 512) {
				this.step_size = (int) value;
			} else {
				throw new RuntimeException("La dimensione del passo di sintesi � troppo piccola");
			}
		}
	}

	public void setParameter(int param, Object value) {
		if (value instanceof Double)  {
			setParameter(param, ((Double) value).doubleValue());
		} else {
			throw new RuntimeException("Il parametro deve essere espresso come valore reale.");
		}
	}

	protected Sample[][] split(Sound source) {
		ArrayList frames;
		Sample[] frame;
		int begin, end, over;
		double[] rFrame;

		begin = 0;
		end = 0;
		frames = new ArrayList();
		over = (int) Math.ceil(step_size * overlap);
		while (end < source.getSize()) {
			end = begin + step_size;
			if (end > source.getSize())  {
				end = source.getSize();
				begin = end - step_size; 	
			}
			//end = Math.min(end, source.getSize());

			rFrame = source.getValues(begin, end);

			frame =  new Sample[rFrame.length];
			for (int i = 0; i < frame.length; i++) {
				frame[i] = new Sample();
				frame[i].real = rFrame[i];
			}

			frames.add(frame);

			begin = end - over;
		}

		return (Sample[][]) frames.toArray(new Sample[0][0]);
	}

	private Sound merge(Sample[][] frames, Sound source) {
		Sound res;
		double[] old, current;
		int over, nf, base, baseA, baseB, pos, size;
		double step, kO, kC, val;

		res = Sound.getDescrictor(source.getType(), source.getBit(), source.getRate(), source.isStereo(),
			source.isLittleEndian(), source.isSigned());
		size = source.getSize();
		res.setSize(size);

		over = (int) Math.round(step_size * overlap);
		nf = frames.length;
		step = 1.0 / over;

		if (nf == 1) {
			current = new double[frames[0].length];
			for (int k = 0; k < frames[0].length; k++) current[k] = frames[0][k].real;
			res.addValues(current);
		} else if (nf > 1) {
			pos = 0;
			//Prima parte del primo
			for (int i = 0; i < frames[0].length - over; i++) {
				res.addValue(frames[0][i].real);
				pos++;
			}

			//Dal secondo al penultimo escluso
			for (int i = 1; i < nf - 2; i++) {
				base = frames[i - 1].length - over;
				//Cross fade fra finale del precedente ed inizio dell'attuale
				for (int j = 0; j < over; j++) {
					kC = j * step;
					kO = 1 - kC;

					val = frames[i - 1][base + j].real * kO + frames[i][j].real * kC;

					res.addValue(val);
					pos++;
				}

				//Valori centrali
				for (int j = over; j < frames[i].length - over; j++) {
					res.addValue(frames[i][j].real);
					pos++;
				}
			}
			
			int pA, pB, pC, pD, pE;
			
			pA = step_size - over;
			pB = over;
			pC = (size - step_size) - 
				 (step_size - over) * (nf - 2); 
			pD = step_size - pC;
			
			if (pB > pC) {
				pC = pB;
				pD = step_size - pC;
			}
			
			step = 1.0 / pB;
			//Penultimo e ultimo
			for (int j = 0; j < pB; j++) {
				kC = j * step;
				kO = 1 - kC;

				val = frames[nf - 3][pA + j].real * kO +
					  frames[nf - 2][j].real * kC;
				res.addValue(val);
				pos++;
			}
			
			for (int j = pB; j < pC; j++)  {
				res.addValue(frames[nf -2][j].real);
				pos++;
			}
			
			step = 1.0 / pD;
			for (int j = 0; j < pD; j++)  {
				kC = j * step;
				kO = 1 - kC;
				
				val = frames[nf - 2][pC + j].real * kO +
					  frames[nf - 1][j].real * kC;
				
				res.addValue(val);
				pos++;
			}
			
			//BOGOUS pos non dovrebbe mai superare size	
			for (int j = pD; (j < step_size) && (pos < size); j++)  {
				res.addValue(frames[nf -1][j].real);
				pos++;
			}
		}

		return res;
	}//end run
}

