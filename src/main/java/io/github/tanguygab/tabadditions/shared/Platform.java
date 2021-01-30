package io.github.tanguygab.tabadditions.shared;

public abstract class Platform {

	public abstract String type();

	public abstract int AsyncTask(Runnable r, long delay, long period);
	
	public abstract void AsyncTask(Runnable r, long delay);
	
	public abstract void cancelTask(int id);

    public abstract void disable();
}