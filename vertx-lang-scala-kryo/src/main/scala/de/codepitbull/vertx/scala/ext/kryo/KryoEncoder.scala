package de.codepitbull.vertx.scala.ext.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator

/**
  * Makes Kryo-encoding magically Thread-safe.
  * @param instantiator the instantiator used to create new Kryo i9sntances
  * @param registeredClasses if None the Kryo instance will de/serialize any class, otherwise only the ones provided
  */
class KryoEncoder(instantiator: ScalaKryoInstantiator, registeredClasses: Option[List[Class[_]]] = None) {
  private val tl = new ThreadLocal[Tuple3[Kryo, Input, Output]] {
    override def initialValue(): Tuple3[Kryo, Input, Output] = {
      val kryo = instantiator.newKryo()
      registeredClasses match {
        case Some(l) =>
          kryo.setRegistrationRequired(true)
          l.foreach(c => kryo.register(c))
        case None =>
      }
      val output = new Output(new ByteArrayOutputStream)
      val input = new Input()
      (kryo, input, output)
    }
  }

  def decodeFromBytes(bytes: Array[Byte]): Object = {
    val k = tl.get()
    k._2.setBuffer(bytes)
    k._1.readClassAndObject(k._2)
  }

  def encodeToBytes(s: Object): Array[Byte] = {
    val k = tl.get()
    k._1.writeClassAndObject(k._3, s)
    val ret = k._3.toBytes
    k._3.clear()
    ret
  }

}

object KryoEncoder {
  val instantiator: ScalaKryoInstantiator = new ScalaKryoInstantiator()
  def apply(registeredClasses: Option[List[Class[_]]] = None): KryoEncoder =
    new KryoEncoder(instantiator, registeredClasses)
}