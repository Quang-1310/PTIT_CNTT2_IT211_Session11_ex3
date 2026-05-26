package ra.edu.ptit_cntt2_it211_session11_ex3.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShoppingCart {
    private Long id;
    private Long userId;
    private List<CartItem> items;

    public ShoppingCart(Long userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
    public void setId(Long id) { this.id = id; }

    public Optional<CartItem> findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    public void addItem(CartItem item) {
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        this.items.remove(item);
    }
}
