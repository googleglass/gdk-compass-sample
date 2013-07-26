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
 * Draws a stylized compass, with text labels at the cardinal and ordinal
 * directions, and tick marks at the half-winds. The red "needles" in the
 * display mark the current heading.
 */
public class CompassView extends View {

  private static final float NEEDLE_WIDTH = 6;
  private static final float NEEDLE_HEIGHT = 125;
  private static final int NEEDLE_COLOR = Color.RED;
  private static final float TICK_WIDTH = 2;
  private static final float TICK_HEIGHT = 10;
  private static final float TEXT_HEIGHT = 84.0f;

  private String[] mDirections;
  private float mHeading;
  private Paint mPaint;
  private Paint mTickPaint;
  private Path mPath;
  private Rect mTextBounds;

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
    mPaint.setTextSize(TEXT_HEIGHT);

    mTickPaint = new Paint();
    mTickPaint.setStyle(Paint.Style.STROKE);
    mTickPaint.setStrokeWidth(TICK_WIDTH);
    mTickPaint.setAntiAlias(true);
    mTickPaint.setColor(Color.WHITE);

    mPath = new Path();
    mTextBounds = new Rect();

    mDirections = context.getResources().getStringArray(R.array.direction_abbreviations);
  }

  /**
   * Gets the current heading in degrees.
   * 
   * @return the current heading.
   */
  public float getHeading() {
    return mHeading;
  }

  /**
   * Sets the current heading in degrees and redraws the compass. If the angle
   * is not between 0 and 360, it is shifted to be in that range.
   * 
   * @param degrees the current heading.
   */
  public void setHeading(float degrees) {
    mHeading = MathUtils.mod(degrees, 360.0f);
    invalidate();
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // The view displays 90 degrees across its width so that one 90 degree
    // head rotation is equal to one full view cycle.
    float pixelsPerDegree = getWidth() / 90.0f;
    float degreesPerTick = 360.0f / mDirections.length;
    float centerX = getWidth() / 2.0f;
    float centerY = getHeight() / 2.0f;

    canvas.save();
    canvas.translate(-mHeading * pixelsPerDegree + centerX, centerY);

    mPaint.setColor(Color.WHITE);

    // We draw two extra ticks/labels on each side of the view so that the
    // full range is visible even when the heading is approximately 0.
    for (int i = -2; i <= mDirections.length + 2; i++) {
      if (MathUtils.mod(i, 2) == 0) {
        // Draw a text label for the even indices.
        String direction = mDirections[MathUtils.mod(i, mDirections.length)];
        mPaint.getTextBounds(direction, 0, direction.length(), mTextBounds);

        canvas.drawText(direction, i * degreesPerTick * pixelsPerDegree - mTextBounds.width() / 2,
            mTextBounds.height() / 2, mPaint);
      }
      else {
        // Draw a tick mark for the odd indices.
        canvas.drawLine(i * degreesPerTick * pixelsPerDegree, -TICK_HEIGHT / 2,
            i * degreesPerTick * pixelsPerDegree, TICK_HEIGHT / 2, mTickPaint);
      }
    }

    canvas.restore();

    mPaint.setColor(NEEDLE_COLOR);
    drawNeedle(canvas, false);
    drawNeedle(canvas, true);
  }

  /**
   * Draws a needle that is centered at the top or bottom of the compass.
   * 
   * @param canvas the {@link Canvas} upon which to draw.
   * @param bottom true to draw the bottom needle, or false to draw the top
   *     needle.
   */
  private void drawNeedle(Canvas canvas, boolean bottom) {
    float centerX = getWidth() / 2.0f;

    float origin;
    float sign;

    // Flip the vertical coordinates if we're drawing the bottom needle.
    if (bottom) {
      origin = getHeight();
      sign = -1;
    }
    else {
      origin = 0;
      sign = 1;
    }

    float needleHalfWidth = NEEDLE_WIDTH / 2;

    mPath.reset();
    mPath.moveTo(centerX - needleHalfWidth, origin);
    mPath.lineTo(centerX - needleHalfWidth, origin + sign * (NEEDLE_HEIGHT - 4));
    mPath.lineTo(centerX, origin + sign * NEEDLE_HEIGHT);
    mPath.lineTo(centerX + needleHalfWidth, origin + sign * (NEEDLE_HEIGHT - 4));
    mPath.lineTo(centerX + needleHalfWidth, origin);
    mPath.close();
    
    canvas.drawPath(mPath, mPaint);
  }
}

