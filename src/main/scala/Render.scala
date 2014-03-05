package com.knownstylenolife.hive.tool

import scala.collection.mutable.HashSet

/**
 * Graphviz dot format render
 */
class Render {

  val DIAGRAM_HEADER="""digraph R {
                       |  rankdir=LR
                       |  %s
                       |  node [style=rounded]
                       |""".stripMargin
  val DIAGRAM_FOOTER="""}
               |""".stripMargin
  val SUB_GRAPH_HEADER="""subgraph "cluster%s" {
                         |  label="%s"
                         |  """.stripMargin
  val SUB_GRAPH_FOOTER=
    """}
      |""".stripMargin
  val TBL_ALL=
    """%s
      |%s
      ||
      |<table border='0' align='left' cellspacing='2' cellpadding='0' width='12'>
      |%s
      |</table>
      |>];
    """.stripMargin
  val TBL_HEADER=""""%s" [shape=record,label=<
                   | <table border='0' align='center' cellspacing='0.5' cellpadding='0' width='12'>
                   |   <tr><td align='center' valign='bottom' width='10'><font face='Arial Bold' point-size='11'>%s %s</font></td></tr>
                   |   </table>""".stripMargin
  val TBL_VIEW_CONTENT = """||
                           | <table border='0' cellspacing='0.5' cellpadding='0' width='12'>
                           |  <tr><td valign='bottom' width='10'>
                           |    <font face='Arial' point-size='11'>View selects: %s</font>
                           |  </td></tr>
                           |</table>""".stripMargin
  val TR_CONTENT=
    """  <tr><td align='left' width='10'>%s<font face='Arial Italic' color='grey60'>%s%s</font></td></tr>
      |""".stripMargin
  val RELATIONS_FOREIGN_KEY_CONTENT=""" {%s} -> %s;
                  |""".stripMargin
  val RELATIONS_VIEW_REFERRERED_TABLE_CONTENT=""" %s -> {%s} [headlabel="ref: %s", color="red", fontsize="8"];
                          |""".stripMargin

  /**
   * Generate dot format ER diagram
   *
   * @param tables
   * @param erMeta
   * @return
   */
  def render(tables: List[Table], erMeta:ErMeta):String = {
    val sb = new StringBuilder()
    val opts = if(Config.isPrintableSetting) """  graph[page="11.7,16.5",size="11.7,16.5",center=true]""" else ""
    sb.append(DIAGRAM_HEADER.format(opts))
    sb.append(getTables(tables, erMeta))
    sb.append(getMasterTableRelations(erMeta.masterTableRelations))
    sb.append(getViewReferreredTableRelations(tables))
    sb.append(DIAGRAM_FOOTER)
    sb.toString()
  }

  private def getSubGroups(tables:List[Table], subGroup:List[SubGroup]):(String, Set[String]) = {
    val sb = new StringBuilder()
    val appendedTableNameSet = new HashSet[String]()

    for((subGroup, i) <- subGroup.view.zipWithIndex) {
      sb.append("\n").append(SUB_GRAPH_HEADER.format("-%d".format(i), subGroup.label)).append("\n")
      sb.append(getTableContent(tables.filter(table => subGroup.tables.exists(tableEntity => tableEntity.table == table.name))))
      sb.append("\n").append(SUB_GRAPH_FOOTER)
      subGroup.tables.map(tableEntity => appendedTableNameSet.add(tableEntity.table))
    }
    (sb.toString, appendedTableNameSet.toSet)
  }

  private def getSubGroups(tables:List[Table], label:String, idSuffix:String):String = {
    val sb = new StringBuilder()
    sb.append("\n").append(SUB_GRAPH_HEADER.format(idSuffix, label)).append("\n")
    sb.append(getTableContent(tables))
    sb.append("\n").append(SUB_GRAPH_FOOTER)
    sb.toString
  }

  private def getTables(tables:List[Table], erMeta:ErMeta):String = {
    val sb = new StringBuilder()
    val appendedTableNameSet = new HashSet[String]()

    // TODO Refactoring
    for((g, i) <- erMeta.regexSubGroup.view.zipWithIndex) {
      val filtered = tables.filter( t => t.name.matches(g.regex.r.toString()))
      sb.append(getSubGroups(filtered, g.label, "-regex-%d".format(i)))
      appendedTableNameSet ++= filtered.map(_.name).toSet
    }
    // TODO filter tables by appendedTableNameSet or admit duplicate table outputs?
    val t:(String, Set[String]) = getSubGroups(tables, erMeta.subGroup)
    sb.append(t._1)
    appendedTableNameSet ++= t._2

    // output rest
    sb.append(getTableContent(tables.filter(table => appendedTableNameSet.contains(table.name) == false)))
    sb.toString
  }

  private def toOneLine(s:String):String = s.replaceAll(""" *\n""","")

  private def replace_special_chars(s:String):String = s.replaceAll("<","&lt;").replaceAll(">","&gt;")

  private def getTableContent(tables:List[Table]):String = tables.map{ t =>
    val sb = new StringBuilder()

    val nameExt:String = if (t.isView) " (V)" else ""
    val name = TBL_HEADER.format(t.name, t.name, nameExt)
    val desc = if(t.isView()) TBL_VIEW_CONTENT.format(t.getViewReferreredDistinctTables().mkString(", ")) else ""
    val rows = t.columns.map{ c =>
      val columnExt:String = if (c.isPartition) " (P)" else ""
      TR_CONTENT.format(c.name, replace_special_chars(c.columnType), columnExt)
    }.mkString("")
    sb.append(TBL_ALL.format(name, desc, rows))

    toOneLine(sb.toString())
  }.mkString("\n\n")


  private def getMasterTableRelations(relations:List[MasterTableRelation]):String
    = relations.map{ r => RELATIONS_FOREIGN_KEY_CONTENT.format(r.tables.map(_.table).mkString(" "), r.masterTable.table)}.mkString("\n")
  private def getViewReferreredTableRelations(tables:List[Table]):String =
    tables.filter(_.isView).map{t =>
      val heads = t.getViewReferreredDistinctTables
      RELATIONS_VIEW_REFERRERED_TABLE_CONTENT.format(t.name, heads.mkString(" "), t.name)
    }.mkString("\n")

}
