package ra.edu.ptit_cntt2_it211_session11_ex3.repository;

import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.ShoppingCart;

import java.util.Optional;

public interface CartRepository {
    Optional<ShoppingCart> findByUserId(Long userId);
    ShoppingCart save(ShoppingCart cart);
}
