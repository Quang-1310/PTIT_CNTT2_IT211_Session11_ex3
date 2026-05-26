package ra.edu.ptit_cntt2_it211_session11_ex3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.CartItem;
import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.Product;
import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.ShoppingCart;
import ra.edu.ptit_cntt2_it211_session11_ex3.repository.CartRepository;
import ra.edu.ptit_cntt2_it211_session11_ex3.repository.ProductRepository;
import ra.edu.ptit_cntt2_it211_session11_ex3.service.ShoppingCartService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShoppingCartServiceTest {

    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        cartRepository = mock(CartRepository.class);
        shoppingCartService = new ShoppingCartService(productRepository, cartRepository);
    }

    @Test
    @DisplayName("Thêm sản phẩm mới hoàn toàn vào giỏ hàng thành công")
    void addProductToCart_newProductAndValidStock() {
        Long userId = 1L, productId = 100L;
        Product product = new Product(productId, "iPhone 15", 1000.0, 10);
        ShoppingCart cart = new ShoppingCart(userId);

        when(productRepository.findById(productId)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart result = shoppingCartService.addProductToCart(userId, productId, 2);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Thêm sản phẩm đã tồn tại trong giỏ (Cộng dồn số lượng)")
    void addProductToCart_existingProductInCart() {
        Long userId = 1L, productId = 100L;
        Product product = new Product(productId, "iPhone 15", 1000.0, 10);
        ShoppingCart cart = new ShoppingCart(userId);
        cart.addItem(new CartItem(productId, 3));

        when(productRepository.findById(productId)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        shoppingCartService.addProductToCart(userId, productId, 4);

        ArgumentCaptor<ShoppingCart> cartCaptor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(cartRepository).save(cartCaptor.capture());

        ShoppingCart savedCart = cartCaptor.getValue();
        assertThat(savedCart.findItemByProductId(productId).get().getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Cập nhật số lượng sản phẩm trong giỏ thành công")
    void updateProductQuantity() {
        Long userId = 1L, productId = 100L;
        Product product = new Product(productId, "iPhone 15", 1000.0, 10);
        ShoppingCart cart = new ShoppingCart(userId);
        cart.addItem(new CartItem(productId, 2));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(product);
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        shoppingCartService.updateProductQuantity(userId, productId, 5);

        assertThat(cart.findItemByProductId(productId).get().getQuantity()).isEqualTo(5);
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Xóa sản phẩm khỏi giỏ hàng thành công")
    void removeProductFromCart() {
        Long userId = 1L, productId = 100L;
        ShoppingCart cart = new ShoppingCart(userId);
        CartItem item = new CartItem(productId, 2);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        shoppingCartService.removeProductFromCart(userId, productId);

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Thêm sản phẩm với số lượng <= 0")
    void addProductToCart_quantityIsZeroOrNegative() {
        assertThatThrownBy(() -> shoppingCartService.addProductToCart(1L, 100L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thêm sản phẩm vượt quá tồn kho hệ thống")
    void addProductToCart() {
        Long userId = 1L, productId = 100L;
        Product product = new Product(productId, "iPhone 15", 1000.0, 5);
        ShoppingCart cart = new ShoppingCart(userId);

        when(productRepository.findById(productId)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> shoppingCartService.addProductToCart(userId, productId, 6))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock for product");

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Giỏ hàng chưa tồn tại (Hệ thống tự động tạo mới giỏ)")
    void addProductToCart_cartDoesNotExist() {
        Long userId = 99L, productId = 100L;
        Product product = new Product(productId, "MacBook", 2000.0, 10);

        when(productRepository.findById(productId)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart result = shoppingCartService.addProductToCart(userId, productId, 1);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getItems()).hasSize(1);
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Cập nhật vượt quá tồn kho mới sau khi bị người khác mua")
    void updateProductQuantity_exceedsNewReducedStock() {
        Long userId = 1L, productId = 100L;
        Product productInDb = new Product(productId, "iPhone 15", 1000.0, 7);
        ShoppingCart cart = new ShoppingCart(userId);
        cart.addItem(new CartItem(productId, 5));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(productInDb);

        assertThatThrownBy(() -> shoppingCartService.updateProductQuantity(userId, productId, 9))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock for product");

        verify(cartRepository, never()).save(any());
    }

}
