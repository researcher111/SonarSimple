package com.example.sonarsimple;

import android.annotation.TargetApi;
import android.os.Build;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import DSP.Complex;
import DSP.FFT;

public class FilterAndClean {
	public static double soundSpeed = 346.65; // Speed of sound in air
	public static double peakThreshold = 22000000000.0;
	public static double multiple = 1000000000.0;
	public static Complex[] cachedPulse = null;
	public static int sharpness = 1;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static Result Distance(short[] signal, short[] pulse,
			int sampleRate, double threshold, double maxDistanceMeters,
			int deadZoneLenght, int pthreshold, float tempature) {

		soundSpeed = 331 + 0.6*15;//331 + 0.6*(double)tempature;
		peakThreshold = pthreshold * multiple;
		// Preprocessing for speed
		// strip out first set of zeros. This deals with concurrency issue
		// between the recording buffer and the play buffer
		int startIndex = 0;
		for (startIndex = 0; startIndex < signal.length; startIndex++) {
			if ((signal[startIndex]) > threshold) {
				break;
			}
		}
		int samplesPerMeter = (int)(sampleRate / soundSpeed); // seed of sound is 330 meters
												// per second in air.
		int maxSamples =(int) (samplesPerMeter * maxDistanceMeters * 2); // We
																			// multiple
																			// by
																			// 2
																			// because
																			// it
																			// is
																			// round
																			// trip
		maxSamples = (int) Math.pow(2,
				Math.ceil(Math.log(maxSamples) / Math.log(2)));// Samples must
																// be a power of
																// 2 for FFT to
																// run smoothly.
		// Trim the pulse and signal based on range and concurrency mismatch
		signal = Arrays
				.copyOfRange(signal, startIndex, startIndex + maxSamples);
		pulse = Arrays.copyOfRange(pulse, 0, maxSamples);

		double distance = 0;
		Complex[] compSignal = Complex.convertShortToComplex(signal);
		// Complex[] compPulse = Complex.convertShortToComplex(pulse);
		Complex[] fftSignal = FFT.fft(compSignal);
		// Complex[] fftPulse = FFT.fft(compPulse);
		Complex[] fftPulse = null;
		if (cachedPulse == null) {

			Complex[] compPulse = Complex.convertShortToComplex(pulse);
			fftPulse = FFT.fft(compPulse);
			cachedPulse = fftPulse;
		} else {
			fftPulse = cachedPulse;

		}
		Complex[] Z = new Complex[pulse.length];
		int Zlen = Z.length;
		for (int i = 0; i < Zlen; i++) {
			Z[i] = (fftPulse[i].conjugate()).times(fftSignal[i]);
		}

		Complex[] ZH = SuppressNegative(Z);

		Complex[] invZ = FFT.fftShift(FFT.ifft(ZH)); // Check for absolute and
														// shifting.

		// Make all the elements abs valued.
		int lenInvZ = invZ.length;
		double[] absInvZ = new double[lenInvZ];
		for (int i = 0; i < lenInvZ; i++) {
			absInvZ[i] = invZ[i].abs();
		}
		// double[] absInvZ = CrossCorr(signal, pulse) ;
		// int lenInvZ = absInvZ.length;

		double[] absXcorr = absInvZ.clone();

		// Remove the dead zone
		// int firstIndex = getMaxIndex(absInvZ);
		// double Value1 = Math.abs(absInvZ[firstIndex]);
		// absInvZ = zeroDeadZone(firstIndex,deadZoneLenght,absInvZ);
		// absInvZ = Arrays.copyOfRange(absInvZ,firstIndex+deadZoneLenght,
		// absInvZ.length);
		// int maxIndex = getMaxIndex(absInvZ);//+firstIndex+deadZoneLenght;
		int[] peaks = peakIndex(absInvZ, peakThreshold, sharpness);
		int firstIndex = peaks[0];
		int secondIndex = peaks[1];
		if (peaks[1] == 0) { // If not confident report zero;
			secondIndex = peaks[0];
		}
		double t1 = ((double) (firstIndex) - (double) (lenInvZ) / (double) (2))
				/ ((double) sampleRate);
		double t2 = ((double) (secondIndex) - (double) (lenInvZ) / (double) (2))
				/ ((double) sampleRate);
		double Value2 = Math.abs(absInvZ[secondIndex]);
		return new Result((t2 - t1) * soundSpeed / (double) (2), Value2,
				signal, absInvZ);
	}

	public static int getMaxIndex(double[] x) {
		int lenx = x.length;
		double max = 0;
		int maxIndex = 0;
		for (int i = 0; i < lenx; i++) {
			if (x[i] > max) {
				max = x[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	// Suppressed negative values.
	public static Complex[] SuppressNegative(Complex[] z) {
		int zlen = z.length;
		int zlenHalf = zlen / 2;
		Complex[] zHilbert = new Complex[zlen];
		for (int i = zlenHalf; i < zlen; i++) {
			zHilbert[i] = new Complex(0, 0);
		}
		for (int i = 0; i < zlenHalf; i++) {
			zHilbert[i] = z[i].times(2);
		}

		return zHilbert;
	}

	// Public Void Kill dead zone
	public static double[] zeroDeadZone(int index, int pulseLenght,
			double[] signal) {
		index = index + pulseLenght;
		for (int i = 0; i <= index; i++) {
			signal[i] = 0;
		}
		return signal;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static int[] peakIndex(double[] timeSeries, double thres,
			 int sharpness) {
		int[] peaks = new int[2];
		int length = timeSeries.length;
		int count = 0;
		int start = getMaxIndex(timeSeries);
		for (int i = start; i < length - sharpness; i++) {
			if (timeSeries[i] > thres) {
				if ((timeSeries[i] > timeSeries[i + sharpness])
						&& (timeSeries[i] > timeSeries[i - sharpness])) {
					peaks[count] = i;
					if(count < 1){
						count++;
					}
					
					i+=10; // For bumbs
				
				}
			}
		}
		//double[] realivant = Arrays.copyOfRange(timeSeries, start-30, start+500);
		return peaks;

	}
	/*
	 * //This method calculate public static double[] CrossCorr(short[] signal,
	 * short[] pulse){ double[] xcorrR = new double[signal.length+pulse.length];
	 * for(int i=0; i<xcorrR.length; i++ ){ int sum =0; for(int j=0;
	 * j<pulse.length; j++){ if(i+j<signal.length){ sum+= signal[i+j]*pulse[j];
	 * } } xcorrR[i] = Math.abs(sum); }
	 * 
	 * return xcorrR; }
	 */

}

// 0 1 2 3 0
// 0 0 1 2 3 0
// 0 0 0 1 2 3
