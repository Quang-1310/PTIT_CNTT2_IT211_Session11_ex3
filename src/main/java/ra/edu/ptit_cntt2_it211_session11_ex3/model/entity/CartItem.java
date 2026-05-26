package ra.edu.ptit_cntt2_it211_session11_ex3.model.entity;
public
class CartItem {
    private Long productId;
    private int quantity;

    public CartItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
