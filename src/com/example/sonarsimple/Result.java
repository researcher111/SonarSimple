package com.example.sonarsimple;

public class Result {
	public double distance;
	public double xcorrHeight;
	public short[] signal; 
	public double[] xcorr;

	
	public Result(double distance, double xcorrHeight, short[] signal, double[] xcorr){
		this.distance = distance;
		this.xcorrHeight = xcorrHeight;
		this.signal = signal;
		this.xcorr = xcorr;
	}
}
