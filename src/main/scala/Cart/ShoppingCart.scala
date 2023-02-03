package Cart

import cats.effect.IO

object ShoppingCart {


  case class UserId(id: String)
  case class UserProfile(userId: UserId, products: List[Product])
  case class Product(id: String, description: String)
  case class ShoppingCart(id: String, products: List[Product])

  // we need to rectify the IO Error for the below
  def create(id: String): IO[A]
  def find(id: String): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
}
