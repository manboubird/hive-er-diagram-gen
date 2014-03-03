package com.knownstylenolife.hive.tool

import java.io.{FileWriter, File}

object Main extends App {
  override val args: Array[String] = if (super.args.isEmpty) Array("./samples/desc", "./samples/erMeta.txt", "./target/er.dot") else super.args

  val srcDir = args(0)
  val metaFile = args(1)
  val outputFile = new File(args(2))

  val tool = new Tool()

  System.out.println("Output file = " + outputFile.getPath)
  System.out.println("Generate png:\n  dot -Tpng " + outputFile.getPath + " > ./target/er.png & open ./target/er.png")
  writeFile(outputFile, tool.generate(new File(srcDir), new File(metaFile)))

  def writeFile(f:File, s:String):Unit = {
    val out = new FileWriter(f)
    out.write(s)
    out.close
  }
}
