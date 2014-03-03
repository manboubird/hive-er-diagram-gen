package com.knownstylenolife.hive.tool

import com.knownstylenolife.hive.tool.{Column,Table,ErMeta}
import scala.collection.mutable.HashSet

/**
 * Graphviz dot format render
 */
class Render {

  val DIAGRAM_HEADER="""digraph R {
               |  rankdir=LR
               |  node [style=rounded]
               |""".stripMargin
  val DIAGRAM_FOOTER="""}
               |""".stripMargin
  val SUB_GRAPH_HEADER="""subgraph cluster%d {
                         |  label="%s"
                         |  """.stripMargin
  val SUB_GRAPH_FOOTER=
    """}
      |""".stripMargin
  val TBL_HEADER= """"%s" [shape=record,label=<
                   |<table border='0' align='center' cellspacing='0.5' cellpadding='0' width='12'>
                   |  <tr><td align='center' valign='bottom' width='10'><font face='Arial Bold' point-size='11'> %s </font></td></tr>
                   |</table>
                   ||
                   |<table border='0' align='left' cellspacing='2' cellpadding='0' width='12'>
                   |""".stripMargin
  val TBL_HOOTER="""</table>
                   |>];
                   |""".stripMargin
  val TR_CONTENT=
    """  <tr><td align='left' width='10' port='%s'>%s<font face='Arial Italic' color='grey60'>%s</font></td></tr>
      |""".stripMargin
  val RELATIONS_CONTENT=""" {%s} -> %s;
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
    sb.append(DIAGRAM_HEADER)
    sb.append(getTables(tables, erMeta.subGroup))
    sb.append(getMasterTableRelations(erMeta.masterTableRelations))
    sb.append(DIAGRAM_FOOTER)
    sb.toString()
  }

  private def getTables(tables:List[Table], subGroup:List[SubGroup]):String = {
    val sb = new StringBuilder()
    val appendedTableNameSet = new HashSet[String]()
    for((subGroup, i) <- subGroup.view.zipWithIndex) {
      sb.append(SUB_GRAPH_HEADER.format(i, subGroup.label))
      sb.append(getTableContent(tables.filter(table => subGroup.tables.exists(tableEntitiy => tableEntitiy.table == table.name))))
      sb.append(SUB_GRAPH_FOOTER)
      subGroup.tables.map(tableEntity => appendedTableNameSet.add(tableEntity.table))
    }
    sb.append(getTableContent(tables.filter(table => appendedTableNameSet.contains(table.name) == false)))
    sb.toString
  }

  private def replace_special_chars(s:String):String = s.replaceAll("<","&lt;").replaceAll(">","&gt;")

  private def getTableContent(tables:List[Table]):String = {
    val sb = new StringBuilder()
    tables.map{ t =>
      sb.append(TBL_HEADER.format(t.name, t.name))
      t.columns.map{ c =>
        sb.append(TR_CONTENT.format(c.name, c.name, replace_special_chars(c.columnType)))
      }
      sb.append(TBL_HOOTER)
    }
    sb.toString
  }

  private def getMasterTableRelations(relations:List[MasterTableRelation]):String
    = relations.map{ r => RELATIONS_CONTENT.format(r.tables.map(_.toIdString).mkString(" "), r.masterTable.toIdString)}.mkString("\n")
}
