package Integrator

class TaglessService[M[_]: Monad](taglessRepository: TaglessRepository[M] ) {
  def getList(limit: Int): M[Seq[Item]] = {
    taglessRepository.getList(limit)
  }
}
