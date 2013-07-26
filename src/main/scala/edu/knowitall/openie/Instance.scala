package edu.knowitall.openie

case class Instance(confidence: Double, sentence: String, extraction: Extraction) {
  def conf = confidence
  def extr = extraction

  override def toString = f"$conf%1.2f $extraction"
}