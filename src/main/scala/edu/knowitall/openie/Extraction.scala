package edu.knowitall.openie

import edu.knowitall.collection.immutable.Interval

/***
 * The abstract representation of an extraction.
 *
 * @params  arg1  the Argument 1
 * @params  rel  the Relation
 * @params  arg2s  a sequence of the Argument 2s
 * @params  context  an optional representation of the context for this extraction
 * @params  negated  whether this is a true or false assertion
 * @params  passive  whether this is a passive or active assertion
 */
case class Extraction(arg1: Part, rel: Part, arg2s: Seq[Part], context: Option[Part], negated: Boolean, passive: Boolean) {
  def tripleString = s"($arg1; $rel; ${arg2s.mkString("; ")})"
  override def toString = {
    val basic = tripleString
    context match {
      case Some(context) => context + ":" + basic
      case None => basic
    }
  }
}

/***
 * A component of an extraction.
 */
case class Part(text: String, offsets: Seq[Interval]) {
  def offsetSpan = Interval.span(offsets)

  override def toString = text
}