package org.scalameter
package utils



import org.scalatest.FunSuite



class ZipperTest extends FunSuite {

  test("Zipper.descend,ascend") {
    val zipper = Tree.Zipper.root[Int]
    val one = Key[Int]("one")
    assert(zipper.descend.ascend.result == Tree[Int](currentContext, Seq(), Seq(
      Tree(currentContext, Seq(), Seq())
    )))
    assert(zipper.descend.ascend.descend.ascend.result == Tree[Int](currentContext, Seq(), Seq(
      Tree(currentContext, Seq(), Seq()),
      Tree(currentContext, Seq(), Seq())
    )))
    assert(zipper.descend.ascend.descend.addItem(1).ascend.result == Tree[Int](currentContext, Seq(), Seq(
      Tree(currentContext, Seq(), Seq()),
      Tree(currentContext, Seq(1), Seq())
    )))
    assert(zipper.descend.addContext(one -> 1).ascend.descend.addItem(1).ascend.result == Tree[Int](currentContext, Seq(), Seq(
      Tree(currentContext + (one -> 1), Seq(), Seq()),
      Tree(currentContext, Seq(1), Seq())
    )))
  }

}



