package com.example.sonarsimple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import DSP.DSP;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Thread to manage live recording/playback of voice input from the device's microphone.
 */
public class Sonar extends Thread {

	public int threshold = 10000;
	public int thresholdPeak = 0;
	public double maxDistanceMeters = 5;
	private final int sampleRate = 44100;
	public static Result result;
	// Chirp Configurations
	public static double f0 = 4000;
	public static double f1 = 8000;
	public static double t1 = 0.01;
	public static double phase = 0;
	private final int numSamples = (int) Math.round(t1 * sampleRate);
	private final int bufferSize = 32768;
	public static boolean stopped = false;
	public static double distance;
	public static double xcorrHeight;
	public static int pulseTrack = 10;
	public static int delay = 0;
	public int deadZoneLength = 60;// (int)(sampleRate*t1/6);
	private static String AUDIO_FILE_PATH = "";
	private Context context;
	private TempSense tempSense;
	/**
	 * Give the thread high priority so that it's not canceled unexpectedly, and
	 * start it
	 * 
	 * @param readingView
	 */
	public Sonar(int thresholdPeak, Context context) {
		this.thresholdPeak = thresholdPeak;
		this.context = context;
		this.tempSense = new TempSense(context);
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		// start();
	}

	@Override
	public void run() {
		this.thresholdPeak = thresholdPeak;
		Log.i("Audio", "Running Audio Thread");
		AudioRecord recorder = null;
		AudioTrack track = null;
		short[] buffer = new short[bufferSize];
		short[] pulse = DSP.ConvertToShort(DSP.padSignal(DSP.HanningWindow(
				DSP.linearChirp(phase, f0, f1, t1, sampleRate), 0, numSamples),
				bufferSize, delay));
		int ix = 0;

		int N = AudioRecord.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(AudioSource.MIC, sampleRate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize * 2);
		
//		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
//				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
//				bufferSize * 2, AudioTrack.MODE_STREAM);
		
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize * 2, AudioTrack.MODE_STREAM);

		track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
		//writeStringToTextFile(Arrays.toString(pulse), AUDIO_FILE_PATH
		//		+ "pulse.txt");
		// short[] buffer = buffers[ix++ % buffers.length];
		try {

			recorder.startRecording();
			track.write(pulse, 0, pulse.length);

            track.play();
			recorder.read(buffer, 0, buffer.length);

		} finally {
			track.stop();
			track.release();
			recorder.stop();
			recorder.release();
		}

		//writeStringToTextFile(Arrays.toString(buffer), AUDIO_FILE_PATH
		//		+ "buffer" + (pulseTrack++) + ".txt");
		result = FilterAndClean.Distance(buffer, pulse, sampleRate, threshold,
				maxDistanceMeters, deadZoneLength, thresholdPeak, this.tempSense.tempature);

	}

	/**
	 * Called from outside of the thread in order to stop the recording/playback
	 * loop
	 * 
	 */
	private void close() {
		stopped = true;
	}

	/*
	 * private void emailFile(String s){
	 * 
	 * Intent i = new Intent(Intent.ACTION_SEND); i.setType("message/rfc822");
	 * i.putExtra(Intent.EXTRA_EMAIL , new String[]{"dggraham@email.wm.edu"});
	 * i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
	 * i.putExtra(Intent.EXTRA_TEXT , s); try {
	 * context.startActivity(Intent.createChooser(i, "Send mail...")); } catch
	 * (android.content.ActivityNotFoundException ex) { Toast.makeText(context,
	 * "There are no email clients installed.", Toast.LENGTH_SHORT).show(); } }
	 */

	// this method writes the pulse array to a file
	/*
	 * private void writeStringToTextFile(String s, String f) { //File sdCard =
	 * Environment.get .getExternalStorageDirectory(); //File dir = new
	 * File(sdCard.getAbsolutePath()); //dir.mkdirs(); //File file = new
	 * File(f); Context ctx = this.context; try { FileOutputStream f1 =
	 * ctx.openFileOutput(f, Context.MODE_PRIVATE); PrintStream p = new
	 * PrintStream(f1); p.print(s); p.close(); f1.close(); } catch
	 * (FileNotFoundException e) {
	 * 
	 * } catch (IOException e) {
	 * 
	 * }
	 * 
	 * }
	 */

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getAlbumStorageDir(String albumName) {
		// Get the directory for the user's public pictures directory.
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				albumName);
		if (isExternalStorageWritable()) {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					Log.e("Write Error", "Directory not created");
				}
			}
		} else {
			Log.e("Write Error", "Storage Not writable");
		}
		return file;
	}

	public void writeStringToTextFile(String text, String fileName) {

		try {
			File dir = getAlbumStorageDir("Sonar");
			File file = new File(dir, fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(text);
			output.close();
			//MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, null);
		} catch (IOException e) {
			Log.e("Write Error", "Failed to write content");
		}
	}

}