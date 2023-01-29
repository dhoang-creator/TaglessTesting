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

class UserRepositorySpecs extends ItTest with OptionValues {
  val repository = new UserRepository(new EmailRepository())
  val fixture = new UserFixtures(db)
  import fixture._
  it should "insert user to database" in withRollback {
    repository.createUser(
      Email("john@example.com"),
      UserProfile(
        name = Some(Name("John")),
        aboutMe = Some("I am John"),
        birthdate = Some(Birthdate(LocalDate.of(1982, 11, 12))),
        languagesSpoken = Some("Polish, English, German, Russian"),
        language = Some(Language(new Locale("pl", "PL")))
      )
    )
  }.map { fromDb =>
    fromDb.primaryEmail shouldEqual "john@example.com"
    val profile = fromDb.profile
    profile.name.value shouldEqual "John"
    profile.aboutMe.value shouldEqual "I am John"
    profile.birthdate.value shouldEqual LocalDate.of(1982, 11, 12)
    profile.languagesSpoken.value shouldEqual "Polish, English, German, Russian"
    profile.language.value shouldEqual new Locale("pl, PL")
  }
  it should "identity user by email" in withUser(
    mkUser("john@example.com",
      userProfile = UserProfile(aboutMe = Some("I am John"),
        language = Some(Language(Locale.US))))(_ =>
    repository.identifyUser(Email("john@example.com"))).map {
      case (maybeJohn, noOne) =>
        noOne shouldBe empty
        val john = maybeJohn.value
        john.profile.aboutMe shouldEqual Some("I am John")
        john.profile.language shouldEqual Some(Locale.US)
        john.primaryEmail shouldEqual "john@example.com"
    }
    it should "get users by uid" in withUsers(
      mkUser("user1@example.com"),
      mkUser("user2@example.com")
  ) {
    case Seq(uid1, uid2) =>
      repository.getUser(UID()) zip repository.
        getUser(uid1) zip repository.
        getUser(uid2)
  }.map {
    case ((noUser, user1), user2) =>
      noUser shouldBe empty
      user1.value.primaryEmail shouldEqual "user1@example.com"
      user2.value.primaryEmail shouldEqual "user2@example.com"
  }
  class EmailRepositorySpecs extends ItTest
                              with EitherValues
                              with OptionValues {
    val repository = new EmailIdentityRepository
    val fixture = new EmailFixtures(db)
    import fixture._
    it should "save email to the db" in withRollback {
      repository.save(mkEmail("wannabe.user@example.com"))
    }.map { errorOrEmail =>
      val fromDb = errorOrEmail.right.value
      fromDb shouldEqual "wannabe.user@example.com"
    }
    it should "not save email if it already exists" in withEmail(
      "wannabe.user@example.com") {
      repository.save(mkEmail("wannabe.user@example.com"))
    }.map { errorOrEmail =>
      val fromDB = errorOrEmail.left.value
      fromDb shouldEqual EmailAlreadyExists
    }
    it should "check if email is known" in withEmail("wannabe.user@example.com")
     repository.known(Email("wannabe.user@example.com")) zip repository.known(
       Email("user@example.com").map {
         case(known, unknown))) =>
           known shouldBe true
           unknown shouldBe false
       }
  }
}