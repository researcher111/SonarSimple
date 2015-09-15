package DSP;

public class DSP {
	/**
	 * Hanning 
	 */
	public static double[] HanningWindow(double[] signal_in, int pos, int size){
		return Hanning.HanningWindow(signal_in, pos, size);
	}

	
	/**
	 * Chirp
	 */
	public static double[] linearChirp(double phase, double f0, double f1, double t1, 
			double samplingFreq){
		return Chirp.linearChirp(phase, f0, f1, t1, samplingFreq);
	}
	
	
	/**
	 * Convert to Short
	 * 
	 */
	public static short[] ConvertToShort(double[] signal_in){
		short[] generatedSnd = new short[signal_in.length];
		int idx = 0;
		for (final double dVal : signal_in) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = val;//(byte) (val & 0x00ff);
			//generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}
		return generatedSnd;
	} 
	
	
	/**
	 * Pad Signal and position signal
	 * 
	 */
	
	public static double[] padSignal(double[] signal, int length, int position){
		double [] newSignal = new double[length];
		int siglen = signal.length;
		for(int i=0; i<siglen; i++){
			newSignal[i+position] = signal[i];
		}
		return newSignal;
		
	}
	
}
