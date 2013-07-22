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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draws a stylized compass. The red needle of the compass always points north.
 */
public class CompassView extends View {

  private static final int BG_ARROW_COLOR = Color.rgb(64, 64, 64);
  private static final int SOUTH_COLOR = Color.rgb(192, 192, 192);
  private static final int NORTH_COLOR = Color.rgb(255, 0, 0);

  private double mBearing;
  private Path mLightArrowPath;
  private Path mDarkArrowPath;
  private Paint mPaint;
  private Rect mTextBounds;
  private String mNorthLabel;

  public CompassView(Context context) {
    this(context, null, 0);
  }

  public CompassView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CompassView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    mPaint = new Paint();
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setAntiAlias(true);
    mPaint.setTextSize(22.0f);

    mLightArrowPath = new Path();
    mDarkArrowPath = new Path();

    // Retrieve the string that will be used to label the north end of the
    // needle and measure it for later.
    mNorthLabel = context.getResources().getStringArray(R.array.direction_abbreviations)[0];
    mTextBounds = new Rect();
    mPaint.getTextBounds(mNorthLabel, 0, mNorthLabel.length(), mTextBounds);
  }

  /**
   * Gets the current bearing in degrees.
   * 
   * @return the current bearing.
   */
  public double getBearing() {
    return mBearing;
  }

  /**
   * Sets the current bearing in degrees and redraws the compass.
   * 
   * @param degrees the current bearing.
   */
  public void setBearing(double degrees) {
    mBearing = degrees;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float width = getWidth();
    float centerX = width / 2f;
    float centerY = getHeight() / 2f;
    float distance = Math.min(width, getHeight()) - mTextBounds.height() * 2f;

    float largeGirth = width * 0.10f;
    float smallGirth = width * 0.075f;

    // Draw the arrows that represent the four cardinal directions.
    for (int i = 0; i < 4; i++) {
      drawArrow(canvas, i * 90, centerX, centerY, distance / 2, largeGirth, BG_ARROW_COLOR);
    }

    // Draw the arrows that represent the four ordinal directions.
    for (int i = 0; i < 4; i++) {
      drawArrow(canvas, i * 90 + 45, centerX, centerY, distance / 2.5f, smallGirth,
          BG_ARROW_COLOR);
    }

    // Draw the compass needles that point north and south.
    drawArrow(canvas, mBearing, centerX, centerY, distance / 2, largeGirth, NORTH_COLOR);
    drawArrow(canvas, mBearing + 180, centerX, centerY, distance / 2, largeGirth, SOUTH_COLOR);

    // Draw the small "joint" about which the needle rotates.
    mPaint.setColor(Color.LTGRAY);    
    canvas.drawCircle(centerX, centerY, width * 0.03f, mPaint);

    // Draw the north indicator at the end of the needle.
    canvas.save();
    canvas.rotate((float) (360 - mBearing), centerX, centerY);    
    mPaint.setColor(Color.WHITE);
    canvas.drawText(mNorthLabel, centerX - mTextBounds.width() / 2, mTextBounds.height() * 1.5f,
        mPaint);
    canvas.save();
  }

  /**
   * Draws a triangular arrow on the canvas.
   * 
   * @param canvas the canvas on which to draw the arrow.
   * @param angle the angle at which to draw the arrow.
   * @param x the x-coordinate of the center of the <em>base</em> of the arrow.
   * @param y the y-coordinate of the center of the <em>base</em> of the arrow.
   * @param distance the length of the arrow from its base.
   * @param girth the "girth" (width) of the arrow.
   * @param mPaint the {@code Paint} object used to describe the arrow's appearance.
   */
  private void drawArrow(Canvas canvas, double angle, float x, float y, float distance,
      float girth, int color) {
    // Construct two paths -- one for the light side of the arrow and one for
    // the dark side -- to obtain a soft 3D effect.
    mLightArrowPath.reset();
    mLightArrowPath.moveTo(x, y);
    mLightArrowPath.lineTo(x - girth / 2, y);
    mLightArrowPath.lineTo(x, y - distance);
    mLightArrowPath.close();

    mDarkArrowPath.reset();
    mDarkArrowPath.moveTo(x, y - distance);
    mDarkArrowPath.lineTo(x + girth / 2, y);
    mDarkArrowPath.lineTo(x, y);
    mDarkArrowPath.close();

    // Rotate the canvas to the requested angle and draw the arrow path. Note
    // that we rotate the canvas in the opposite direction as the desired
    // bearing so that the compass needle will always point north.
    canvas.save();
    canvas.rotate((float) (360 - angle), x, y);

    // Draw both sides of the arrow.
    mPaint.setColor(color);
    canvas.drawPath(mLightArrowPath, mPaint);
    mPaint.setColor(darkenColor(color));
    canvas.drawPath(mDarkArrowPath, mPaint);

    canvas.restore();
  }
  
  /**
   * Returns a color that is 70% as bright as the specified color.
   *  
   * @param color the color to darken.
   * @return the darkened version of the color.
   */
  private static int darkenColor(int color) {
    int r = (int) (Color.red(color) * 0.7);
    int g = (int) (Color.green(color) * 0.7);
    int b = (int) (Color.blue(color) * 0.7);
    return Color.rgb(r, g, b);
  }
}
