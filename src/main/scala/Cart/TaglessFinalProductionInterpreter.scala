import Cart.ShoppingCart.{ShoppingCart, UserId, UserProfile}
import cats.MonadError
import cats.data.{EitherT, State}


object TaglessFinalProductionInterpreter {

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
type ScRepository = Map[UserId, ShoppingCart]
// here, we're trying to encapsulate the data that is being created from this application and the typelevel directly below that is to ensure that we're testing both if state is present or not
type ScRepoState[A] = State[ScRepository, A]
// we have yet to use the errorLogging to check the EitherT state of the ShoppingCart
type ScThrowState[A] = EitherT[ScRepoState, Throwable, A]



/**
 * Smart Constructor Pattern
 */

// the below implementation takes the above ScRepository and converts it into a state of ScRepoState which can be used later in the testing
class ShoppingCartsInterpreter (repo: ScRepository)(userId: UserId) {
    def combineFindAndCreateRecentSc[F[_] : MonadThrowable : Create : Find : FinalSc : Logging](repo: ScRepository, userId: UserId):
      F[ScRepoState] = {
        val result = for {
          oldOrders <- Create[F].create(userId)
          newOrders <- Find[F].find(userId)
          // how do we wrap the yielded result into a state? -> do we simply not refer to this method in tje future and then we can wrap it into a state
        } yield (repo.from(oldOrders, newOrders))

        val e = result.onError {
          case e => Logging[F].error(e)
    }
  }
}

object ShoppingCartsInterpreter {
  // surely by making a for comprehension, you are attempting to map and then flatmap but remember that the result can also be a map
  private val repository = for {
    // we need to return a Map of a corresponding UserId to a ShoppingCart
    // am I just being an idiot here, isn't the whole purpose of a 'for comprehension' to have both methods map over the same data rather than providing two data sets?
    _ <- ShoppingCart.findId(userId)
    _ <- ShoppingCart.findSc(userId)
  } yield ()

  def make(): ShoppingCartsInterpreter = {
    new ShoppingCartsInterpreter(repository)
  }
}

/**
 *  NOTE:
 *  This is the production interpreter and not the test interpreter!
 *  So we refer to the EitherT typelevel in a later part of the application
 */


/**
 * Program
 */

// we need to break down the program here
object Program {

  import cats._

  // this looks like the correct steps for returning -> trying to differentiate between the Sc object and the corresponding data type but this needs to be honed in on at some point
  def createAndAddToCart[F[_]: Monad](product: Product, cartId: Int)
                                     (implicit shoppingCart: ShoppingCarts):
       F[Option[ShoppingCarts]] =
      for {
        _ <- ShoppingCart.create(cartId)
        maybeSc <- ShoppingCart.find(cartId)
        maybeNewSc <- maybeSc.traverse(sc => ShoppingCart.add(sc, product))
      } yield maybeNewSc
}

  // would there by any confusion between the ShoppingCart object and the generic data type object that we're earmarking below?
object ShoppingCarts[A] {
  def apply[F[_]](implicit sc: ShoppingCart[F]): ShoppingCart[F] = sc
}

// note that you can in fact create an object which corresponds to a case class
case class Program[F[_] : Monad](carts: ShoppingCarts[F]) {
  override def createAndAddToCart(product: Product, cartId: String): F[Option[ShoppingCart]] = {
    _ <- carts.create(cartId)
    maybeSc <- carts.find(cartId)
    maybeNewSc <- maybeSc.traverse(sc => carts.add(sc, product))
  } yield maybeNewSc
}

// the below should evidently be the final step of the application program
val program: ProgramWithDep[ScRepoState] = ProgramWithDep {
  ShoppingCartWithDependencyInterpreter.make()
}
program.createAndToCart(Product("id", "a product"), "cart1")

}
