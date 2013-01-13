
import scala.collection.mutable._
import scala.util.Random
import cc.mallet.types._

trait FV {
	def add(i: Int, v: Double)
}

trait FVWrapper {

	/** registers a sub-space */
	def register(s: String)

	/** return FV that only captures the sub-space related to s */
	def getAdder(s: String): FV

	/** return FV for all sub-spaces */
	def getFullFV(): AugmentableFeatureVector
}


/**
 * implements FVGetter using anonymous
 * classes and partial function application
 */
class Fancy extends FVWrapper {
	val alph = new Alphabet
	val sv = new AugmentableFeatureVector(alph)
	def augmentedAdd(s: String)(i: Int, v: Double) {
		// the version with string.format is about 3x slower than just string+int
		//val key = "%s:%d".format(s, i)
		val key = s + i
		sv.add(key, v)
	}
	override def register(s: String) {}	// no need to do anything
	override def getAdder(s: String): FV = {
		new FV {
			def add(i: Int, v: Double) {
				augmentedAdd(s)(i, v)
			}
		}
	}
	override def getFullFV = sv
	override def toString = "Fancy"
}

/**
 * implements FVGetter using a hashmap
 */
class Plain extends FVWrapper {
	val m = new scala.collection.mutable.HashMap[String, (Alphabet, AugmentableFeatureVector)]
	override def toString = "Plain"
	override def register(s: String) {
		if(m.contains(s))
			throw new RuntimeException("already contains this key: " + s)
		else {
			val alph = new Alphabet
			m += (s -> (alph, new AugmentableFeatureVector(alph)))
		}
	}
	override def getAdder(s: String) = {
		new FV {
			val fv = m(s)._2
			def add(i: Int, v: Double) {
				fv.add(i, v)
			}
		}
	}
	def items(fv: AugmentableFeatureVector) = {
		val idx = fv.getIndices
		val vals = fv.getValues
		(0 until fv.getNumDimensions).map(i => (idx(i), vals(i)))
	}
	override def getFullFV(): AugmentableFeatureVector = {
		val a = new Alphabet
		val full = new AugmentableFeatureVector(a)
		for((s, fv) <- m.mapValues(_._2)) {
			for((i,v) <- items(fv)) {
				val key = "%s:%d".format(s, i)
				full.add(key, v)
			}
		}
		full
	}
}

/**
 * tests the performance characteristics of
 * Fancy and Plain
 */
object App {
	
	val rand = new Random(9001)
	var n_runs = 100
	var n_adds = 1000
	val vec_dim = 99999

	val runPlain = false
	val runFancy = true

	def testFVWrapper(fvw: FVWrapper, keys: List[String]): (Double, AugmentableFeatureVector) = {
		val start = java.lang.System.currentTimeMillis
		for(c <- keys) {
			val adder = fvw.getAdder(c)
			var i = 0
			while(i < n_adds) {
				i += 1
				val k = rand.nextInt(vec_dim)
				val v = rand.nextDouble
				adder.add(k, v)
			}
		}
		val s = (java.lang.System.currentTimeMillis - start) / 1000d
		//println("took %.1f seconds".format(s))
		(s, fvw.getFullFV)
	}

	def main(args: Array[String]) {

		if(args.length > 2) {
			println("usage: [runs [n_adds]]")
			return
		}
		if(args.length > 0)
			n_runs = args(0).toInt
		if(args.length > 1)
			n_adds = args(1).toInt

		val keys = List("foo", "bar", "baz", "batmobile", "dogfood", "f1", "f2", "f3")

		println("n_runs = " + n_runs)
		println("n_adds = " + n_adds)

		var fancy_times = new ArrayBuffer[Double]
		var plain_times = new ArrayBuffer[Double]

		for(iter <- 1 to n_runs) {
			val plain = new Plain
			val fancy = new Fancy

			//println("registering keys...")
			for(k <- keys) {
				if(runPlain) plain.register(k)
				if(runFancy) fancy.register(k)
			}

			//println("testing implementations...")
			val (plain_time, plain_fv) = if(runPlain) testFVWrapper(plain, keys) else (0d, null)
			val (fancy_time, fancy_fv) = if(runFancy) testFVWrapper(fancy, keys) else (0d, null)

			plain_times += plain_time
			fancy_times += fancy_time

			if(iter % 100 == 0)
				println("iter = %d, mem = %.1f MB".format(iter, Runtime.getRuntime.totalMemory /1024d /1024d))
		}

		println("fancy: total = %.1f".format(fancy_times.sum))
		for(p <- List(0.9, 0.95, 0.99))
			println("     : %d%% = %.1f".format((p*100.0).toInt, percentile(fancy_times, 0.9)))
		println("plain: total = %.1f".format(plain_times.sum))
		for(p <- List(0.9, 0.95, 0.99))
			println("     : %d%% = %.1f".format((p*100.0).toInt, percentile(plain_times, 0.9)))
	}

	def percentile(things: Buffer[Double], p: Double) = {
		val n = (things.length * p).toInt
		things.sorted.take(n).last
	}
}




