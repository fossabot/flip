package flip.pdf.monad

import flip.conf.SmoothDistConf
import flip.measure.Measure
import flip.pdf._
import flip.pdf.arithmetic._
import flip.range.RangeM

object PointToPointSketchBind extends SketchBind[Sketch, Dist, Sketch] {

  def bind[A, B](sketch: Sketch[A], f: A => Dist[B], measureB: Measure[B]): Sketch[B] =
    (for {
      // bindToDist
      sampling <- sketch.sampling
      weightDists = sampling.records.map {
        case (range, value) =>
          (range.roughLength * value, f(sketch.measure.from(range.middle)) match {
            case dist: DeltaDist[B] =>
              val conf = SmoothDistConf(delta = sketch.conf.delta)
              UniformDist.apply(dist.pole, range.roughLength)(measureB, conf)
            case dist => dist
          })
      }
      bindedDist = Sum.weightedSum(weightDists, measureB)
      // find sampling points
      smplPointBs = weightDists
        .map(_._2)
        .flatMap(dist => samplingPoints(dist, sketch.conf.bindSampling))
        .sortBy(smpl => measureB.to(smpl))
      sum = sketch.sum
      samplesB = smplPointBs
        .sliding(2)
        .toList
        .flatMap {
          case start :: end :: Nil =>
            val domainB = RangeM(start, end)(measureB)
            bindedDist.probability(domainB.start, domainB.end).map(prob => (domainB.middle, sum * prob))
          case _ => None
        }
        .filter { case (_, count) => !count.isNaN }
    } yield Sketch.concat(samplesB)(measureB, sketch.conf))
      .getOrElse(Sketch.empty(measureB, sketch.conf))

  def samplingPoints[A](dist: Dist[A], samplingNo: Int): List[A] = dist match {
    case sketch: Sketch[A] =>
      sketch.samplingPoints.flatMap(ps => ps.start :: ps.end :: Nil).distinct
    case delta: DeltaDist[A] =>
      val measure = delta.measure
      val poleP = measure.to(delta.pole)
      val width = delta.conf.delta
      measure.from(poleP - width) :: measure.from(poleP + width) :: Nil
    case numeric: NumericDist[A] =>
      val unit = 1 / (samplingNo.toDouble + 1)
      (0 to samplingNo).toList.map(i => numeric.icdf(i * unit))
  }

//  def discretizeRecords[A](records: List[((A, A), Count)]): List[(A, Count)] =
//    records.flatMap { case (range, count) => (range._1, count / 2) :: (range._2, count / 2) :: Nil }

}
