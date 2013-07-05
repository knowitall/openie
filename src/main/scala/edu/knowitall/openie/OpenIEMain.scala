package edu.knowitall.openie

import java.io.File
import java.io.PrintWriter

import scala.io.Source

import resource._

object OpenIEMain extends App {
  case class Config(inputFile: Option[File] = None,
    outputFile: Option[File] = None) {
    def source() = {
      inputFile match {
        case Some(file) => Source.fromFile(file, "UTF-8")
        case None => Source.fromInputStream(System.in, "UTF-8")
      }
    }

    def writer() = {
      outputFile match {
        case Some(file) => new PrintWriter(file, "UTF8")
        case None => new PrintWriter(System.out)
      }
    }
  }

  val argumentParser = new scopt.immutable.OptionParser[Config]("openie") {
    def options = Seq(
      argOpt("input file", "input file") { (string, config) =>
        val file = new File(string)
        require(file.exists, "input file does not exist: " + file)
        config.copy(inputFile = Some(file))
      },
      argOpt("ouput file", "output file") { (string, config) =>
        val file = new File(string)
        config.copy(outputFile = Some(file))
      })
  }

  argumentParser.parse(args, Config()) match {
    case Some(config) => run(config)
    case None =>
  }

  def run(config: Config) {
    val openie = new OpenIE()

    for {
      source <- managed(config.source())
      writer <- managed(config.writer())

      line <- source.getLines
      extr <- openie.extract(line)
    } {
      println(extr)
    }
  }
}