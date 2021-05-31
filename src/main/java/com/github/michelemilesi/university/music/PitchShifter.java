package com.github.michelemilesi.university.music;

public class PitchShifter extends FrequencyFilter {

	public final static int SCALE = OVERLAP + 1;

	private double scale;

	protected Spectrum[] process(Spectrum[] spectre, double rate) {
		Spectrum[] result;
		int bin, n;
		double nf, base;
		double limit;

		n = spectre.length;
		base = rate / n;
		limit = rate / 2;

		result = new Spectrum[n];
		for (int i = 0; i < n; i++) result[i] = new Spectrum();


		for (int i = 0; i <= n / 2; i++) {
			nf = spectre[i].frequency * scale;
			if (nf <= limit) {
				bin = (int) Math.round(nf / base);
				if ((bin <= n/2) && (result[bin].amplitude < spectre[i].amplitude)) {
					result[bin].amplitude = spectre[i].amplitude;
					result[bin].phase = 0.0;
					result[bin].frequency = nf;
				}
			}
		}
		return result;
	}    

	public void setParameter(int param, double value) {
		if (param == SCALE) {
			this.scale = value;
		} else {
			super.setParameter(param, value);
		}
	}

	public double getParameter(int param) {
		if (param == SCALE) {
			return this.scale;
		} else {
			return super.getParameter(param);
		}
	}


	public static void main(String[] args) {
		Sound test, result;

		test = Sound.getDescrictor("WAVE", 16, 32.0, false, false, true);
		test.setSize(11264);
		for (int i = 0; i < 11264; i++) {
			test.addValue(Math.sin(i * Math.PI / 16) * ((Math.sin(i * Math.PI / 1000) + 1) / 2.0));
		}

		PitchShifter ps = new PitchShifter();
		ps.setParameter(ps.SCALE, 0.5);
		System.out.println("Inizio");
		result = ps.process(test);
		System.out.println("FIne");

		for (int i = 0; i < result.getSize(); i++) {
			System.out.println(test.getValue(i) +  ";" + result.getValue(i));
		}
	}
}////:~
