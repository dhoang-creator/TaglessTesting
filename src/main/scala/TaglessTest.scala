import cats.effect.IO

object TaglessTest {

  case class UserId(id: Int)
  case class UserProfile(userId: UserId, products: List[Product])
  case class Product(id: Int, description: String)
  case class ShoppingCart(id: Int, products: List[Product])

  // DBIO Methods
  def create(id: Int): IO[A]
  def findId(id: Int): Option[UserId]
  def findSc(id: Int): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
  def error(e: Throwable): IO[Unit]
}
