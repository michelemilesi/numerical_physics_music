public interface StereoSource {

    public double getDxValue();

    public double getSxValue();

    public double getDxValue(int at);

    public double getSxValue(int at);

    public double[] getDxValues();

    public double[] getSxValues();

    public double[] getDxValues(int from, int to);

    public double[] getSxValues(int from, int to);
    
    public void addDxValue(double val);
    
    public void addSxValue(double val);
    
    public void addDxValues(double[] vals);
    
    public void addSxValues(double[] vals);
	
	public Sound getChanel(int ch);
	
}
