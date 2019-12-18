package com.roncoo.eshop.product.service;

import java.util.List;

import com.roncoo.eshop.product.model.Product;

public interface ProductService {
	public void add(Product product, String operationType);

	public void update(Product product, String operationType);

	public void delete(Long id, String operationType);

	public Product findById(Long id);

	public List<Product> findByIds(String ids);
}
