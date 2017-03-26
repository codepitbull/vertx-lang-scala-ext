package de.codepitbull.vertx.scala.ext.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.{MessageCodec, EventBus => JEventBus}
import io.vertx.scala.core.eventbus.EventBus

/**
  * A codec implementation to allow case classes to be transfered via the [[io.vertx.scala.core.eventbus.EventBus]].
  * It uses Twitter Chill under the hood so all Scala-types can be used.
  */
class KryoMessageCodec(encoder: KryoEncoder) extends MessageCodec[Object, Object] {

  private val output = new Output(new ByteArrayOutputStream)
  private val input = new Input()

  def register(eventBus: EventBus): KryoMessageCodec = {
    eventBus.asJava.asInstanceOf[JEventBus].registerCodec(this)
    this
  }

  override def transform(s: Object): Object = s

  override def decodeFromWire(pos: Int, buffer: Buffer): Object = {
    encoder.decodeFromBytes(buffer.getBytes(pos, buffer.length()))
  }

  override def name(): String = CodecName

  override def encodeToWire(buffer: Buffer, s: Object): Unit = {
    buffer.appendBytes(encoder.encodeToBytes(s))
  }

  override def systemCodecID(): Byte = -1

  /**
    * Checks if the given class is a case-class.
    *
    * @param v the Class to check
    * @return true if the given class is a case class
    */
  def isCaseClass(v: Class[_]): Boolean = {
    import reflect.runtime.universe._
    runtimeMirror(v.getClass.getClassLoader).classSymbol(v).isCaseClass
  }

}

object KryoMessageCodec {
  val CodecName = "k"

  def apply(encoder: KryoEncoder): KryoMessageCodec = new KryoMessageCodec(encoder)
  def apply(): KryoMessageCodec = new KryoMessageCodec(KryoEncoder())
}

