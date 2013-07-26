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

/**
 * An activity that displays a compass that visually tracks the wearer's
 * heading. Tapping the touchpad will cause Glass to speak the current
 * heading.
 */
public class CompassActivity extends Activity
    implements SensorEventListener, TextToSpeech.OnInitListener {

  private SensorManager mSensorManager;
  private TextToSpeech mSpeech;
  
  private CompassView mCompassView;
  
  private String[] mDirectionNames;
  private String[] mSpokenDirections;
  private float[] mRotationMatrix;
  private float[] mOrientation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_compass);

    mCompassView = (CompassView) findViewById(R.id.compass);
    
    mDirectionNames = getResources().getStringArray(R.array.direction_abbreviations);
    mSpokenDirections = getResources().getStringArray(R.array.spoken_directions);

    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mSpeech = new TextToSpeech(this, this);
    mRotationMatrix = new float[16];
    mOrientation = new float[3];
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
      // Get the current heading from the compass view and then ask the
      // text-to-speech engine to speak it.
      float heading = mCompassView.getHeading();

      Resources res = getResources();
      String speechFormat = res.getString(R.string.spoken_heading_format);
      String directionName = mSpokenDirections[getDirectionIndex(heading)];
      String headingText = String.format(speechFormat, (int) heading, directionName);
      mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);
      return true;
    default:
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
      // Get the current heading from the sensor, then update the graphical
      // compass.
      SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
      SensorManager.remapCoordinateSystem(mRotationMatrix,
          SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
      SensorManager.getOrientation(mRotationMatrix, mOrientation);

      float heading = (float) Math.toDegrees(mOrientation[0]);
      mCompassView.setHeading(heading);
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
   * Starts tracking the user's heading.
   */
  private void startTracking() {
    mSensorManager.registerListener(this,
        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        SensorManager.SENSOR_DELAY_UI);
  }

  /**
   * Stops tracking the user's heading.
   */
  private void stopTracking() {
    mSensorManager.unregisterListener(this);
  }
  
  /**
   * Converts the specified heading angle into an index between 0-15 that can
   * be used to retrieve the direction name for that heading (known as "boxing
   * the compass, down to the half-wind level).
   * 
   * @param heading the heading angle.
   * @return the index of the direction name for the angle.
   */
  private int getDirectionIndex(float heading) {
    float partitionSize = 360.0f / mDirectionNames.length;
    float displacedHeading = MathUtils.mod(heading + partitionSize / 2, 360.0f);
    return (int) (displacedHeading / partitionSize);
  }
}
