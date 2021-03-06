package flip.pdf.monad

import flip.pdf.{Dist, Sketch}

import scala.language.higherKinds

trait SketchBind[Sketch1[_] <: Sketch[_], D[_] <: Dist[_], Sketch2[_] <: Sketch[_]]
    extends SamplingDistBind[Sketch1, D, Sketch2] {}
