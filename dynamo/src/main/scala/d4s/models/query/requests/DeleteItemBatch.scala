package d4s.models.query.requests

import d4s.codecs.circe.DynamoEncoder
import izumi.fundamentals.platform.functional.Identity
import d4s.models.query.DynamoRequest.{DynamoWriteBatchRequest, WithBatch, WithTableReference}
import d4s.models.table.TableReference
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, BatchWriteItemRequest, DeleteRequest, WriteRequest}

import scala.jdk.CollectionConverters._

final case class DeleteItemBatch(
  table: TableReference,
  batchItems: List[Map[String, AttributeValue]] = Nil
) extends DynamoWriteBatchRequest
  with WithTableReference[DeleteItemBatch]
  with WithBatch[DeleteItemBatch, Identity] {

  override def withBatch[Item: DynamoEncoder](batchItems: List[Item]): DeleteItemBatch = {
    withBatch(batchItems.map(DynamoEncoder[Item].encode))
  }

  override def withBatch(batchItems: List[Map[String, AttributeValue]]): DeleteItemBatch = copy(batchItems = batchItems)

  override def withTableReference(t: TableReference => TableReference): DeleteItemBatch = copy(table = t(table))

  override def toAmz: BatchWriteItemRequest = {
    val items = batchItems.map(ll => WriteRequest.builder().deleteRequest(DeleteRequest.builder().key(ll.asJava).build()).build())
    BatchWriteItemRequest
      .builder()
      .requestItems(Map(table.fullName -> items.asJava).asJava)
      .build()
  }
}