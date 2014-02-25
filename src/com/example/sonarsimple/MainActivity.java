package com.example.sonarsimple;

import java.io.IOException;
import java.text.DecimalFormat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	public double[] sumArray;
	public double[] xcorrSum;
	
	public boolean recording = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final DecimalFormat df = new DecimalFormat("#.####");
		Button measurebutton= (Button) findViewById(R.id.measureButton);
		final GraphView graphView = new LineGraphView(  
			      this // context  
			      , "Absolute Xcorr" // heading  
			);  
		
		final GraphView graphViewSignal = new LineGraphView(  
			      this // context  
			      , "Raw Signal" // heading  
			);  
		
		final TextView readingView = (TextView) findViewById(R.id.readingsView);
    	final TextView feetView = (TextView) findViewById(R.id.feetView);
    	final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1); 
    
    	seekBar.setProgress(22);
    	final Sonar sonsys = new Sonar(seekBar.getProgress(), getApplicationContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 
       final TextView seekBarValue = (TextView)findViewById(R.id.ProgressValue); 

        	   @Override 
        	   public void onProgressChanged(SeekBar seekBar, int progress, 
        	     boolean fromUser) { 

        		   seekBarValue.setText(String.valueOf(progress)); 
        	   }

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			} 
        	   }); 
        	  
    	
		measurebutton.setOnClickListener(new View.OnClickListener() {
			@Override
		    public void onClick(View v) {
		    	//sonarSystem.playPluse();

				
				double[] distances = new double[10];
				for(int i=0 ; i< 10; i++ ){
				sonsys.thresholdPeak = seekBar.getProgress();
				//final long startTime = System.currentTimeMillis();
				sonsys.run();
				//final long endTime = System.currentTimeMillis();
				//Toast.makeText(getApplicationContext(),"Total execution time: " + (endTime - startTime), Toast.LENGTH_LONG).show();
				//feetView.setText(String.valueOf( (endTime - startTime))); 
		    	//readingView.setText(Double.toString(sonsys.distance));
		    	//
				String distanceMeters = df.format(sonsys.result.distance);
		    	//String distanceFeet = df.format(sonsys.result.distance*3.28084);
		    	distances[i] = sonsys.result.distance;
		    	//This diplays the reading from them tempature sensor 
		    	//String distanceFeet = df.format(TempSense.tempature);
		    	
		    	//readingView.setText(distanceMeters);
		    	//feetView.setText(distanceFeet);
		    	if(distanceMeters.equals("0")){ //Bad Reading so we get zero zero
		    	// Toast.makeText(getApplicationContext(), "Bad Reading Try Adjusting The Treshold", Toast.LENGTH_LONG).show();
		    		i--;
		    	}
		    	if(!distanceMeters.equals("0")){
		    		sumArray =  getSumArray(convertFromShortArrayToDoubleArray(sonsys.result.signal), sumArray);
		    		sumArray = divideArray(sumArray ,10);
		    		xcorrSum =  getSumArray(sonsys.result.xcorr, xcorrSum);
		    		xcorrSum = divideArray(xcorrSum ,10);
		    	}
				}
		    	GraphViewSeries exampleSeries =new GraphViewSeries(generateSeries(xcorrSum)); //new GraphViewSeries(generateSeries(sonsys.result.xcorr));
		    	//exampleSeries.resetData(generateSeries(sonsys.result.signal));
		    	GraphViewSeries constantline = new GraphViewSeries(generateConstantLine(seekBar.getProgress()/10,sonsys.result.xcorr.length));
		    	graphView.addSeries(exampleSeries); // data
		    	graphView.addSeries(constantline);
		    	graphView.setViewPort(1, 2000);  
		    	graphView.setScrollable(true);  
		    	// optional - activate scaling / zooming  
		    	graphView.setScalable(true);
		    	
		    	GraphViewSeries exampleSeriesSignal = new GraphViewSeries(generateSeries(sumArray)); //new GraphViewSeries(generateSeries(sonsys.result.signal));
		    	//exampleSeries.resetData(generateSeries(sonsys.result.signal))//exampleSeries.resetData(generateSeries(sonsys.result.signal));
		    	graphViewSignal.addSeries(exampleSeriesSignal); // data  
		    	graphViewSignal.setViewPort(1, 2000);  
		    	graphViewSignal.setScrollable(true);  
		    	// optional - activate scaling / zooming  
		    	graphViewSignal.setScalable(true);
				
				
				readingView.setText(getAverage(distances)+"");	
				feetView.setText(getSTDEV(distances)+"");
		    }
		});
		
		
		LinearLayout layout1 = (LinearLayout) findViewById(R.id.layout1);  
		layout1.addView(graphView);  
		LinearLayout layout2 = (LinearLayout) findViewById(R.id.layout2);  
		layout2.addView(graphViewSignal);  
		
		// init example series data  
		
		
