package com.roncoo.eshop.product.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roncoo.eshop.product.mapper.ProductSpecificationMapper;
import com.roncoo.eshop.product.model.ProductSpecification;
import com.roncoo.eshop.product.rabbitmq.RabbitMQSender;
import com.roncoo.eshop.product.rabbitmq.RabbitQueue;
import com.roncoo.eshop.product.service.ProductSpecificationService;

@Service
public class ProductSpecificationServiceImpl implements
		ProductSpecificationService {

	@Autowired
	private ProductSpecificationMapper productSpecificationMapper;

	@Autowired
	private RabbitMQSender rabbitMQSender;

	public void add(ProductSpecification productSpecification,
			String operationType) {
		productSpecificationMapper.add(productSpecification);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender.send(queue,
				"{\"event_type\": \"add\", \"data_type\": \"product_specification\", \"id\": "
						+ productSpecification.getId() + "}");
	}

	public void update(ProductSpecification productSpecification,
			String operationType) {
		productSpecificationMapper.update(productSpecification);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender
				.send(queue,
						"{\"event_type\": \"update\", \"data_type\": \"product_specification\", \"id\": "
								+ productSpecification.getId() + "}");
	}

	public void delete(Long id, String operationType) {
		productSpecificationMapper.delete(id);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender
				.send(queue,
						"{\"event_type\": \"delete\", \"data_type\": \"product_specification\", \"id\": "
								+ id + "}");
	}

	public ProductSpecification findById(Long id) {
		return productSpecificationMapper.findById(id);
	}

	@Override
	public List<ProductSpecification> findByIds(String ids) {
		return productSpecificationMapper.findByIds(ids);
	}

	@Override
	public ProductSpecification findByProductId(Long productId) {
		return productSpecificationMapper.findByProductId(productId);
	}

}
