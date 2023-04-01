import cats.effect.IO

object Repository {

  // are we making use of each of the below in the Interpreter code?
  case class UserId(id: Int)
  case class Product(id: Int, description: String)
  case class ShoppingCart(id: Int, products: List[Product])

  // DBIO Methods

  // what's the whole point of wrapping the output in IOs?
  def createUserId(shoppingCart: ShoppingCart, product: Product): IO[UserId]
  def createShoppingCart(userId: UserId, product: Product): IO[ShoppingCart]
  def findId(id: Int): IO[Option[UserId]]
  def findSc(id: Int): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
  def error(e: Throwable): IO[Unit]
}
