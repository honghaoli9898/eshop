package com.roncoo.eshop.product.service;

import java.util.List;

import com.roncoo.eshop.product.model.ProductSpecification;

public interface ProductSpecificationService {

	public void add(ProductSpecification productSpecification,
			String operationType);

	public void update(ProductSpecification productSpecification,
			String operationType);

	public void delete(Long id, String operationType);

	public ProductSpecification findById(Long id);

	public List<ProductSpecification> findByIds(String ids);

	public ProductSpecification findByProductId(Long productId);
}
