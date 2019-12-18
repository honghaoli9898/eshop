package com.roncoo.eshop.product.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roncoo.eshop.product.mapper.ProductIntroMapper;
import com.roncoo.eshop.product.model.ProductIntro;
import com.roncoo.eshop.product.rabbitmq.RabbitMQSender;
import com.roncoo.eshop.product.rabbitmq.RabbitQueue;
import com.roncoo.eshop.product.service.ProductIntroService;

@Service
public class ProductIntroServiceImpl implements ProductIntroService {

	@Autowired
	private ProductIntroMapper productIntroMapper;

	@Autowired
	private RabbitMQSender rabbitMQSender;

	public void add(ProductIntro productIntro, String operationType) {
		productIntroMapper.add(productIntro);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender.send(queue,
				"{\"event_type\": \"add\", \"data_type\": \"product_intro\", \"id\": "
						+ productIntro.getId() + "}");
	}

	public void update(ProductIntro productIntro, String operationType) {
		productIntroMapper.update(productIntro);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender.send(queue,
				"{\"event_type\": \"update\", \"data_type\": \"product_intro\", \"id\": "
						+ productIntro.getId() + "}");
	}

	public void delete(Long id, String operationType) {
		productIntroMapper.delete(id);
		String queue = null;
		if (StringUtils.isEmpty(operationType)) {
			queue = RabbitQueue.DATA_CHANGE_QUEUE;
		} else if ("refresh".equals(operationType)) {
			queue = RabbitQueue.REFRESH_DATA_CHANGE_QUEUE;
		} else if ("high".equals(operationType)) {
			queue = RabbitQueue.HIGH_PRIORITY_DATA_CHANGE_QUEUE;
		}
		rabbitMQSender.send(queue,
				"{\"event_type\": \"delete\", \"data_type\": \"product_intro\", \"id\": "
						+ id + "}");
	}

	public ProductIntro findById(Long id) {
		return productIntroMapper.findById(id);
	}

	@Override
	public List<ProductIntro> findByIds(String ids) {
		return productIntroMapper.findByIds(ids);
	}

}
