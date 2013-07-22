/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.glass.samples.compass;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.widget.TextView;

/**
 * An activity that displays a compass that visually tracks the wearer's
 * bearing and also displays with the numerical value in degrees. Tapping
 * the touchpad will cause Glass to speak the current bearing.
 */
public class CompassActivity extends Activity
    implements SensorEventListener, TextToSpeech.OnInitListener {

  private SensorManager mSensorManager;
  private TextToSpeech mSpeech;
  
  private CompassView mCompassView;
  private TextView mBearingView;
  
  private String[] mDirectionNames;
  private String[] mSpokenDirections;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_compass);

    mCompassView = (CompassView) findViewById(R.id.compass);
    mBearingView = (TextView) findViewById(R.id.bearing);

    mDirectionNames = getResources().getStringArray(R.array.direction_abbreviations);
    mSpokenDirections = getResources().getStringArray(R.array.spoken_directions);

    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mSpeech = new TextToSpeech(this, this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    startTracking();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopTracking();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSpeech.shutdown();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    // Handle tap events on the touchpad.
    case KeyEvent.KEYCODE_DPAD_CENTER:
    case KeyEvent.KEYCODE_ENTER:
      // Get the current bearing from the compass view and then ask the
      // text-to-speech engine to speak it.
      double bearing = mCompassView.getBearing();

      Resources res = getResources();
      String speechFormat = res.getString(R.string.spoken_bearing_format);
      String directionName = mSpokenDirections[getDirectionIndex(bearing)];
      String bearingText = String.format(speechFormat, (int) bearing, directionName);
      mSpeech.speak(bearingText, TextToSpeech.QUEUE_FLUSH, null);
      return true;
    default:
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
      // Get the current bearing from the sensor, then update the graphical
      // compass and the text view.
      double bearing = event.values[0];
      mCompassView.setBearing(bearing);
  
      Resources res = getResources();
      String textFormat = res.getString(R.string.display_bearing_format);
      String directionName = mDirectionNames[getDirectionIndex(bearing)];
      String bearingText = String.format(textFormat, (int) bearing, directionName);
      mBearingView.setText(bearingText);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // We don't need to do anything here; a full app may want to display a
    // message to the user if the sensor's accuracy becomes unreliable.
  }

  @Override
  public void onInit(int status) {
    // Called when the text-to-speech engine is initialized; we don't need
    // to do anything here.
  }

  /**
   * Starts tracking the user's bearing.
   */
  @SuppressWarnings("deprecation")
  private void startTracking() {
    // TODO(allevato): Sensor type TYPE_ORIENTATION was deprecated in API 8;
    // the current way of obtaining the device orientation is to use a
    // combination of TYPE_ACCELEROMETER, TYPE_MAGNETIC_FIELD, and some matrix
    // math. However, the results also appear to require some smoothing when
    // doing this, so we'll use the deprecated sensor for now with the
    // intention of updating this in the future.

    mSensorManager.registerListener(this,
        mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  /**
   * Stops tracking the user's bearing.
   */
  private void stopTracking() {
    mSensorManager.unregisterListener(this);
  }
  
  /**
   * Converts the specified bearing angle into an index between 0-15 that can
   * be used to retrieve the direction name for that bearing (known as "boxing
   * the compass, down to the half-wind level).
   * 
   * @param bearing the bearing angle.
   * @return the index of the direction name for the angle.
   */
  private int getDirectionIndex(double bearing) {
    double partitionSize = 360.0 / mDirectionNames.length;
    double displacedBearing = bearing + partitionSize / 2;
    if (displacedBearing > 360.0) {
      displacedBearing -= 360.0;
    }

    return (int) (displacedBearing / partitionSize);
  }
}
