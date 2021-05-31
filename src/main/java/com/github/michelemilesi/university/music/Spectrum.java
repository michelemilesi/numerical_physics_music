package com.github.michelemilesi.university.music;

import java.text.*;

public class Spectrum {

	/**
	* L'ampiezza dello spettro
	*/
	public double amplitude;

	/**
	* La fase dello spettro
	*/
	public double phase;

	/**
	* La frequenza dello spettro
	*/
	public double frequency;

	public String toString() {
		NumberFormat nf = new DecimalFormat("#0.000");

		return new String("(" + nf.format(amplitude) + ";" + nf.format(frequency) +
		                  ";" + nf.format(phase) + ")");
	}

}////:~
