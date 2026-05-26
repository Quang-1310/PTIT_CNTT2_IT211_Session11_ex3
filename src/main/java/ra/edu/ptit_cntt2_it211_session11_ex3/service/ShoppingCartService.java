package ra.edu.ptit_cntt2_it211_session11_ex3.service;

import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.CartItem;
import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.Product;
import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.ShoppingCart;
import ra.edu.ptit_cntt2_it211_session11_ex3.repository.CartRepository;
import ra.edu.ptit_cntt2_it211_session11_ex3.repository.ProductRepository;

import java.util.Optional;

public class ShoppingCartService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public ShoppingCartService(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    public ShoppingCart addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> new ShoppingCart(userId));

        Optional<CartItem> existingItem = cart.findItemByProductId(productId);
        int currentQuantityInCart = existingItem.map(CartItem::getQuantity).orElse(0);
        int totalRequestedQuantity = currentQuantityInCart + quantity;

        if (product.getStock() < totalRequestedQuantity) {
            throw new IllegalStateException("Not enough stock for product: " + product.getName());
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(totalRequestedQuantity);
        } else {
            cart.addItem(new CartItem(productId, quantity));
        }

        return cartRepository.save(cart);
    }

    public ShoppingCart updateProductQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem itemToUpdate = cart.findItemByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart: " + productId));

        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found in database: " + productId);
        }

        if (product.getStock() < newQuantity) {
            throw new IllegalStateException("Not enough stock for product: " + product.getName());
        }

        itemToUpdate.setQuantity(newQuantity);
        return cartRepository.save(cart);
    }

    public ShoppingCart removeProductFromCart(Long userId, Long productId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem itemToRemove = cart.findItemByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart: " + productId));

        cart.removeItem(itemToRemove);
        return cartRepository.save(cart);
    }
}