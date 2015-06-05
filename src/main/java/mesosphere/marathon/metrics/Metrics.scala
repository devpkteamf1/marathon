package mesosphere.marathon.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import mesosphere.marathon.metrics.Metrics.{Histogram, Meter, Timer}
import org.aopalliance.intercept.MethodInvocation

import scala.collection.concurrent.TrieMap

/**
  * Utils for timer metrics collection.
  */
class Metrics @Inject() (val registry: MetricRegistry) {
  private[this] val classNameCache = TrieMap[Class[_], String]()

  def timed[T](name: String)(block: => T): T = {
    val timer = registry.timer(name)

    val startTime = System.nanoTime()
    try {
      block
    }
    finally {
      timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
    }
  }

  def timer(name: String): Timer = {
    new Timer(registry.timer(name))
  }

  def meter(name: String): Meter = {
    new Meter(registry.meter(name))
  }

  def histogram(name: String): Histogram = {
    new Histogram(registry.histogram(name))
  }

  def name(prefix: String, clazz: Class[_], method: String): String = {
    s"${prefix}.${className(clazz)}.${method}"
  }

  def name(prefix: String, in: MethodInvocation): String = {
    name(prefix, in.getThis.getClass, in.getMethod.getName)
  }

  def className(clazz: Class[_]): String = {
    classNameCache.getOrElseUpdate(clazz, stripGuiceMarksFromClassName(clazz))
  }

  private def stripGuiceMarksFromClassName(clazz: Class[_]): String = {
    val name = clazz.getName
    if (name.contains("$EnhancerByGuice$")) clazz.getSuperclass.getName else name
  }
}

object Metrics {
  class Timer(timer: com.codahale.metrics.Timer) {
    def apply[T](block: => T): T = {
      val startTime = System.nanoTime()
      try {
        block
      }
      finally {
        timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
      }
    }
  }

  class Histogram(histogram: com.codahale.metrics.Histogram) {
    def update(value: Long): Unit = {
      histogram.update(value)
    }

    def update(value: Int): Unit = {
      histogram.update(value)
    }
  }

  class Meter(meter: com.codahale.metrics.Meter) {
    def mark(): Unit = {
      meter.mark()
    }

    def mark(n: Long): Unit = {
      meter.mark(n)
    }
  }
}
