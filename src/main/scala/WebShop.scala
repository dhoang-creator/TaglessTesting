import cats.IO._

object WebShop {

  case class Product(id: String, description: String)
  case class ShoppingCart(id: String, products: List[Product])

  def create(id: String): IO[Unit]
  def find(id: String): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
}
