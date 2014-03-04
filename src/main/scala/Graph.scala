package com.knownstylenolife.hive.tool

class ErMeta(val masterTableRelations:List[MasterTableRelation], val subGroup:List[SubGroup], val regexSubGroup:List[RegexSubGroup])
class SubGroup (val label:String, val tables:List[TableEntity])
class RegexSubGroup (val label:String, val regex:String)
class MasterTableRelation(val masterTable:TableEntity, val tables:List[TableEntity])

class TableEntity(val table:String, val column:String){
  def toColumnId:String = TableEntity.getColumnId(table, column)
}

object TableEntity {

  private val COLUMN_ID_SEP_EXP = """\."""
  private val COLUMN_ID_SEP = "."

  def create(columnId:String): TableEntity = {
    val splits = columnId.split(COLUMN_ID_SEP_EXP)
    if(splits.size == 2) new TableEntity(splits(0), splits(1)) else new TableEntity(splits(0), "")
  }
  def hasColumn(columnId:String):Boolean = columnId.split(COLUMN_ID_SEP_EXP).size == 2
  def getColumnId(table:String, column:String) = if(column.isEmpty) table else "%s%s%s".format(table, COLUMN_ID_SEP, column)
}
