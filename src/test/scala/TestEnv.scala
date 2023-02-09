object TestEnv {

  // these are the three algebras which we'll be using later
  trait Users[F[_]] {
    def profileFor(userId: UserId): F[UserProfile]
  }

  trait Orders[F[_]] {
    def ordersFor(userId: UserId): F[List[Order]]
  }

  trait Logging[F[_]] {
    def error(e: Throwable): F[Unit]
  }

  // below is the implementation of the above algebras
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

  /**
   * Test
   */
  // since one of the effects deals with reading some external data, it would make sense to use the Reader monad
  import cats.data._

  case class TestEnv(
                    profiles: Map[UserId, UserProfile],
                    orders: Map[UserId, List[Order]])
  type Test[A] = Reader[TestEnv, A]

  // we need to log the state of the data that is being read and the State monad can be used here
  case class TestEnv(
                    profiles: Map[UserId, UserProfile],
                    orders: Map[UserId, List[Order]],
                    loggedErrors: List[Throwable]) {
    def withProfile(profile: UserProfile): TestEnv =
      copy(profiles = profiles + (profile.userId -> profile))
    def withOrder(order: Order): TestEnv = {
      val updatedUserOrders = order :: userOrders(order.userId)
      copy(orders = orders + (order.userId -> updatedUserOrders))
    }
    def logError(e: Throwable): TestEnv =
      copy(loggedErrors = e :: loggedErrors)
    def userOrders(userId: UserId): List[Order] =
      orders.getOrElse(userId, Nil)
  }
  object TestEnv {
    final val Empty = TestEnv(Map.empty, Map.empty, Nil)
  }
//  type Test[A] = State[TestEnv, A]
  // the above needs to be transformed into an EitherT since the above implementation would only really work if a success is made and won't return anything if a Failure is returned
  type Test[A] = EitherT[State[TestEnv, ?], Throwable, A]

  // the below implicit vals are simply the corresponding interpreters for each of the above algebras and implementations
  // we have to restructure the implementations given the above alteration to the type of Test[A]
  implicit val usersTest: User[Test] = new Users[Test] {
    override def profileFor(userId: UserId): Test[UserProfile] = {
      EitherT {
        State.inspect { env =>
          env.profiles.get(userId) match {
            case Some(profile) => Right(profile)
            case None => Left(UserNotFound(userId))
          }
        }
      }
    }
  }
  implicit val ordersTest: Orders[Test] = new Orders[Test] {
    override def ordersFor(userId: UserId): Test[List[Order]] =
      EitherT.liftF(State.inspect(_.userOrders(userId)))
  }
  implicit val loggingTest: Logging[Test] = new Logging[Test] {
    override def error(e : Throwable): Test[Unit] =
      EitherT(State(env => (env.logError(e), Right())))
  }

  // Since cats doesn't handle errors all too well, we can thus test the effectual State
  // using the StateT alongside the Either data type
  type EitherThrowableOr[A] = Either[Throwable, A]
  type Test[A] = State[EitherThrowableOr, TestEnv, A]

  // Or with kind-project compiler plugin:
  // type Test[A] = State[Either[Throwable, ?] TestEnv, A]

  case class UserNotFound(userId: UserId) extends RuntimeException(s"User with ID $userId does not exist") {

    implicit val usersTest: Users[Test] = new Users[Test] {
      override def profileFor(userId: UserId): Test[UserProfile] =
        StateT.inspectF { env =>
          case Some(profile) => Right(profile)
          case None => Left(UserNotFound(userId))
        }
    }
  }

  // We can finally apply some tests via the specs2 framework
  import org.specs2.mutable.Specification

  class UserInformationSpec extends Specification {
    "fetch user name and orders by ID" in {
      val userId = UserId("user-1234")
      val env = TestEnv.Empty
        .withProfile(UserProfile(userId, "John Doe"))
        .withOrder(Order(userId, OrderId("order-1")))
        .withOrder(Order(userId, OrderId("order-2")))

      val result = fetchUserInformation[Test](userId)

      result.runA(env) must beRight(
        haveUserName("John Doe") and
          haveOrders(OrderId("order-1"), OrderId("order-2"))
      )
    }
  }
}
