package com.nnapz.hbaseexplorer.services
import org.apache.hadoop.hbase.util.Bytes
import com.nnapz.hbaseexplorer.domain.HbasePattern
import java.util.regex.Matcher
import java.util.regex.Pattern

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

/**
 * This service provides methods to manipulate the display of values. Its a service, because many things can be improved
 * by keeping initialized instances around (todo).
 */
class PatternService {

    boolean transactional = false

    /**
    * return a HTMLified String for the column, should it match any pattern
    * @param hbaseSource the actual hbaseSource name whose values are displayed
    */
    String transformColumn(String hbaseSource,  String table, String family, byte[] column, byte[] value) {
       // go trough all patterns and apply the first match
       String columnString = Bytes.toString(column)
       String valueString = Bytes.toString(value)
       String ret = columnString.encodeAsHTML()
       HbasePattern.findAll().find { HbasePattern pattern ->
         if (match(pattern.hbase, hbaseSource) && match(pattern.table, table) &&
             match(pattern.family, family) && match(pattern.column, columnString) && match(pattern.value, valueString)) {
           if (pattern.linkColumn) {
             String columnValue = applyType(pattern.type, column)
             ret =  applyLink(pattern, columnValue, hbaseSource, table, family, columnValue, valueString /* no types here :-( */)
             return true
           }
         }
         return false
       }
       return ret;
    }

    /**
    * return a HTMLified String for the value field, should it match any pattern
    */
    String transformValue(String hbaseSource,  String table, String family, byte[] column, byte[] value) {
       // go trough all patterns and apply the first match
       String columnString = Bytes.toString(column)
       String valueString = Bytes.toString(value)
       String ret = valueString?.encodeAsHTML()
       HbasePattern.findAll().find { HbasePattern pattern ->
         if (match(pattern.hbase, hbaseSource) && match(pattern.table, table) &&
             match(pattern.family, family) && match(pattern.column, columnString) && match(pattern.value, valueString)) {
           if (pattern.linkValue) {
              String valueValue = applyType(pattern.type, value)
              ret =  applyLink(pattern, valueValue, hbaseSource, table, family, columnString /* no types here */ , valueValue)
              return true
           }
         }
         return false
       }
       return ret;
    }

  String applyType(String typeName, byte[] data) {
     if (typeName.equals(HbasePattern.STRING)) {
       return Bytes.toString(data)
     } else if (typeName.equals(HbasePattern.HBASE_LONG)) {
       return Bytes.toLong(data) + ''
     } else if (typeName.equals(HbasePattern.HBASE_DOUBLE)) {
       return Bytes.toDouble(data) + ''
     } else {
       return new String(data)
     }
  }

  /**
   * Make a HTML link out of the link description in pattern:
   * * make it HTML save
   * * URL Encode the link parts 
   * * exchange the placeholders with the real values from the args. If a pattern contained a regex-group (), the first
   *   group is used to replace the placeholder, otherwise the complete value is used.
   * @param linkText the unencoded html link body
   */
  String applyLink(HbasePattern pattern,String linkText, String hbaseSource, String table, String family, String column, String value) {
     String target = pattern.targetLink

     if (target && target.trim().length() > 0) {

       // do we have groups in the patterns?
       String hbaseReplacement = getReplacements(pattern.hbase, hbaseSource)
       String tableReplacement = getReplacements(pattern.table, table)
       String familyReplacement = getReplacements(pattern.family, family)
       String columnReplacement = getReplacements(pattern.column, column)
       String valueReplacement = getReplacements(pattern.value, value)

       String link = target.replaceAll('@HBASE@', hbaseReplacement).replaceAll("@TABLE@", tableReplacement)
               .replaceAll('@FAMILY@', familyReplacement)
               .replaceAll('@COLUMN@', columnReplacement).replaceAll('@VALUE@', valueReplacement);
      return "<a href=\"" + link  + "\">" + linkText.encodeAsHTML() + "</a>"
     } else {
      return linkText.encodeAsHTML()
     }

  }

  /**
   * check if a group match has to be looked at.
   * @returns a ready html string, incl. encoding.
   */
  private String getReplacements(String pattern, String targetElement) {
    String ret = targetElement
    if (pattern && pattern.contains('(')) {
      Pattern p = Pattern.compile(pattern)    // bad to do it again...
      Matcher matcher = new Matcher(p, targetElement)
      String groupMatch = matcher.group(1)
      if (groupMatch) {
        ret = groupMatch
      }
    }
    return URLEncoder.encode(ret, "UTF-8");
  }

  /**
   * match a single field
   */
  boolean match(String patternValue, String actualValue) {
    //println "Match " + patternValue + "=" + actualValue
    if (!patternValue) return false
    if (patternValue.equals('.*')) return true
    if (!actualValue) return false
    return actualValue.matches(patternValue)    // oops, heavy
  }


}
