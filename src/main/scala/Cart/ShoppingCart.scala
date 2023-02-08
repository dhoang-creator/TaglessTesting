package Cart

import cats.effect.IO

object ShoppingCart {

  // should we be differentiating between the ShoppingCart data type and that of the object which we're currently inside
  case class UserId(id: Int)
  case class UserProfile(userId: UserId, products: List[Product])
  case class Product(id: Int, description: String)
  case class ShoppingCart(id: Int, products: List[Product])

  // we need to rectify the IO Error for the below
  def create(id: Int): IO[A]
  def findId(id: Int): Option[UserId]
  def findSc(id: Int): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
}
