package edu.knowitall.openie

import org.scalatest._

class OpenIESpecTest extends FlatSpec with Matchers {
  "OpenIE" should "instantiate and extract correctly" in {
    val openie = new OpenIE()

    val insts = openie("U.S. president Obama gave a speech")

    insts.size should be (2)
    insts.map(_.extraction.toString).sorted should be (Seq("(Obama; [is] president [of]; U.S.)", "(U.S. president Obama; gave; a speech)"))
  }
}