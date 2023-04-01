import Repository
import TaglessFinalInterpreter._

object Main {

  /**
   * Program is placed inside the Main to be  executed when necessary
   */

  // note that you can in fact create an object which corresponds to a case class
  case class Program[F[_] : Monad](carts: ShoppingCarts[F]) {
    // remember that the below has to follow the Map & FlatMap method
    // we also get the cardId from the corresponding UserId, so we should probably create Map[K, V] based on this
    def createAndAddToCart(product: Product, cartId: String): F[Option[ShoppingCart]] = {
      _ <- carts.create(cartId)
      maybeSc <- carts.find(cartId)
      maybeNewSc <- maybeSc.traverse(sc => carts.add(sc, product))
    }
    yield maybeNewSc
  }

  // we need to break down the program here
  object Program {

    import cats._

    // this looks like the correct steps for returning -> trying to differentiate between the Sc object and the corresponding data type but this needs to be honed in on at some point
    def createAndAddToCart[F[_] : Monad](product: Product, cartId: Int)
                                        (implicit shoppingCart: ShoppingCarts):
    F[Option[ShoppingCarts]] =
      for {
        _ <- ShoppingCart.create(cartId)
        maybeSc <- ShoppingCart.find(cartId)
        maybeNewSc <- maybeSc.traverse(sc => ShoppingCart.add(sc, product))
      } yield maybeNewSc
  }

  // the below should evidently be the final step of the application program
  val program: ProgramWithDep[RepositoryState] = ProgramWithDep {
    ShoppingCartWithDependencyInterpreter.make()

    program.createAndToCart(Product("id", "a product"), "cart1")
  }

}
