package flip.measure

import flip.pdf.Prim
import org.scalactic._
import TripleEquals._
import org.scalactic.Tolerance._

object PrimOps {

  def similarForError(prim1: Prim, prim2: Prim, error: Double): Boolean = {
    if (prim1 == 0) {
      prim1 == prim2
    } else {
      prim1 === (prim2 +- math.abs(prim1 * error))
    }
  }

  def similarForTolerance(prim1: Prim, prim2: Prim, tol: Prim): Boolean = {
    prim1 === (prim2 +- tol)
  }

}

trait PrimSyntax {

  implicit val defaultError: Double = 0.05

  implicit class PrimSyntaxImpl(prim: Prim) {
    def ~=(prim2: Prim)(implicit error: Double): Boolean = PrimOps.similarForError(prim, prim2, error)
    def ~=(primTol: (Prim, Prim)): Boolean = PrimOps.similarForTolerance(prim, primTol._1, primTol._2)
  }

}
