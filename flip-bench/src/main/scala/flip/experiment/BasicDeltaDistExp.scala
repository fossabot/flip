package flip.experiment

import flip.experiment.ops.ExpOutOps
import flip._

object BasicDeltaDistExp {

  def main(args: Array[String]): Unit = {
    val expName = "basic-delta"
    val dataNo = 10
    val samplingNo = 100

    implicit val conf: SketchConf = SketchConf(
      startThreshold = 2,
      thresholdPeriod = 1,
      cmapSize = samplingNo,
      cmapNo = 2,
      cmapStart = Some(-10d),
      cmapEnd = Some(10d),
      counterSize = samplingNo
    )
    val sketch = Sketch.empty[Double]
    val (_, datas) = Dist.delta(0.1).samples(dataNo)
    val dataIdxs = datas.zipWithIndex

    var tempSketchO: Option[Sketch[Double]] = Option(sketch)
    val idxUtdSketches: List[(Int, Sketch[Double])] = (0, sketch) :: dataIdxs.flatMap {
      case (data, idx) =>
        tempSketchO = tempSketchO.flatMap(_.update(data))
        tempSketchO.map(sketch => (idx + 1, sketch))
    }
    val plots = idxUtdSketches.flatMap { case (idx, utdSkt) => utdSkt.densityPlot.map(plot => (idx, plot)) }

    ExpOutOps.clear(expName)
    ExpOutOps.writePlots(expName, "pdf", plots)
  }

}
