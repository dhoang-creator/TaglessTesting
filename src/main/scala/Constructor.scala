/**
 * Smart Constructor
 */
class ShoppingCartsInterpreter private(repo: ShoppingCartRepository)
  extends ShoppingCarts[ScRepoState] {
  // Functions implementation
}

object ShoppingCartsInterpreter {
  def make(): ShoppingCartsInterpreter = {
    new ShoppingCartsInterpreter(repository)
  }
  private val repository: ShoppingCartRepository = Map()
}

case class ProgramWithDep[F[_] : Monad](carts: ShoppingCarts[F]) {
  def createAndToCart(product: Product, cartId: String): F[Option[ShoppingCart]] = {
    _ <- carts.create(cartId)
    maybeSc <- carts.find(cartId)
    maybeNewSc <- maybeSc.traverse(sc => carts.add(sc, product))
  } yield maybeNewSc
}

val program: ProgramWithDep[ScRepoState] = ProgramWithDep {
  ShoppingCartWithDependencyInterpreter.make()
}
program.createAndToCart(Product("id", "a product"), "cart1")