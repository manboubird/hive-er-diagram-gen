package com.knownstylenolife.hive.tool

import scala.collection.mutable.ListBuffer

class TableParser {

  private object State {
    case object START extends State()
    case object COLUMNS extends State("""^# col_name\s+data_type\s+comment\s*""")
    case object PARTITION_COLUMNS extends State("""^# Partition Information\s*""")
    case object DETAIL extends State("""^# Detailed Table Information\s*""")
    case object VIEW_INFO extends State("""^# View Information\s*""")
    case object VIEW_INFO_ORIGINAL_TEXT extends State("""^View Original Text:(.+)""")
    case object VIEW_INFO_EXPANDED_TEXT extends State("""^View Expanded Text:(.+)""")

    private val successStates:Set[State] = Set(DETAIL, VIEW_INFO, VIEW_INFO_ORIGINAL_TEXT, VIEW_INFO_EXPANDED_TEXT)

    def isSuccess(state:State):Boolean = successStates.contains(state)
  }

  val COLUMN_MATCH = """^([\w-]+)\s+([^\s]+)\s+(.+)""".r

  def parse(table_name: String, lines: List[String]): Table = {
    var state:State = State.START
    val columns = new ListBuffer[Column]()
    var isView:Boolean = false
    val viewOriginalTextSb = new StringBuilder()

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
        case State.VIEW_INFO.boundary_re() =>
          state = State.VIEW_INFO
          isView = true
        case State.VIEW_INFO_ORIGINAL_TEXT.boundary_re(s) =>
          state = State.VIEW_INFO_ORIGINAL_TEXT
          viewOriginalTextSb.append(s.trim)
        case State.VIEW_INFO_EXPANDED_TEXT.boundary_re(s) =>
          state = State.VIEW_INFO_EXPANDED_TEXT
        case _ => state match {
          case State.COLUMNS | State.PARTITION_COLUMNS => line match {
            case COLUMN_MATCH(n, t, c) =>
//            System.out.println("n=" + n + ", t=" + t + ", c=" + c)
            columns.append(new Column(n, t, c, state == State.PARTITION_COLUMNS))
            case _ =>
          }
          case State.VIEW_INFO_ORIGINAL_TEXT =>
            viewOriginalTextSb.append(" ").append(line.trim)
          case _ =>
        }
      }
    }

    if(State.isSuccess(state) == false) {
      throw new IllegalStateException("Fail to parse the content. table_name = " + table_name + ", state = " + state.toString)
    }
    val viewOriginalText = if(viewOriginalTextSb.isEmpty) None else Some(viewOriginalTextSb.toString)
    new Table(table_name, columns.toList, viewOriginalText)
  }
}

class ErMetaParser {

  private object State {
    case object START extends State()
    case object MASTER_TABLE_RELATIONS extends State("""^# master table relations\s*""")
    case object SUB_GROUP extends State("""^# sub group\s*""")
    case object REGEX_SUB_GROUP extends State("""^# columId regular expression matched sub group\s*""")
  }

  val REGEX_SUB_GROUP_MATCH = """(.+),""(.+)""\s*""".r
  val COMMENT_MATCH = """^#.*""".r

  def parse(lines:List[String]):ErMeta = {
    var state:State = State.START
    val masterTableRelations = new ListBuffer[MasterTableRelation]()
    val subGroups = new ListBuffer[SubGroup]()
    val regexSubGroups = new ListBuffer[RegexSubGroup]()

    for (line <- lines) {
      line match {
        case State.MASTER_TABLE_RELATIONS.boundary_re() =>
          state = State.MASTER_TABLE_RELATIONS
        case State.SUB_GROUP.boundary_re() =>
          state = State.SUB_GROUP
        case State.REGEX_SUB_GROUP.boundary_re() =>
          state = State.REGEX_SUB_GROUP
        case "" =>
        case COMMENT_MATCH() =>
        case _ => state match {
          case State.MASTER_TABLE_RELATIONS =>
            val splits = line.split(",")
            val masterTable = TableEntity.create(splits.head)
            val tables = splits.tail.map(columnId =>
              if(TableEntity.hasColumn(columnId)) TableEntity.create(columnId) else new TableEntity(columnId, masterTable.column)
            ).toList
            masterTableRelations.append(new MasterTableRelation(masterTable, tables))
          case State.SUB_GROUP =>
            val splits = line.split(",")
            subGroups.append(new SubGroup(splits.head, splits.tail.map(e => TableEntity.create(e)).toList))
          case State.REGEX_SUB_GROUP => line match {
            case REGEX_SUB_GROUP_MATCH(label, re) =>
              regexSubGroups.append(new RegexSubGroup(label, re))
            case _ =>
          }
          case _ =>
        }
      }
    }
    new ErMeta(masterTableRelations.toList, subGroups.toList, regexSubGroups.toList)
  }
}

sealed abstract class State(boundary_regex:String) {
  val boundary_re = boundary_regex.r
  def this() = this("")
  def eqEither(states:State*) = states.exists(_ == this)
}
