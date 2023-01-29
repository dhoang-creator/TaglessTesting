/**
 * The below is simply the Algebra/Interfaces and Interpreter/Implementation of the WebShop program
 */

trait ShoppingCarts[F[_]] {
  def create(id: String): F[Unit]
  def find(id: String): F[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): F[ShoppingCart]
}

type ShoppingCartRepository = Map[String, ShoppingCart]
type ScRepoState[A] = State[ShoppingCartRepository, A]

implicit object TestShoppingCartInterpreter extends ShoppingCart[ScRepoState] {
  override def create(id: String): ScRepoState[Unit] = {
    State.modify { carts =>
      val shoppingCart = ShoppingCart(id, List())
      carts + (id -> shoppingCart)
    }
    override def find(id: String): ScRepoState[Option[ShoppingCart]] = {
      State.inspect { carts =>
        carts.get(id)
      }
      override def add(sc: ShoppingCart, product: Product): ScRepoState[ShoppingCart] =
        State {
          carts =>
            val products = sc.products
            val updatedCart = sc.copy(products = product :: products)
            (carts + (sc.id -> updatedCart), updatedCart)
        }
    }
  }
}

object Program {

  def createAndAddToCart[F[_]: Monad](productL Product, cartId: String)
  (implicit shoppingCarts: ShoppingCarts[F]): F[Option[ShoppingCart]] =
    for {
      _ <- shoppingCarts.create(cartId)
      maybeSc <- shoppingCarts.find(cartId)
      maybeNewSc <- maybeSc.traverse(sc => shoppingCarts.add(sc, product))
    } yield maybeNewSc

  def createAndToCart[F[_]: Monad : ShoppingCarts](product: Product, cartId: String):
  F[Option[ShoppingCart]] =
    for {
      _ <- ShoppingCarts[F].create(cartId)
      maybeSc <- ShoppingCarts[F].find(cartId)
      maybeNewSc <- maybeSc.traverse(sc => ShoppingCarts[F].add(sc, product))
    } yield maybeNewSc
}

object ShoppingCarts {
  def apply[F[_]](implicit sc: ShoppingCarts[F]): ShoppingCarts[F] = sc
}

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
