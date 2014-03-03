package com.knownstylenolife.hive.tool

import scala.collection.mutable.ListBuffer

class TableParser {

  private object State {
    case object START extends State()
    case object COLUMNS extends State("""^# col_name\s+data_type\s+comment\s*""")
    case object PARTITION_COLUMNS extends State("""^# Partition Information\s*""")
    case object DETAIL extends State("""^# Detailed Table Information\s*""")
  }

  val COLUMN_MATCH = """^([\w-]+)\s+([^\s]+)\s+(.+)""".r

  def parse(table_name: String, lines: List[String]): Table = {
    var state:State = State.START
    val columns = new ListBuffer[Column]()
    var table: Option[Table] = None

    for (line <- lines) {
      line match {
        case State.COLUMNS.boundary_re() => state match {
          case State.START =>
            state = State.COLUMNS
          case _ =>
        }
        case State.PARTITION_COLUMNS.boundary_re() =>
          state = State.PARTITION_COLUMNS
        case State.DETAIL.boundary_re() =>
          state = State.DETAIL
          table = Some(new Table(table_name, columns.toList))
        case _ => state match {
            case State.COLUMNS | State.PARTITION_COLUMNS => line match {
              case COLUMN_MATCH(n, t, c) =>
//              System.out.println("n=" + n + ", t=" + t + ", c=" + c)
              columns.append(new Column(n, t, c, state == State.PARTITION_COLUMNS))
            case _ =>
          }
          case _ =>
        }
      }
    }
    if(table.isEmpty) {
      throw new IllegalStateException("Cannot parse the content. table_name = " + table_name)
    }
    table.get
  }
}

class ErMetaParser {

  private object State {
    case object START extends State()
    case object MASTER_TABLE_RELATIONS extends State("""^# master table relations\s*""")
    case object SUB_GROUP extends State("""^# sub group\s*""")
  }

  def parse(lines:List[String]):ErMeta = {
    var state:State = State.START
    val masterTableRelations = new ListBuffer[MasterTableRelation]()
    val subGroups = new ListBuffer[SubGroup]()

    for (line <- lines) {
      line match {
        case State.MASTER_TABLE_RELATIONS.boundary_re() =>
          state = State.MASTER_TABLE_RELATIONS
        case State.SUB_GROUP.boundary_re() =>
          state = State.SUB_GROUP
        case "" =>
        case _ => state match {
          case State.MASTER_TABLE_RELATIONS =>
            val splits = line.split(",")
            val mst = TableEntity.create(splits(0))
            val tables = new ListBuffer[TableEntity];
            for (i <- 1 until splits.length) {
              var te = TableEntity.create(splits(i))
              if(te.column.isEmpty) {
                te = new TableEntity(splits(i), mst.column)
              }
              tables.append(te)
            }
            masterTableRelations.append(new MasterTableRelation(mst,tables.toList))
          case State.SUB_GROUP =>
            val splits = line.split(",")
            val tables = new ListBuffer[TableEntity]
            for (i <- 1 until splits.length) {
              tables.append(TableEntity.create(splits(i)))
            }
            subGroups.append(new SubGroup(splits(0), tables.toList))
        }
      }
    }
    new ErMeta(masterTableRelations.toList, subGroups.toList)
  }
}

sealed abstract class State(boundary_regex:String) {
  def this() = this("")
  val boundary_re = boundary_regex.r
}
