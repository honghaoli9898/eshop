package com.roncoo.eshop.product.service;

import java.util.List;

import com.roncoo.eshop.product.model.ProductProperty;

public interface ProductPropertyService {

	public void add(ProductProperty productProperty, String operationType);

	public void update(ProductProperty productProperty, String operationType);

	public void delete(Long id, String operationType);

	public ProductProperty findById(Long id);

	public List<ProductProperty> findByIds(String ids);

	public ProductProperty findByProductId(Long productId);
}
