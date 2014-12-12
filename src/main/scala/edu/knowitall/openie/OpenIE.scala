package edu.knowitall.openie

import com.google.common.base.CharMatcher
import edu.knowitall.chunkedextractor.BinaryExtractionInstance
import edu.knowitall.chunkedextractor.Relnoun
import edu.knowitall.chunkedextractor.confidence.RelnounConfidenceFunction
import edu.knowitall.collection.immutable.Interval
import edu.knowitall.srlie.SrlExtraction
import edu.knowitall.srlie.SrlExtractionInstance
import edu.knowitall.srlie.SrlExtractor
import edu.knowitall.srlie.confidence.SrlConfidenceFunction
import edu.knowitall.tool.chunk.ChunkedToken
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.parse.DependencyParser
import edu.knowitall.tool.postag.OpenNlpPostagger
import edu.knowitall.tool.srl.Srl
import edu.knowitall.tool.stem.MorphaStemmer
import edu.knowitall.tool.tokenize.OpenNlpTokenizer
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.ClearSrl

class OpenIE(parser: DependencyParser = new ClearParser(), srl: Srl = new ClearSrl(), triples: Boolean = false) {
  // confidence functions
  val srlieConf = SrlConfidenceFunction.loadDefaultClassifier()
  val relnounConf = RelnounConfidenceFunction.loadDefaultClassifier()

  // sentence pre-processors
     val tokenizer = new OpenNlpTokenizer()
     val postagger = new OpenNlpPostagger(tokenizer)
     val chunkerOIE = new OpenNlpChunker(postagger)
  // Comented above as added these in method apply

  // subextractors
  val relnoun = new Relnoun
  val srlie = new SrlExtractor(srl)

  /***
   * Remove problematic characters from a line before extracting.
   */
  def clean(line: String): String = {
    var cleaned = line.replace('’','\'').replace('–','-')
    
    cleaned = CharMatcher.WHITESPACE.replaceFrom(cleaned, ' ')
    cleaned = CharMatcher.JAVA_ISO_CONTROL.removeFrom(cleaned)

    cleaned
  }

  def apply(sentence: String): Seq[Instance] = extract(sentence)
  def extract(sentence: String, chunker : OpenNlpChunker = chunkerOIE): Seq[Instance] = {
    // pre-process the sentence
    val cleaned = clean(sentence)
    val chunked = chunker(cleaned) map MorphaStemmer.lemmatizePostaggedToken
    val parsed = parser(cleaned)

    // run extractors
    val srlExtrs: Seq[SrlExtractionInstance] =
      if (triples) srlie(parsed).flatMap(_.triplize())
      else srlie(parsed)
    val relnounExtrs = relnoun(chunked)

    def convertSrl(inst: SrlExtractionInstance): Instance = {
      def offsets(part: SrlExtraction.MultiPart) = {
        var intervals = part.intervals
        // hack: sorted should not be necessary
        // see https://github.com/knowitall/srlie/issues/8)
        var tokens = part.tokens.sorted
        var offsets = List.empty[Interval]
        while (!intervals.isEmpty) {
          val sectionTokens = tokens.take(intervals.head.size)
          tokens = tokens.drop(intervals.head.size)
          intervals = intervals.drop(1)

          offsets ::= Interval.open(sectionTokens.head.offsets.start, sectionTokens.last.offsets.end)
        }

        offsets.reverse
      }
      try {
        val extr = new Extraction(
          rel = new Relation(inst.extr.rel.text, offsets(inst.extr.rel)),
          // can't use offsets field due to a bug in 1.0.0-RC2
          arg1 = new SimpleArgument(inst.extr.arg1.text, Seq(Interval.open(inst.extr.arg1.tokens.head.offsets.start, inst.extr.arg1.tokens.last.offsets.end))),
          arg2s = inst.extr.arg2s.map { arg2 =>
            val offsets = Seq(Interval.open(arg2.tokens.head.offsets.start, arg2.tokens.last.offsets.end))
            arg2 match {
              case arg2: SrlExtraction.TemporalArgument => new TemporalArgument(arg2.text, offsets)
              case arg2: SrlExtraction.LocationArgument => new SpatialArgument(arg2.text, offsets)
              case arg2: SrlExtraction.Argument => new SimpleArgument(arg2.text, offsets)
            }
          },
          context = inst.extr.context.map(context => new Context(context.text, Seq(Interval.open(context.tokens.head.offsets.start, context.tokens.last.offsets.end)))),
          negated = inst.extr.negated,
          passive = inst.extr.passive)
        Instance(srlieConf(inst), sentence, extr)
      }
      catch {
        case e: Exception =>
          throw new OpenIEException("Error converting SRL instance to Open IE instance: " + inst, e)
      }
    }

    def convertRelnoun(inst: BinaryExtractionInstance[ChunkedToken]): Instance = {
      val extr = new Extraction(
        rel = new Relation(inst.extr.rel.text, Seq(inst.extr.rel.offsetInterval)),
        arg1 = new SimpleArgument(inst.extr.arg1.text, Seq(inst.extr.arg1.offsetInterval)),
        arg2s = Seq(new SimpleArgument(inst.extr.arg2.text, Seq(inst.extr.arg2.offsetInterval))),
        context = None,
        negated = false,
        passive = false)
      Instance(relnounConf(inst), sentence, extr)
    }

    val extrs = (srlExtrs map convertSrl) ++ (relnounExtrs map convertRelnoun)

    extrs
  }
}

