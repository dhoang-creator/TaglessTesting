type ShoppingCartRepository = Map[String, ShoppingCart]
type ScRepoState[A] = State[ShoppingCartRepository, A]

implicit object TestShoppingCartInterpreter extends ShoppingCart[ScRepoState] {
  override def create(id: String): ScRepoState[Unit] = {
    State.modify { carts =>
      val shoppingCart = ShoppingCart(id, List())
      carts + (id -> shoppingCart)
    }
    override def find(id: String): ScRepoState[Option[ShoppingCart]] = {
      State.inspect { carts =>
        carts.get(id)
      }
      override def add(sc: ShoppingCart, product: Product): ScRepoState[ShoppingCart] =
        State {
          carts =>
            val products = sc.products
            val updatedCart = sc.copy(products = product :: products)
            (carts + (sc.id -> updatedCart), updatedCart)
        }
    }
  }
}
