package examples.tailchain.modifiers

import com.google.common.primitives.Longs
import examples.commons.SimpleBoxTransaction
import examples.tailchain.core.Constants._
import examples.tailchain.core.{Constants, Ticket, TicketSerializer}
import io.circe.Json
import scorex.core.NodeViewModifier.{ModifierId, ModifierTypeId}
import scorex.core.serialization.Serializer

import scala.util.Try

//TODO compact proof of ticket in header
case class BlockHeader(override val parentId: ModifierId,
                       stateRoot: StateRoot,
                       txRoot: TransactionsRoot,
                       ticket: Ticket,
                       powNonce: Long
                      ) extends TModifier {
  override type M = BlockHeader

  // with Dotty is would be Seq[TX] | Nothing
  override def transactions: Option[Seq[SimpleBoxTransaction]] = None

  override val modifierTypeId: ModifierTypeId = TModifier.Header

  //todo: for Dmitry: implement as hash of the header
  override lazy val id: ModifierId = ???

  //todo: for Dmitry: implement
  override def json: Json = ???

  override lazy val serializer = BlockHeaderSerializer
}

object BlockHeaderSerializer extends Serializer[BlockHeader] {
  private val ds = Constants.hashfn.DigestSize

  override def toBytes(obj: BlockHeader): Array[ModifierTypeId] = obj.parentId ++ obj.stateRoot ++ obj.txRoot ++
    Longs.toByteArray(obj.powNonce) ++ TicketSerializer.toBytes(obj.ticket)


  override def parseBytes(bytes: Array[ModifierTypeId]): Try[BlockHeader] = Try {
    val parentId = bytes.slice(0, ds)
    val stateRoot = bytes.slice(ds, 2 * ds)
    val txRoot = bytes.slice(2 * ds, 3 * ds)
    val powNonce = Longs.fromByteArray(bytes.slice(3 * ds, 3 * ds + 8))
    val ticket = TicketSerializer.parseBytes(bytes.slice(3 * ds + 8, bytes.length)).get
    BlockHeader(parentId, stateRoot, txRoot, ticket, powNonce)
  }
}
