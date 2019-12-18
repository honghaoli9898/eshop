package com.roncoo.eshop.dataaggr.rabbitmq;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSONObject;

@Component
@RabbitListener(queues = "aggr-data-change-queue")
public class AggrDataChangeQueueReceiver {
	@Autowired
	private JedisPool jedisPool;

	@RabbitHandler
	public void process(String message) {
		JSONObject messageJSONObject = JSONObject.parseObject(message);
		String dimType = messageJSONObject.getString("dim_type");
		if ("brand".equals(dimType)) {
			System.out.println("接收到信息:" + messageJSONObject.toJSONString());
			processBrandDimDataChange(messageJSONObject);
		} else if ("category".equals(dimType)) {
			processCategoryDimDataChange(messageJSONObject);
		} else if ("product_intro".equals(dimType)) {
			processProductIntroDimDataChange(messageJSONObject);
		} else if ("product".equals(dimType)) {
			processProductDimDataChange(messageJSONObject);
		}
	}

	private void processBrandDimDataChange(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Jedis jedis = jedisPool.getResource();
		String dataJSON = jedis.get("brand_" + id);
		if (dataJSON != null && !"".equals(dataJSON)) {
			jedis.set("dim_brand_" + id, dataJSON);
		} else {
			jedis.del("dim_brand_" + id);
		}
	}

	private void processCategoryDimDataChange(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Jedis jedis = jedisPool.getResource();
		String dataJSON = jedis.get("category_" + id);
		if (dataJSON != null && !"".equals(dataJSON)) {
			jedis.set("dim_category_" + id, dataJSON);
		} else {
			jedis.del("dim_category_" + id);
		}
	}

	private void processProductIntroDimDataChange(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Jedis jedis = jedisPool.getResource();
		String dataJSON = jedis.get("product_intro_" + id);
		if (dataJSON != null && !"".equals(dataJSON)) {
			jedis.set("dim_product_intro_" + id, dataJSON);
		} else {
			jedis.del("dim_product_intro_" + id);
		}
	}

	private void processProductDimDataChange(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Jedis jedis = jedisPool.getResource();
		List<String> results = jedis.mget("product_" + id,"product_property_" + id,"product_specification_" + id);
		String productDataJSON = results.get(0);
		if (StringUtils.isNotEmpty(productDataJSON)) {
			JSONObject productDataJSONObject = JSONObject
					.parseObject(productDataJSON);
			String productPropertyDataJSON = results.get(1);
			if (StringUtils.isNotEmpty(productPropertyDataJSON)) {
				productDataJSONObject.put("product_property",
						JSONObject.parse(productPropertyDataJSON));
			}

			String productSpecificationDataJSON = results.get(2);
			if (StringUtils.isNotEmpty(productSpecificationDataJSON)) {
				productDataJSONObject.put("product_specification",
						JSONObject.parse(productSpecificationDataJSON));
			}

			jedis.set("dim_product_" + id, productDataJSONObject.toJSONString());

		} else {
			jedis.del("dim_product_" + id);
		}
	}
}
