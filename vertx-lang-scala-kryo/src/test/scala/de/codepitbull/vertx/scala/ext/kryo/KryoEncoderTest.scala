package de.codepitbull.vertx.scala.ext.kryo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{AsyncFlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KryoEncoderTest extends AsyncFlatSpec with Matchers {

  "A case class" should "be (de)serializable using the raw decoder" in {
    val test = ACaseClass("12", Some(1))
    val codec = KryoEncoder()
    val encoded = codec.encodeToBytes(test)
    val decoded = codec.decodeFromBytes(encoded)
    test should equal(decoded)
  }

  "If classes are specified in the constructor then only registered classes" should "be (de)serializable using the raw decoder" in {
    val test = ACaseClass("12", Some(1))
    val codec = KryoEncoder(Some(List(classOf[ACaseClass])))
    val encoded = codec.encodeToBytes(test)
    val decoded = codec.decodeFromBytes(encoded)
    test should equal(decoded)
    val nonRegistered = AnotherCaseClass("12", Some(1))
    assertThrows[IllegalArgumentException] {
      codec.encodeToBytes(nonRegistered)
    }
  }

}




