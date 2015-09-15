package DSP;

public class Chirp {

	public static double[] linearChirp(double phase, double f0, double f1, double t1, 
			double samplingFreq){
		double k = (f1-f0)/t1;
		int samples = (int)Math.ceil(t1*samplingFreq)+1;//Padding incase of double precision complications.
		double[] chirp =new double[samples];
		int count =0;
		double inc =1/samplingFreq; //Stops it from having to have it calculate at every iteration
		for(double t=0; t<=t1; t+=inc){
			chirp[count] =	Math.sin(phase+2*Math.PI*(f0*t +(k/2)*t*t));
			count++;
		}
		return chirp;
	}
	
	

}