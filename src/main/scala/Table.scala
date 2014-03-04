package com.knownstylenolife.hive.tool

class Table(val name: String, val columns: List[Column], val viewOriginalText:Option[String]) {
  def hasColumn(s: String): Boolean = columns.exists(_.name == s)
  def hasPartitionColumn(s: String): Boolean = columns.exists(c => c.name == s && c.isPartition)
  def isView():Boolean = viewOriginalText.nonEmpty
  def getViewReferreredTables():List[String]
    = """\s+from\s+([\w_]+)""".r.findAllMatchIn(viewOriginalText.getOrElse("").toLowerCase).map(_.group(1)).toList
}
class Column(val name: String, val columnType: String, val comment: String, val isPartition: Boolean)
