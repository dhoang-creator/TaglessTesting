package Integrator

class TaglessService[M[_]: Monad](taglessRepository: TaglessRepository[M] ) {
  def getList(limit: Int): M[Seq[Item]] = {
    taglessRepository.getList(limit)
  }
}

// User Subscription and Email Preference Service
trait Emails[F[_]] {
  def save(email: Email): F[Either[EmailAlreadyExists.type, Email]]
  def known(email: Email): F[Boolean]
  def findEmail(email: Email): F[Option[Email]]
}
trait Users [F[_]] {
  def createUser(primaryEmail: Email,
                 userProfile: UserProfile = UserProfile()): F[PersistedUser]
  def findUser(uid: UID): F[Option[PersistedUser]]
  def identifyUser(email: Email): F[Option[PersistedUser]]
  def attachEmail(user: PersistedUser, email: Email): F[Int]
  final def attachEmails(user: PersistedUser, emails: Email*)(
                        implicit F: Applicative[F]): F[Int] = {
    import cats.instances.list._
    val fa = Traverse[List].sequence(emails.map(attachEmail(user, _)).toList)
    F.map(fa)(_.sum)
  }
  def getEmails(uid: UID): F[Option[NonEmptyList[Email]]]
  def updateUserProfile(uid: UID, f: UserProfile => UserProfile): F[Option[PersistedUser]]
}

final class EmailRepository(implicit ec: ExecutionContext) extends Emails[DBIO] {
  override def save(email: Email) = {
    val row = EmailRow.from(email)
    (EmailsTable += row)
      .map(_ => Right(email): Either[EmailAlreadyExists.type, Email])
      .recoverPSQLException {
        case UniqueViolation("emails_pkey", _) => Left(EmailAlreadyExists)
      }
  }
  override def known(email: Email) = existsQuery(email).result
  override def findEmail(email: Email) =
    filterEmailQuery(email).result.headOption
  //...
}
class UserRepository(emailRepository: EmailRepository)(
                    implicit ec: ExecutionContext) extends Users[DBIO] {
  //...
  override def createUser(primaryEmail: Email, userProfile: UserProfile) = {
    val row = DbUser.from(primaryEmail, userProfile)
    (UsersTable += row).map(PersistedUser(_, row))
  }
  override def identifyUser(email: Email) = identifyQuery(email).result.flatMap{
    case Seq()          => DBIO.successful(None)
    case Seq(singleRow) => DBIO.successful(Some(singleRow))
    case _              => DBIO.failed(
      new IllegalStateException(s"More than one user uses email: $email"))
  }
  override def attachEmail(user: PersistedUser, email: Email) = {
    val id = user.id
    emailRepository.upsert(email, id)
  }
  //...
}

// Below is a fixture from what would be Typical Integration Test
class BaseFixture(db: Database) {
  private case class IntentionalRollbackException[R](result: R)
    extends Exception("Rolling back transaction ater text")
  def withRollback[A](testCode: => DBIO[A])(
                     implicit ec: ExecutionContext): Future[A] = {
    val testWithRollback = testCode flatMap (a =>
      DBIO.failed(IntentionalRollbackException(a)))
    val testResult = db.run(testWithRollback.transactionally)
    testResult.recover {
      case IntentionalRollbackException(success) => success.asInstanceOf[A]
    }
  }
}
class UserFixtures(db: Database) extends BaseFixture(db) {
  //...
  def mkUser(primaryEmail: String,
             userProfile: UserProfile = UserProfile()): User =
    User.from(Email(primaryEmail), userProfile)
  def mkUser(primaryEmail: String,
             emails: NonEmptyList[Email],
             userProfile: UserProfile): UserWithEmails =
    UserWithEmails(mkUser(primaryEmail, userProfile), emails)
  def withUser[A](user: User)(testCode: UID => DBIO[A])(
                 implicit ec: ExecutionContext): Future[A] =
    withRollback(userRepository.insert(user).flatMap(testCode))
  def withUser[A](primaryEmail: String,
                  userProfile: UserProfile = UserProfile())(
    testCode: UID => DBIO[A])(
    implicit ec: ExecutionContext): Future[A] = withUser(mkUser(primaryEmail, userProfile))(testCode)
  def withUsers[A](users: User*)(testCode: Seq[UID] => DBIO[A])(
                  implicit ec: ExecutionContext): Future[A] =
    withRollback(DBIO
                  .sequence(users.map(user => usersRepository.insert(user)))
                  .flatMap(testCode))
  //...
}

// ended at the class UserRepository