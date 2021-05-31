package com.github.michelemilesi.university.music;/*
 * com.github.michelemilesi.university.music.StereoEcho.java
 *
 * Created on 2 aprile 2002, 12.05
 */

/**
 *
 * @author  michele
 * @version 
 */
public class StereoEcho extends Filter {
	public final static int DELAY_DX =  0;
	public final static int VOLUME_DX = 1;
	public final static int DELAY_SX =  2;
	public final static int VOLUME_SX = 3;
	public final static int LINKED =    4;

	private double delayDx;
	private double volumeDx;
	private double delaySx;
	private double volumeSx;
	private boolean linked;

	/** Creates new com.github.michelemilesi.university.music.StereoEcho */
	public StereoEcho() {
		delayDx = 0.0;
		delaySx = 0.0;
		volumeDx = 0.0;
		volumeSx = 0.0;
		linked = false;
	}

	public double getParameter(int param) {
		switch (param) {
		case DELAY_DX : return delayDx;
		case DELAY_SX : return delaySx;
		case VOLUME_DX : return volumeDx;
		case VOLUME_SX : return volumeSx;
		case LINKED : return linked ? 1.0 : 0.0;
		default : throw new RuntimeException("Il parametro " + param + " non esiste.");
		}
	}

	public Object getParameterAsObject(int param) {
		return new Double(getParameter(param));
	}
	
	public Sound process(Sound source) {
		StereoSound out;
		StereoSound in;
		double val;
		int ddx, dsx, opStep, ops;
		
		if (source instanceof StereoSource) {
			in = (StereoSound) source;
			
			opStep = in.getSize() / 100;
			setNOperations(100);
		

			out = (StereoSound) Sound.getDescrictor(in.getType(), in.getBit(), in.getRate(),
				in.isStereo(), in.isLittleEndian(),
				in.isSigned());

			out.setSize(in.getSize());

			ddx = (int) Math.ceil(in.getRate() *  delayDx / 1000.0);
			dsx = (int) Math.ceil(in.getRate() *  delaySx / 1000.0);
			
			ops = 0;
			for (int i = 0; i < in.getSize(); i++) {
				//Canale destro
				val = in.getDxValue();
				if (i >= ddx) val = val + out.getDxValue(i - ddx) * volumeDx;

				if (linked) {
					if (i >= (ddx + dsx)) {
						val = val + out.getSxValue(i - ddx - dsx) * volumeDx;
					}
				}

				out.addDxValue(val);
				
				//Canale sinistro
				val = in.getSxValue();
				if (i >= dsx) val = val + out.getSxValue(i - dsx) * volumeSx;

				if (linked) {
					if (i >= (dsx + ddx)) {
						val = val + out.getDxValue(i - dsx - ddx) * volumeSx;
					}
				}

				out.addSxValue(val);
				
				ops++;
				if ((ops % opStep) == 0) nextOp();		
			}
			
			if ((ops % opStep) != 0) nextOp();
			return out;
		}
		else {
			Filter mono = new Echo();

			mono.setParameter(Echo.DELAY, delaySx);
			mono.setParameter(Echo.VOLUME, volumeSx);

			return mono.process(source);
		}
	}
	
	public void setParameter(int param, Object value) {
		if (value instanceof Double) {
			setParameter(param, ((Double)value).doubleValue());
		} else {
			throw new RuntimeException("L'oggetto passato non � convertibile in Double");
		}
	}
	
	public void setParameter(int param, double value) {
		switch (param) {
		case DELAY_DX : 
			if (value > 0.0) {
				delayDx = value;
			}
			else {
				throw new RuntimeException("Il valore del delay deve essere maggiore di 0.");
			}
			break;
		case DELAY_SX :
			if (value > 0.0) {
				delaySx = value;
			}
			else {
				throw new RuntimeException("Il valore del delay deve essere maggiore di 0.");
			}
			break;
		case VOLUME_DX :
			if (value >= 0.0) {
				if (value <= 1.0) {
					volumeDx = value;
				}
				else {
					throw new RuntimeException("Il valore del volume non pu� essere maggiore di 1.");
				}
			}
			else {
				throw new RuntimeException("Il valore del volume non pu� essere inferiore a 0.");
			}
			break;
		case VOLUME_SX :
			if (value >= 0.0) {
				if (value <= 1.0) {
					volumeSx = value;
				}
				else {
					throw new RuntimeException("Il valore del volume non pu� essere maggiore di 1.");
				}
			}
			else {
				throw new RuntimeException("Il valore del volume non pu� essere inferiore a 0.");
			}
			break;
		case LINKED :
			if (value < 0.5) {
				linked = false;
			}
			else {
				linked = true;
			}
			break;
		default :
			throw new RuntimeException("Il parametro " + param + " non esiste.");
		}
	}
	
}
