package edu.knowitall.openie

import edu.knowitall.collection.immutable.Interval

case class Extraction(arg1: Part, rel: Part, arg2s: Seq[Part], context: Option[Part], negated: Boolean)

case class Part(text: String, offsets: Seq[Interval]) {
  def offsetSpan = Interval.span(offsets)
}