//	
//		
//		// graph with dynamically genereated horizontal and vertical labels  
//		GraphView graphViewSignal = new LineGraphView(  
//		  this  
//		  , signal
//		  , "Raw Signal"  
//		  , null  
//		  , null  
//		);  
//		// set view port, start=2, size=10  
//		graphViewSignal.setViewPort(2, 10);  
//		graphViewSignal.setScalable(true);  
//		//graphView.setDrawBackground(true);  
//		LinearLayout layout = (LinearLayout) findViewById(R.id.graphViewSignalLayout);  
//		layout.addView(graphViewSignal); 
//		
//		
//		// graph with dynamically genereated horizontal and vertical labels  
//		GraphView graphViewFilteredSignal = new LineGraphView(  
//		  this  
//		  , data  
//		  , "Filtered Signal "  
//		  , null  
//		  , null  
//		);  
//		// set view port, start=2, size=10  
//		graphViewFilteredSignal.setViewPort(2, 10);  
//		graphViewFilteredSignal.setScalable(true);  
//		//graphView.setDrawBackground(true);  
//		LinearLayout layoutFilter = (LinearLayout) findViewById(R.id.graphViewFilteredLayout);  
//		layout.addView(graphViewFilteredSignal); 
//		
		
	}
	
	public GraphViewData[] generateConstantLine(double value, int length){
		
		GraphViewData[] data = new GraphViewData[length];  
		double v=1000000000.0*value;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, v);  
		}  
		return data;
	}
	
	
	public GraphViewData[] generateSeries(double[] signal){
		int length = signal.length;
		GraphViewData[] data = new GraphViewData[length];  
		double v=0;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, (double)(signal[i]));  
		}  
		return data;
	}
	
	public GraphViewData[] generateSeries(short[] signal){
		int length = signal.length;
		GraphViewData[] data = new GraphViewData[length];  
		double v=0;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, (double)(signal[i]));  
		}  
		return data;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public double getAverage (double[] distances){
		double average = 0;
		for(double x: distances){
			average+= x;
		}
		return average/distances.length;
	}
	
	public double[] getSumArray(double[] measurement, double[] sumArray){
		int i =0;
		if(sumArray == null){
			sumArray = new double[measurement.length];
		}
		for(double x: measurement){
			sumArray[i]+=x;
			i++;
		}
		return sumArray;
	}
	
	public double[] divideArray(double[] sumArray, double value){
		int i=0;
		for(double x: sumArray){
			sumArray[i]=x/value;
			i++;
		}
		return sumArray;
	}
	
	public double getSTDEV(double[] distances){
		double average = getAverage(distances);
		double stdev = 0;
		for(double x : distances){
			stdev += Math.pow(x-average, 2);
		}
		return Math.pow(stdev/distances.length,0.5);
	}
	
	  public static double[] convertFromShortArrayToDoubleArray(short[] shortData) {
		    int size = shortData.length;
		    double[] doubleData = new double[size];
		    for (int i = 0; i < size; i++) {
		      doubleData[i] = shortData[i] / 32768.0;
		    }
		    return doubleData;
		  }

}
