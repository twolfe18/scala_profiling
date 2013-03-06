
import scala.util.Random

object Bar {
	
	val seed = 10

	def compiledFoo(s: String): String = {
		// burrows-wheeler
		val starts = (0 until s.length).toSeq
		val rotations = starts.map(i => s.substring(i, s.length) + s.substring(i))
		rotations.sorted.map(_.charAt(0)).mkString
	}

	def randomString(d: Int): String = (0 until d).map(i => Random.alphanumeric(seed)).mkString

	def benchmark(f: String => String, n: Int = 150000, d: Int = 100) {
		val strings = (0 until n).map(i => randomString(d))
		val start = System.currentTimeMillis
		val mapped = strings.map(f)
		println("%d strings of length %d took %.1f seconds".format(n, d, (System.currentTimeMillis - start)/1000d))
	}

	def main(args: Array[String]) {
		println("starting benchmark...")
		benchmark(compiledFoo)
	}

}

