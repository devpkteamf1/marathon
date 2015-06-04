package mesosphere.util.state

import scala.concurrent.Future

/**
  * An Entity that is provided and can be changed by the related store.
  */
trait PersistentEntity {

  /**
    * The identifier of this entity.
    */
  def id: String

  /**
    * Get the content bytes of this entity.
    */
  def bytes: Array[Byte]

  /**
    * Mutate this entity with given bytes.
    */
  def mutate(bytes: Array[Byte]): PersistentEntity

}

/**
  * Store abstraction for different store implementations.
  */
trait PersistentStore {

  type ID = String

  /**
    * Fetch entity with given key identifier. If the item does not exists, None is returned.
    * @param key the identifier of this id
    * @return either the entity or None.
    *         In case of a storage specific failure, a StoreCommandFailedException is thrown.
    */
  def load(key: ID): Future[Option[PersistentEntity]]

  /**
    * Create a new entity with given id and content.
    * This will result in a StoreCommandFailedException if an entity with this key already exists.
    * @param key the identifier of this entity.
    * @param content the content of this entity.
    * @throws mesosphere.marathon.StoreCommandFailedException
    *         in the case of an existing entity or underlying store problems.
    */
  def create(key: ID, content: Array[Byte]): Future[PersistentEntity]

  /**
    * Store the given entity.
    * Since the update relates to a given PersistentEntity, the underlying storage might
    * check the read version to prevent concurrent modifications.
    * @param entity the entity to store
    * @return either the entity or a failure.
    *         In case of a storage specific failure, a StoreCommandFailedException is thrown.
    * @throws mesosphere.marathon.StoreCommandFailedException
    *         in the case of concurrent modifications or underlying store problems.
    */
  def update(entity: PersistentEntity): Future[PersistentEntity]

  /**
    * Expunge the entity with given id.
    * @param key the key identifier
    * @return true if the entity was deleted and false if not existent.
    *         In case of a storage specific failure, a StoreCommandFailedException is thrown.
    * @throws mesosphere.marathon.StoreCommandFailedException
    *         if the item could not get deleted or underlying store problems.
    */
  def delete(key: ID): Future[Boolean]

  /**
    * List all available identifier.
    * @return the list of available identifier.
    */
  def allIds(): Future[Seq[ID]]
}

