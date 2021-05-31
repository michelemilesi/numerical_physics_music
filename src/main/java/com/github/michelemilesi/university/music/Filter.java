package com.github.michelemilesi.university.music;

public abstract class Filter {
    public abstract void setParameter(int param, double value);
    public abstract double getParameter(int param);
    public abstract Sound process(Sound source);
    public abstract void setParameter(int param, Object value);
    public abstract Object getParameterAsObject(int param);
	
	private int nOps;
	private int currOp;
	private String message;
	private Monitor monitor;
	
	public Filter()  {
		nOps = 100;
		currOp = 0;
		message = "Applicazione del filtro in corso. Attendere...";
	}
	
	protected void setMessage(String message)  {
		this.message = message;
		notifyMonitor();
	}
	
	protected void setNOperations(int nops) {
		this.nOps = nops;
		notifyMonitor();
	}
	
	protected void nextOp() {
		this.currOp++;
		notifyMonitor();
	}

	
	protected void notifyMonitor()  {
		if (monitor != null)  {
			monitor.hasChanged(this);
		}
	}
	
	
	public void setMonitor(Monitor monitor)  {
		this.monitor = monitor;
	}
	
	public void removeMonitor()  {
		this.nOps = 0;
		this.currOp = 0;
		this.monitor = null;
	}	

	public int getMaximum() {
		return nOps;
	}
	
	public int getCurrent() {
		return currOp;
	}
	
	public String getMessage() {
		return message;
	}
}////:~
