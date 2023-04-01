import TaglessFinalInterpreter.ShoppingCartsInterpreter
import Repository.{ShoppingCart, UserId, UserProfile}
import cats.MonadError
import cats.data.{EitherT, State}

// are we not simply gravitating towards a MVC Centric Application?

// should we contain the traits and the types inside an overall object -> this needs to be looked up
object TaglessFinalInterpreter extends RepoModelTraits {

/**
 * Production Interpreter
 */

// why do we have a Map[K, V] for the below format when we already have a ShoppingCart model in the RepoModels?
case class ShoppingCarts(
                        // let's keep it simple with just an 'orders' Map[K,V]
                        orders: Map[UserId, List[Product]]
                        )

type ShoppingCartRepository = Map[UserId, ShoppingCarts]

type RepositoryState[A] = State[ShoppingCartRepository, A]

// Monadic Either Alias
type RepoThrowableState[A] = EitherT[RepositoryState, Throwable, A]

  // do this and the above not simply conflict? ->
  type MonadThrowable[F[_]] = MonadError[F, Throwable]


/**
 * Smart Constructor Pattern
 */

// the below implementation takes the above ScRepository and converts it into a state of ScRepoState which can be used later in the testing
class ShoppingCartsInterpreter(repo: ShoppingCartRepository)(userId: UserId) {
    def combineFindAndCreateRecentSc[F[_] : MonadThrowable : Create : FindUserId : FinalShoppingCartState : ErrorLogging](repo: ShoppingCartRepository, userId: UserId):
      // we're returning a generic of the RepoState which also contains a generic -> which generic should we be wrapping?
      F[RepositoryState[A]] = {
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