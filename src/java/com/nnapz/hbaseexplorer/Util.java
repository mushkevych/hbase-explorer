/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nnapz.hbaseexplorer;

/**
 * todo: use some common library
 * @author Bob Schulze
 */
public class Util {

   public static boolean getBoolean(String value, boolean def) {
      if (value == null | value.length() == 0) return def;
      return Boolean.valueOf(value);
  }

  public static int getInt(String value, int def) {
      if (value == null || value.length() == 0) return def;
      return Integer.parseInt(value);
  }

}
