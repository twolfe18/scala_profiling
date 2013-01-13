
// I'm fairly sure this shows that there is no memory
// leak in the scala implementation of partial function application

import scala.collection.mutable._
import scala.util.Random

trait Setter { def set(k: Int, v: Double) }

class PartialFunctionApplier {
	val m = new HashMap[String, Map[Int, Double]]
	def register(s: String) {
		m += (s -> new HashMap[Int, Double])
	}
	def partialApply(s: String): Setter = {
		new Setter {
			val m_inner = m(s)
			def set(k: Int, v: Double) {
				m_inner.put(k, v)
			}
		}
	}
}

object App {

	val rand = new Random(9001)
	var n_runs = 5000
	var n_adds = 1000

	def main(args: Array[String]) {
		if(args.length > 2) {
			println("usage: App [n_runs [n_adds]]")
			return
		}
		if(args.length > 0)
			n_runs = args(0).toInt
		if(args.length > 1)
			n_adds = args(1).toInt
		for(iter <- 1 to n_runs) {
			run
			if(iter % 250 == 0)
				println("iter = %d, mem = %.1f MB".format(iter, Runtime.getRuntime.totalMemory /1024d /1024d))
		}
	}

	def run() {
		val p = new PartialFunctionApplier
		val keys = List("foo", "bar", "baz")
		for(k <- keys) {
			p.register(k)
			val setter = p.partialApply(k)
			var iter = 0
			while(iter < n_adds) {
				iter += 1
				val i = rand.nextInt(99999)
				val d = rand.nextDouble
				setter.set(i, d)
			}
		}
	}

}

