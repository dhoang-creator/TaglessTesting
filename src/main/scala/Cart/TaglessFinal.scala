import Cart.ShoppingCart.{ShoppingCart, UserId, UserProfile}
import cats.MonadError
import cats.data.{EitherT, State}

/**
 * By splitting the Algebra/Interface into component parts, it allows us to compartmentalise the IO Testing
 */
trait Create[F[_]] {
  def create(userId: UserId): F[Unit]
}

trait Find[F[_]] {
  def find(userId: UserId): F[Option[ShoppingCart]]
}

trait FinalSc[F[_]] {
  def add(sc: ShoppingCart, product: Product): F[ShoppingCart]
}

trait Logging[F[_]] {
  def error(e: Throwable): F[Unit]
}

/**
 * Test
 */

type MonadThrowable[F[_]] = MonadError[F, Throwable]

case class ShoppingCarts(
                        profile: Map[UserId, UserProfile],
                        orders: Map[UserId, List[Product]]
                        )

// the top typelevel is just a simplistic way of understanding the DB interaction
type ScRepository = Map[String, ShoppingCart]
// the below typelevel had to be altered to ensure that
type ScRepoState[A] = EitherT[State[ScRepository, ?], Throwable, A]



/**
 * Smart Constructor Pattern
 */
class ShoppingCartsInterpreter (repo: ScRepository) {
    def combineFindAndCreateRecentSc[F[_] : MonadThrowable : Create : Find : FinalSc](userId: UserId):
      F[ScInformation] = {
        val result = for {
          oldOrders <- Create[F].create(userId)
          newOrders <- Find[F].find(userId)
        } yield ScInformation.from(oldOrders, newOrders)

        result.onError {
          case e => Logging[F].error(e)
    }
  }
}

// Note that the Smart Constructor Pattern utilises companion objects & factory make methods
object ShoppingCartsInterpreter {
  def make(): ShoppingCartsInterpreter = {
    new ShoppingCartsInterpreter(repository)
  }
  private val repository: ScRepository = Map(
  // TODO )
}

/**
 * Implicit Object Resolution
 */
// implicit object TestShoppingCartInterpreter extends ShoppingCart[ScRepoState] {
//  override def create(id: String): ScRepoState[Unit] = {
//    State.modify { carts =>
//      val shoppingCart = ShoppingCart(id, List())
//      carts + (id -> shoppingCart)
//    }
//    override def find(id: String): ScRepoState[Option[ShoppingCart]] = {
//      State.inspect { carts =>
//        carts.get(id)
//      }
//      override def add(sc: ShoppingCart, product: Product): ScRepoState[ShoppingCart] =
//        State {
//          carts =>
//            val products = sc.products
//            val updatedCart = sc.copy(products = product :: products)
//            (carts + (sc.id -> updatedCart), updatedCart)
//        }
//    }
//  }
//}

/**
 * Program
 */
object Program {

  import cats._

  def createAndAddToCart[F[_]: Monad](product: Product, cartId: String)
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
