import Repository.{ShoppingCart, UserId, Product}

/**
 * By splitting the Algebra/Interface into component parts, it allows us to compartmentalise the IO Testing
 */

// there seems to be a lot of confusion here as to what the ShoppingCart is in fact? A generic or a case class?

trait RepoModelTraits {

  // create
  def createUserId[F[_]](shoppingCart: ShoppingCart, product: Product): F[UserId]
  def createShoppingCart[F[_]](userId: UserId, product: Product): F[ShoppingCart]

  // findUserId
  def findUserId[F[_]](userId: Option[UserId]): F[Option[ShoppingCart]]

  // finalShoppingCartState
  def findShoppingCart[F[_]](userId: Option[UserId]): F[Option[ShoppingCart]]
  def addProduct[F[_]](shoppingCart: ShoppingCart, product: Product): F[ShoppingCart]

  // ErrorLogging
  def error[F[_]](error: Throwable): F[Unit]
}
