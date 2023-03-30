import TaglessFinalInterpreter.ShoppingCartsInterpreter
import TaglessTest.{ShoppingCart, UserId, UserProfile}
import cats.MonadError
import cats.data.{EitherT, State}


object TaglessFinalInterpreter {

/**
 * By splitting the Algebra/Interface into component parts, it allows us to compartmentalise the IO Testing
 */
trait Create[F[_]] {
  def create(userId: UserId): F[Unit]
}

trait FindId[F[_]] {
  def find(userId: Option[UserId]): F[Option[ShoppingCart]]
}

trait FinalSc[F[_]] {
    def findSc(userId: Option[UserId]): F[Option[ShoppingCart]]
    def add(sc: ShoppingCart, product: Product): F[ShoppingCart]
}

trait Logging[F[_]] {
  def error(e: Throwable): F[Unit]
}

/**
 * Production Interpreter
 */

type MonadThrowable[F[_]] = MonadError[F, Throwable]

case class ShoppingCarts(
                        // we don't make use of the UserProfile below
                        profile: Map[UserId, UserProfile],
                        orders: Map[UserId, List[Product]]
                        )

// shouldn't this make use of the above case class 'ShoppingCarts' but then we have a Map within a map and this will need to be flat mapped?
type ScRepository = Map[UserId, ShoppingCart]

type ScRepoState[A] = State[ScRepository, A]

// Monadic Either Alias
type ScThrowState[A] = EitherT[ScRepoState, Throwable, A]

  
/**
 * Smart Constructor Pattern
 */

// the below implementation takes the above ScRepository and converts it into a state of ScRepoState which can be used later in the testing
class ShoppingCartsInterpreter (repo: ScRepository)(userId: UserId) {
    def combineFindAndCreateRecentSc[F[_] : MonadThrowable : Create : FindId : FinalSc : Logging](repo: ScRepository, userId: UserId):
      // we're returning a generic of the RepoState which also contains a generic -> which generic should we be wrapping?
      F[ScRepoState[A]] = {
        val orders = match A {
          // how do we return the above Monadic Either Alias
          case e => Logging[F].error(e)
          case _ => val repo = for {
            // the below should demonstrate a Map and FlatMap rather than two independent functions
            oldOrders <- FindSc[F].findSc(Option[UserId])
            newOrders <- Create[F].create(userId)
            // how do we wrap the yielded result into a state? -> do we simply not refer to this method in tje future and then we can wrap it into a state
          } yield (UserId, (oldOrders ++ newOrders))
        }

      // need to restructure the below since it's bad practice to use a 'return' statement
      if (e)
        return Throwable
      else
        return result
    }

  // I think we're overcomplicating things with a CartId on top of the Shopping Cart, rather than have a value which points to the products within a cart, you should simply return the cart
  def findCartId(userId: UserId): Option[Map[UserId, A]] = match UserId {
    case UserId =>
    }
  }
}

// where is the magic done? In the object or the class?
object ShoppingCartsInterpreter {
  // surely by making a for comprehension, you are attempting to map and then flatmap but remember that the result can also be a map
  private val repository = for {
    // we need to return a Map of a corresponding UserId to a ShoppingCart
    // am I just being an idiot here, isn't the whole purpose of a 'for comprehension' to have both methods map over the same data rather than providing two data sets?
    _ <- ShoppingCart.findId(userId)
    _ <- ShoppingCart.findSc(userId)
  } yield ()

  // the below Factory Method needs to either make an empty or filled ShoppingCartsInterpreter
  def make(ScRepository, UserId): ShoppingCartsInterpreter = {
    new ShoppingCartsInterpreter(repository)
  }
}

// do we even need the below ShoppingCarts[A]?

//object ShoppingCarts[A] {
//  def apply[F[_]](implicit sc: ShoppingCart[F]): ShoppingCart[F] = sc
//}