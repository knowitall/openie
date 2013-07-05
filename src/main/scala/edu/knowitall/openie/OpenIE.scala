package edu.knowitall.openie

import edu.knowitall.srlie.SrlExtraction
import edu.knowitall.tool.tokenize.ClearTokenizer
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.postag.OpenNlpPostagger
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.postag.ClearPostagger
import edu.knowitall.chunkedextractor.Relnoun
import edu.knowitall.srlie.SrlExtractor
import edu.knowitall.tool.stem.MorphaStemmer
import edu.knowitall.srlie.SrlExtraction.Argument
import edu.knowitall.srlie.SrlExtraction.Relation
import edu.knowitall.chunkedextractor.BinaryExtractionInstance
import edu.knowitall.srlie.SrlExtractionInstance
import edu.knowitall.tool.stem.Lemmatized
import edu.knowitall.tool.chunk.ChunkedToken
import edu.knowitall.tool.parse.graph.DependencyNode
import edu.knowitall.tool.srl.Roles
import edu.knowitall.collection.immutable.Interval

class OpenIE(triples: Boolean = false) {
  // sentence pre-processors
  val tokenizer = new ClearTokenizer()
  val postagger = new OpenNlpPostagger(tokenizer)
  val chunker = new OpenNlpChunker(postagger)
  val parser = new ClearParser(new ClearPostagger(tokenizer))

  // subextractors
  val relnoun = new Relnoun
  val srlie = new SrlExtractor

  def apply(sentence: String): Seq[Extraction] = extract(sentence)
  def extract(sentence: String): Seq[Extraction] = {
    // pre-process the sentence
    val chunked = chunker(sentence) map MorphaStemmer.lemmatizePostaggedToken
    val parsed = parser(sentence)

    // run extractors
    val srlExtrs = srlie(parsed)
    val relnounExtrs = relnoun(chunked)

    def convertRelnoun(inst: BinaryExtractionInstance[Lemmatized[ChunkedToken]]): Extraction = {
      new Extraction(
        rel = new Part(inst.extr.rel.text, Seq(inst.extr.rel.offsetInterval),
        arg1 = new Part(inst.extr.arg1.text, Seq(inst.extr.arg1.offsetInterval))),
        arg2s = Seq(new Part(inst.extr.arg2.text, Seq(inst.extr.arg2.interval))),
        context = None,
        negated = false)
    }

    val extrs = srlExtrs ++ convertRelnoun(relnounExtrs)
  }
}

