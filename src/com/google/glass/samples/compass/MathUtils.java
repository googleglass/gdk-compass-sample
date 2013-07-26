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

/**
 * A utility class containing math helper methods used in other classes.
 */
public class MathUtils {

  /**
   * Calculates {@code a mod b} in a way that respects negative values (for
   * example, {@code mod(-1, 5) == 4}, rather than {@code -1}).
   * 
   * @param a the dividend.
   * @param b the divisor.
   * @return {@code a mod b}.
   */
  public static int mod(int a, int b) {
    return (a % b + b) % b;
  }

  /**
   * Calculates {@code a mod b} in a way that respects negative values (for
   * example, {@code mod(-1, 5) == 4}, rather than {@code -1}).
   * 
   * @param a the dividend.
   * @param b the divisor.
   * @return {@code a mod b}.
   */
  public static float mod(float a, float b) {
    return (a % b + b) % b;
  }
}
