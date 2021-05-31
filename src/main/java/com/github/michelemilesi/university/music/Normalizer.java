package com.github.michelemilesi.university.music;/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
/**
 * @version 	1.0
 * @author
 */
public class Normalizer extends Filter {

	public final static int MAX_VALUE = 0;
	
	private double max_level = 0.0;

	/*
	 * @see com.github.michelemilesi.university.music.Filter#setParameter(int, double)
	 */
	public void setParameter(int param, double value) {
		if (param == MAX_VALUE) {
			if ((value > 0.0) && (value <= 1.0)) {
				this.max_level = value;
			} else {
				throw new RuntimeException("Il livello massimo deve essere compreso tra 0 e 1");
			}
				
		} else {
			throw new RuntimeException("Parametro inesistente");
		}
	}

	/*
	 * @see com.github.michelemilesi.university.music.Filter#getParameter(int)
	 */
	public double getParameter(int param) {
		if (param == MAX_VALUE) {
			return max_level;
		} else {
			throw new RuntimeException("Parametro inesistente");
		}
	}

	/*
	 * @see com.github.michelemilesi.university.music.Filter#process(com.github.michelemilesi.university.music.Sound)
	 */
	public Sound process(Sound source) {
		if (source.isStereo()) {
			return process((StereoSound) source);
		} else {
			return process((MonoSound) source);
		}
	}

	public Sound process(MonoSound source) {
		MonoSound out;
		
		double max, curr, k;
		int n;
		
		out = (MonoSound) Sound.getDescrictor(source.getType(), source.getBit(), 
								  source.getRate(), source.isStereo(),
								  source.isLittleEndian(), 
								  source.isSigned() );
		out.setSize(source.getSize());
		
		n = source.getSize();
		
		setNOperations(n * 2);
		System.out.println(n);
		max = 0.0;
		
		for (int i = 0; i < n; i++)  {
			curr = Math.abs(source.getValue(i));
			
			if (curr > max) max = curr;
			
			nextOp();
		}
		
		if (max > 0) k = max_level / max;
		else k = 0;
		
		for (int i = 0; i < n; i++)  {
			out.addValue(source.getValue(i) * k);
			nextOp();
		}
				
		return out;
	}

	public Sound process(StereoSound source) {
		StereoSound out;
		
		double max, curr, k;
		int n;
		
		out = (StereoSound) Sound.getDescrictor(source.getType(), source.getBit(), 
								  source.getRate(), source.isStereo(),
								  source.isLittleEndian(), 
								  source.isSigned() );
		out.setSize(source.getSize());
		
		n = source.getSize();
		
		setNOperations(n * 2);
		
		max = 0.0;
		
		for (int i = 0; i < n; i++)  {
			curr = Math.abs(source.getSxValue(i));
			if (curr > max) max = curr;
			curr = Math.abs(source.getDxValue(i));
			if (curr > max) max = curr;
			
			nextOp();
		}
		
		if (max > 0) k = max_level / max;
		else k = 0;
		
		for (int i = 0; i < n; i++)  {
			out.addDxValue(source.getDxValue(i) * k);
			out.addSxValue(source.getSxValue(i) * k);
			nextOp();
		}
		
		return out;
	}

	/*
	 * @see com.github.michelemilesi.university.music.Filter#setParameter(int, Object)
	 */
	public void setParameter(int param, Object value) {
		if (value instanceof Double) {
			setParameter(param, ((Double) value).doubleValue());
		} else {
			throw new RuntimeException("Valore incompatibile");			
		}
	}

	/*
	 * @see com.github.michelemilesi.university.music.Filter#getParameterAsObject(int)
	 */
	public Object getParameterAsObject(int param) {
		if (param == MAX_VALUE) {
			return new Double(max_level);
		} else {
			throw new RuntimeException("Parametro inesistente");
		}
	}

}
