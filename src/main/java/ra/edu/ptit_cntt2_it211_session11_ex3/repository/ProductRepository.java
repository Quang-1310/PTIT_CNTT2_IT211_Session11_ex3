package ra.edu.ptit_cntt2_it211_session11_ex3.repository;

import ra.edu.ptit_cntt2_it211_session11_ex3.model.entity.Product;

public interface ProductRepository {
    Product findById(Long id);
    void save(Product product);
}
