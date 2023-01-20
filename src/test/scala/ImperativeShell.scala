/**
 * This project should be to showcase the 'Functional Core, Imperative Shell' paradigm and highlight the use of
 * TDD/BDD alongside that of the Tagless Final approach to Testing IO Monads relationshis to Application Business Logic
 */

// below is taken from the SEEK article and forms part of an application that includes IO Monad Layers,
// TODO: Expand upon the below with a simplistic HTTP API and where you can map out the relationships.

object ImperativeShell {

  class JwksTest(keys: Set[Jwk]) extends JwksDsl[TestProgram] {
    def getKeys: TestProgram[Set[Jwk]] = wrapped(keys)
  }

  class LoggerTest extends Logger[TestProgram] {
    def into(msg: String): TestProgram[Unit] = wrapped(LogInfo(msg))
    def warn(msg: String): TestProgram[Unit] = wrapped(LogWarn(msg))
    def error(msg: String): TestProgram[Unit] = wrapped(LogError(msg))
  }

  sealed trait SideEffect
  final case class LogInfo(msg: String) extends SideEffect
  final case class LogWarn(msg: String) extends SideEffect
  final case class LogError(msg: String) extends SideEffect

  type TestProgram[A] = WriterT[IO, List[SideEffect], A]

  def wrapped[A](value: A): TestProgram[A] = {
    WriterT[IO, List[SideEffect], A](IO.pure((List(), value)))
  }
  def wrapped(se: SideEffect): TestProgram[Unit] =
    WriterT[IO, List[SideEffect], Unit](IO.pure((List(se), ())))

}
