package com.nnapz.hbaseexplorer.domain
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
 * A LinkPattern encapsulates the information that is needed to automatically provide a link on result screens for
 * your data model.
 *
 * LinkPatterns are applied to all output fields whose full qualifier matched the pattern.
 *
 * The Pattern is expected to be a Java RegEx
 *
 * hBasePattern can be applied to results of Hbase queries. The target Link may point to just anything.
 *
 */

public class HbasePattern {

 //  enum TypeOpts { STRING, HBASE_LONG, HBASE_DOUBLE }
    public static String STRING = "String"
    public static String HBASE_LONG = "Hbase_LONG"
    public static String HBASE_DOUBLE = "Hbase_DOUBLE"

    String hbase         ; // a regex is expected
    String table
    String family
    String column
    String value = '.*'   ; // by default all match

    boolean linkColumn    ; // link is set onto column value
    boolean linkValue     ; // link is set on value
    String targetLink       ; // anything with placeholders $cloud, $table, $family, $column, $value
    String type  = STRING ; // a type that is supported for display

//    static hasMany = [types:TypeOptions]


    static constraints = {
      type(inList:[STRING, HBASE_LONG, HBASE_DOUBLE])
      targetLink(nullable: false)
    }
}
