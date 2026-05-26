Phân tích kịch bản:
Bước 1 (User A): Giỏ hàng của A đang có 5 sản phẩm X. Lúc này, hệ thống kiểm tra Product X có đủ tồn kho (ví dụ ban đầu kho có 10).
Bước 2 (User B): B thực hiện thanh toán mua 3 sản phẩm X -> Hệ thống trừ kho của X từ 10 xuống còn 7 (product.setStock(7)).
Bước 3 (User A): A nhấn nút tăng số lượng trong giỏ hàng lên 7.
Những điểm tiềm ẩn gây lỗi và hành vi không mong muốn trong ShoppingCartService:
Lỗi bất đồng bộ dữ liệu: Khi User A gửi yêu cầu cập nhật lên 7, hàm updateProductQuantity sẽ gọi productRepository.findById(productId) để bốc dữ liệu mới nhất từ Database lên. Tại thời điểm này, tồn kho thực tế của sản phẩm đã bị User B rút xuống còn 7.
Hành vi xử lý điều kiện biên: * Số lượng A yêu cầu mới là 7.
Tồn kho hiện tại trong DB là 7.Phép so sánh product.getStock() < newQuantity (7 < 7) trả về false.
Do đó, hệ thống vẫn cho phép User A cập nhật lên 7 thành công và lưu giỏ hàng.
Hệ quả tiêu cực tiềm ẩn ở bước Thanh toán (Checkout): * Mặc dù bước cập nhật giỏ hàng không lỗi, nhưng nếu User A bấm Thanh toán ngay sau đó, tổng số lượng sản phẩm X cần giao cho cả A và B sẽ là 7 (của A) + 3 (của B) = 10.
Trong khi kho thực tế ban đầu chỉ có 10, nhưng sau khi B mua 3 thì kho chỉ còn 7. Nếu hệ thống cho phép A mua tiếp 7, tổng số lượng bán ra sẽ là 10 trên một kho hàng thực tế đã giảm $\rightarrow$ Gây ra hiện tượng Bán âm kho / Bán lố hàng (Overselling) ở tầng Payment/Order nếu tầng đó không khóa dữ liệu.