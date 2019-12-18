package com.roncoo.eshop.datasync.service.fallback;

import org.springframework.stereotype.Component;

import com.roncoo.eshop.datasync.service.EshopProductService;

@Component
public class EshopProductServiceFallback implements EshopProductService {

	public String findBrandById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findBrandByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findCategoryById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findProductIntroById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findProductPropertyById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findProductById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findProductSpecificationById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findCategoryByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProductIntroByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProductPropertyByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProductByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProductSpecificationByIds(String ids) {
		// TODO Auto-generated method stub
		return null;
	}
}