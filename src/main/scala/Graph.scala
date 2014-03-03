package com.knownstylenolife.hive.tool

class ErMeta(val masterTableRelations:List[MasterTableRelation], val subGroup:List[SubGroup])
class SubGroup(val label:String, val tables:List[TableEntity])
class MasterTableRelation(val masterTable:TableEntity, val tables:List[TableEntity])

class TableEntity(val table:String, val column:String){
  def toIdString:String = {
    var s = table
    if(column.isEmpty == false) {
      s += ":%s".format(column)
    }
    s
  }
}

object TableEntity {
  def create(id:String): TableEntity = {
    val splits = id.split(":")
    var te:Option[TableEntity] = None
    if(splits.size == 2) {
      te = Some(new TableEntity(splits(0), splits(1)))
    }else{
      te = Some(new TableEntity(splits(0), ""))
    }
    te.get
  }
}
