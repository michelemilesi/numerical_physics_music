public class Echo extends Filter {
	public final static int DELAY = 0;
	public final static int VOLUME = 1;

	private double delay;
	private double volume;

	public Echo() {
		this.delay = 0.0;
		this.volume = 0.0;
	}

	public Object getPameterAsObject(int param) {
		return new Object();
	}

	public void setParameter(int param, double value) {
		switch (param) {
		case DELAY :
			if (value > 0.0) {
				delay = value;
			} else {
				throw new RuntimeException("Il valore del delay deve essere maggiore di 0.");
			}
			break;
		case VOLUME :
			if (value >= 0.0) {
				if (value <= 1.0) {
					volume = value;
				} else {
					throw new RuntimeException("Il valore del volume non può essere maggiore di 1.");
				}
			} else {
				throw new RuntimeException("Il valore del volume non può essere inferiore a 0.");
			}
			break;
		default :
			throw new RuntimeException("Il parametro " + param + " non esiste.");
		}
	}

	public double getParameter(int param) {
		switch (param) {
		case DELAY : return delay;
		case VOLUME : return volume;
		default : throw new RuntimeException("Il parametro " + param + " non esiste.");
		}
	}

	public void setParameter(int param, Object value) {
		Double dblVal;

		if (value instanceof Double) {
			dblVal = (Double) value;
			setParameter(param, dblVal.doubleValue());
		} else {
			throw new RuntimeException("L'oggetto passato deve essere di tipo Double");
		}
	}

	public Object getParameterAsObject(int param) {
		switch (param) {
		case DELAY : return new Double(delay);
		case VOLUME : return new Double(volume);
		default : throw new RuntimeException("Il parametro " + param + " non esiste.");
		}
	}


	public Sound process(Sound in) {

		int nFrames, pos, opStep, ops;
		double val;
		double[] buffer;
		double max_val, min_val;
		Sound out;

		if (in.isStereo()) {
			//TO DO: Gestire i file Stereo
			return in;
		}
		
		opStep = in.getSize() / 100;
		setNOperations(100);
		
		out = Sound.getDescrictor(in.getType(), in.getBit(), in.getRate(), in.isStereo(),
									in.isLittleEndian(), in.isSigned() );
		out.setSize(in.getSize());

		max_val = (Math.pow(2.0, in.getBit()) - 1.0 - Math.pow(2.0, in.getBit() - 1)) / Math.pow(2.0, in.getBit() - 1);
		min_val = - (Math.pow(2.0, in.getBit() - 1) / Math.pow(2.0, in.getBit() - 1));

		nFrames = (int) Math.ceil(in.getRate() *  delay / 1000.0);

		buffer = new double[nFrames];
		ops = 0;
		
		for (int i = 0; i < in.getSize(); i++) {
			pos = i % nFrames;
			val = in.getValue(i) + volume * buffer[pos];
			if (val > max_val) val = max_val;
			if (val < min_val) val = min_val;
			out.addValue(val);
			buffer[pos] = val;
			
			ops++;
			if ((ops % opStep) == 0) nextOp();
		}

		if ((ops % opStep) != 0) nextOp();
		
		return out;
	}

	public static void main(String[] args) {
		Sound test, res;
		Echo echo;

		test = Sound.getDescrictor("test", 8, 4, false, true, false);
		test.setSize(32);

		for (int i = 0; i < 32; i++) test.addValue(Math.random() * 2.0 - 1.0);

		echo = new Echo();

		echo.setParameter(Echo.DELAY, 500.0);
		echo.setParameter(Echo.VOLUME, 0.25);

		res = echo.process(test);

		double[] starts = test.getValues();
		double[] ends = res.getValues();

		for (int i = 0; i < ends.length; i++) {
			System.out.println(i + "\t" + starts[i] + "\t" + ends[i]);
		}

	}


}////:~
	
