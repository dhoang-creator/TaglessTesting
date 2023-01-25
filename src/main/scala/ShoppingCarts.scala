/**
 * Algebra Attempt
 */

trait ShoppingCarts[F[_]] {
  def create(id: String): F[Unit]
  def find(id: String): F[Option[ShoppingCart]]
  def add(sc: ShoppingCart, product: Product): F[ShoppingCart]
}
