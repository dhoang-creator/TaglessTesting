import TaglessTest.{ShoppingCart, UserId, UserProfile}
import TaglessFinalInterpreter.ScRepository
import cats.MonadError
import cats.data.{EitherT, State}

object TaglessFinalTestingInterpreter {

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

  type MonadThrowable[F[_]] = MonadError[F, Throwable]

  // we've imported the dependency from the 'Production Interpreter' -> will this cause a cluster fuck of the dependency injection?
    def combineFindAndCreateRecentSc[F[_] : MonadThrowable : Create : Find : FinalSc : Logging](repo: ScRepository, userId: UserId):
    F[NewRepoState] = {
      val result = for {
        oldOrders <- Create[F].create(userId)
        newOrders <- Find[F].find(userId)
      } yield (repo.from(oldOrders, newOrders))
    }

  case class Test(
                   profile: Map[UserId, UserProfile],
                   orders: Map[UserId, List[Product]]
                 )

  type Test[A] = Reader[Test, A]

  // got to this point but we need to backward engineer the TestEnv file and create a corresponding test interpreter



}
