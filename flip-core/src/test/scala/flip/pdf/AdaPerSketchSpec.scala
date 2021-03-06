package flip.pdf

import org.specs2.mutable._
import org.specs2.ScalaCheck
import flip._
import flip.conf.{CustomAdaPerSketchConf, CustomSketchConf}
import flip.measure.syntax._

import scala.language.postfixOps

class AdaPerSketchSpec extends Specification with ScalaCheck {

  "AdaPerSketch" should {

    "type invariance" in {

      "type invariance after update" in {
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = 10,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch: Sketch[Double] = AdaPerSketch.empty[Double]

        sketch.update(1).fold(ko("Exception occurs.")){
          case sketch: AdaPerSketch[Double] => if(sketch.queue.nonEmpty) ok else ko("Empty queue.")
          case _ => ko(s"sketch: $sketch")
        }
      }

      "type invariance after rearrange" in {
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = 10,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch.update(1)
          sketch2 <- sketch1.rearrange
        } yield sketch2)
          .fold(ko("Exception occurs.")){
            case sketch: AdaPerSketch[Double] => if(sketch.queue.isEmpty) ok else ko("Queue is not cleaned.")
            case _ => ko(s"sketch: $sketch")
          }
      }

    }

    "queue" in {

      "empty queue" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch = AdaPerSketch.empty[Double]

        val queue = sketch.asInstanceOf[AdaPerSketch[Double]].queue
        if(queue.nonEmpty) ko("Queue of empty sketch have to be empty.") else ok
      }

      "queue io" in {
        val queueSize = 2
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch.update(1)
          sketch2 <- sketch1.update(2)
          sketch3 <- sketch2.update(3)
          q2 = sketch2.asInstanceOf[AdaPerSketch[Double]].queue
          q3 = sketch3.asInstanceOf[AdaPerSketch[Double]].queue
        } yield (q2, q3))
          .fold(ko("Exception occurs.")){
            case (q2, q3) =>
              if(q3.size != queueSize) ko(s"queue1 size: ${q3.size}.")
              else if(q3.size != q3.size) ko(s"queue1 size(${q3.size}) and queue2 size(${q3.size}) have to equal.")
              else if(q2.headOption == q3.headOption) ko(s"queue1: $q2 == queue2: $q3")
              else ok
          }
      }

    }

    "count" in {

      "queue only" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch0.update(1)
          count <- sketch1.count(1, 1)
        } yield count)
          .fold(ko("Exception occurs."))(count => if(count <= 0) ko(s"Counts nothing: $count") else ok)
      }

      "queue and structure" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch0.update(1)
          sketch2 <- sketch1.update(1)
          count <- sketch2.count(0.9, 1.1)
        } yield count)
          .fold(ko("Exception occurs."))(count => if(count <= 1) ko(s"Counts nothing: $count") else ok)
      }

    }

    "sum" in {

      "queue only" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch0.update(1)
          sum = sketch1.sum
        } yield sum)
          .fold(ko("Exception occurs."))(sum => if(sum < 1) ko(s"Counts nothing: $sum") else ok)
      }

      "queue and structure" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 100, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]

        (for {
          sketch1 <- sketch0.update(1)
          sketch2 <- sketch1.update(1)
          sum = sketch2.sum
        } yield sum)
          .fold(ko("Exception occurs."))(sum => if(sum < 2) ko(s"Counts nothing: $sum") else ok)
      }

    }

    "narrowUpdate" in {
      implicit val conf: CustomAdaPerSketchConf = SketchConf(
        cmapSize = 2000, cmapNo = 20, cmapStart = Some(-10d), cmapEnd = Some(10d),
        counterSize = 1000, counterNo = 10
      )
      val sketch0 = AdaPerSketch.empty[Double]

      sketch0.narrowUpdate(0) must beAnInstanceOf[Some[Sketch[Double]]]
    }

    "deepUpdate" in {
      implicit val conf: CustomAdaPerSketchConf = SketchConf(
        cmapSize = 2000, cmapNo = 20, cmapStart = Some(-10d), cmapEnd = Some(10d),
        counterSize = 10000, counterNo = 10
      )
      val sketch0 = AdaPerSketch.empty[Double]

      sketch0.deepUpdate(0.0 to 10.0 by 0.1: _*) must beAnInstanceOf[Some[Sketch[Double]]]
    }

    "rearrange" in {

      "" in {
        val cmapSize = 20
        implicit val conf: CustomAdaPerSketchConf = CustomSketchConf(
          cmapSize = cmapSize, cmapNo = 2, cmapStart = Some(-10.0), cmapEnd = Some(10.0),
          queueSize = 50,
          counterSize = cmapSize
        )
        val sketch0 = AdaPerSketch.empty[Double]
        val (_, samples) = NumericDist.normal(0.0, 1).samples(100)

        (for {
          sketch1 <- sketch0.narrowUpdate(samples: _*)
          sketch2 <- sketch1.rearrange
          sketch3 <- sketch2.narrowUpdate(samples: _*)
        } yield sketch3)
          .fold(ko)(_ => ok)
      }

    }

    "fastPdf" in {

      "queue only" in {
        val queueSize = 10
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 20, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]
        val p = 0.5

        (for {
          sketch1 <- sketch0.update(0, 0, 1, 2, 4, 5)
          interpPdf <- SamplingDist.interpolationPdf(sketch1, p)
          fastPdf <- Sketch.fastPdf(sketch1, p)
        } yield (interpPdf, fastPdf))
          .fold(ko("Exception occurs.")){ case (interpPdf, fastPdf) =>
            if(interpPdf ~= fastPdf) ok else ko(s"interpPdf: $interpPdf, fastPdf: $fastPdf")
          }
      }

      "structure dominant" in {
        val queueSize = 1
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 20, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]
        val p = 0.5

        (for {
          sketch1 <- sketch0.update(0, 0, 1, 2, 4, 5)
          interpPdf <- SamplingDist.interpolationPdf(sketch1, p)
          fastPdf <- Sketch.fastPdf(sketch1, p)
        } yield (interpPdf, fastPdf))
          .fold(ko("Exception occurs.")){ case (interpPdf, fastPdf) =>
            if(interpPdf ~= fastPdf) ok else ko(s"interpPdf: $interpPdf, fastPdf: $fastPdf")
          }
      }

      "mixed" in {
        val queueSize = 3
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = queueSize,
          cmapSize = 20, cmapNo = 2, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]
        val p = 0.5

        (for {
          sketch1 <- sketch0.update(0, 1, 2, 4, 5, 0)
          interpPdf <- SamplingDist.interpolationPdf(sketch1, p)
          fastPdf <- Sketch.fastPdf(sketch1, p)
        } yield (interpPdf, fastPdf))
          .fold(ko("Exception occurs.")){ case (interpPdf, fastPdf) =>
            if(interpPdf ~= fastPdf) ok else ko(s"interpPdf: $interpPdf, fastPdf: $fastPdf")
          }
      }

      "multiple cmaps" in {
        implicit val conf: CustomAdaPerSketchConf = SketchConf(
          queueSize = 3,
          startThreshold = 2d, thresholdPeriod = 2d,
          cmapSize = 20, cmapNo = 3, cmapStart = Some(-10d), cmapEnd = Some(10d),
          counterSize = 20, counterNo = 2
        )
        val sketch0 = AdaPerSketch.empty[Double]
        val p = 2.2

        (for {
          sketch1 <- sketch0.update(0, 1, 2, 4, 5, 0, 1, 2, 3, 4, 5, 2, 2)
          interpPdf <- SamplingDist.interpolationPdf(sketch1, p)
          fastPdf <- Sketch.fastPdf(sketch1, p)
        } yield (interpPdf, fastPdf))
          .fold(ko("Exception occurs.")){ case (interpPdf, fastPdf) =>
            if(interpPdf ~= fastPdf) ok else ko(s"interpPdf: $interpPdf, fastPdf: $fastPdf")
          }
      }

    }

    // End

  }

}