// here you'll be mocking the testing environ by mimicking the traits of the higher kinded types from the base file
object TestEnv {

  trait Users[F[_]] {
    def profileFor(userId: UserId): F[UserProfile]
  }

  trait Orders[F[_]] {
    def ordersFor(userId: UserId): F[List[Order]]
  }

  trait Logging[F[_]] {
    def error(e: Throwable): F[Unit]
  }

  imports cats.implicits._

  type MonadThrowable[F[_]] = MonadError[F, Throwable]

  def fetchUserInformation[F[_]: MonadThrowable : Users : Orders : Logging](userId: UserId):
    F[UserInformation] = {
    val result = for {
      profile <- Users[F].profileFor(userId)
      orders <- Orders[F].ordersFor(userId)
    } yield UserInformation.from(profile, orders)

    result.onError {
      case e => Logging[F].error(e)
    }
  }

}
