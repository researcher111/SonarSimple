package com.example.sonarsimple;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TempSense  implements SensorEventListener {
	  private SensorManager mSensorManager;
	  private Sensor mTemp;
	  public static float tempature=23; //Set the tempuature to def if no sensor is present 
	  public  TempSense (Context context) {
	    // Get an instance of the sensor service, and use that to get an instance of
	    // a particular sensor.
	    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	    mTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
	    mSensorManager.registerListener(this, mTemp, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
	    // Do something here if sensor accuracy changes.
	  }

	  @Override
	  public final void onSensorChanged(SensorEvent event) {
		  try{
	     tempature = event.values[0];
		  }catch(Exception e){
			  tempature = 23;
		  }
	    // Do something with this sensor data.
	  }

	  protected void CleanUp() {
	    // Be sure to unregister the sensor when the activity pauses.
	    mSensorManager.unregisterListener(this);
	  }

	}
