package com.knownstylenolife.hive.tool

import com.knownstylenolife.hive.tool.{Render, Table, TableParser, ErMetaParser, ErMeta}
import scala.io.Source
import java.io.File

class Tool {

  val tp:TableParser = new TableParser
  val emp:ErMetaParser = new ErMetaParser
  val r:Render = new Render

  /**
   * Read hives' describe-TABLE output files under a directory and a ER meta file.
   * Render graphviz dot format content.
   *
   * @param descFileDir
   * @param erMetaFile
   * @return
   */
  def generate(descFileDir:File, erMetaFile:File):String
    = r.render(parseTableFiles(descFileDir), parseErMetaFile(erMetaFile))

  private def parseTableFiles(dir:File):List[Table] = dir.listFiles.filter(_.getPath.endsWith(".txt")).map{ f =>
        System.out.println(s"Reading file: $f")
        tp.parse(getTableName(f), readIntoList(f))
  }.toList
  private def parseErMetaFile(f:File):ErMeta = emp.parse(readIntoList(f))
  private def getTableName(f:File):String = f.getName.replaceFirst("""\..+$""","")
  private def readIntoList(f:File):List[String] = Source.fromFile(f)(scala.io.Codec.ISO8859).getLines.toList
}
