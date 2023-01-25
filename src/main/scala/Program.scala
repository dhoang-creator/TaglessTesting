object Program {

  def createAndAddToCart[F[_]: Monad](productL Product, cartId: String)
  (implicit shoppingCarts: ShoppingCarts[F]): F[Option[ShoppingCart]] =
    for {
      _ <- shoppingCarts.create(cartId)
      maybeSc <- shoppingCarts.find(cartId)
      maybeNewSc <- maybeSc.traverse(sc => shoppingCarts.add(sc, product))
    } yield maybeNewSc

  def createAndToCart[F[_]: Monad : ShoppingCarts](product: Product, cartId: String):
    F[Option[ShoppingCart]] =
    for {
      _ <- ShoppingCarts[F].create(cartId)
      maybeSc <- ShoppingCarts[F].find(cartId)
      maybeNewSc <- maybeSc.traverse(sc => ShoppingCarts[F].add(sc, product))
    } yield maybeNewSc
}

object ShoppingCarts {
  def apply[F[_]](implicit sc: ShoppingCarts[F]): ShoppingCarts[F] = sc
}
