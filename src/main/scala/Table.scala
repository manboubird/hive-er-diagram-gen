package com.knownstylenolife.hive.tool

class Table(val name: String, val columns: List[Column]) {
  def hasColumn(s: String): Boolean = columns.exists(_.name == s)
  def hasPartitionColumn(s: String): Boolean = columns.exists(c => c.name == s && c.isPartition)
}
class Column(val name: String, val columnType: String, val comment: String, val isPartition: Boolean)
