package Cart

object ShoppingCart {

  case class Product(id: String, description: String)
  case class ShoppingCart(id: String, products: List[Product])

  // the below are side effectual code which is going to be tested via Tagless Final
  def create(id: String): IO[Unit]
  def find(id: String): IO[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
}
