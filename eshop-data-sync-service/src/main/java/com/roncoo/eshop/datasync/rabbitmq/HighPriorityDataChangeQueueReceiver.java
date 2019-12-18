package com.roncoo.eshop.datasync.rabbitmq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.datasync.service.EshopProductService;

/**
 * 
 * 数据同步服务，就是获取各种原子数据的变更消息
 * 
 * @author lihonghao
 *
 */
@Component
@RabbitListener(queues = "high-priority-data-change-queue")
public class HighPriorityDataChangeQueueReceiver {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HighPriorityDataChangeQueueReceiver.class);
	private List<JSONObject> brandDataChangeMessageList = new ArrayList<JSONObject>();
	private List<JSONObject> categoryDataChangeMessageList = new ArrayList<JSONObject>();
	private List<JSONObject> productIntroDataChangeMessageList = new ArrayList<JSONObject>();
	private List<JSONObject> productDataChangeMessageList = new ArrayList<JSONObject>();
	private List<JSONObject> productPropertyDataChangeMessageList = new ArrayList<JSONObject>();
	private List<JSONObject> productSpecificationDataChangeMessageList = new ArrayList<JSONObject>();
	@Autowired
	private EshopProductService eshopProductService;
	@Autowired
	private JedisPool jedisPool;
	@Autowired
	private RabbitMQSender rabbitMQSender;

	private Set<String> dimDataChangeMessageSet = Collections
			.synchronizedSet(new HashSet<String>());

	public HighPriorityDataChangeQueueReceiver() {
		new SendThread().start();
	}

	@RabbitHandler
	public void process(String message) {
		// 对这个message进行解析
		JSONObject jsonObject = JSONObject.parseObject(message);

		// 先获取data_type
		String dataType = jsonObject.getString("data_type");
		if ("brand".equals(dataType)) {
			processBrandDataChangeMessage(jsonObject);
		} else if ("category".equals(dataType)) {
			processCategoryDataChangeMessage(jsonObject);
		} else if ("product_intro".equals(dataType)) {
			processProductIntroDataChangeMessage(jsonObject);
		} else if ("product_property".equals(dataType)) {
			processProductPropertyDataChangeMessage(jsonObject);
		} else if ("product".equals(dataType)) {
			processProductDataChangeMessage(jsonObject);
		} else if ("product_specification".equals(dataType)) {
			processProductSpecificationDataChangeMessage(jsonObject);
		}
	}

	private void processBrandDataChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");
		if ("add".equals(eventType) || "update".equals(eventType)) {
			brandDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将品牌数据放入内存list中】,list.size="
					+ brandDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| brandDataChangeMessageList.size() >= 2) {

					if (brandDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out.println("【将品牌数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < brandDataChangeMessageList.size(); i++) {
						ids += brandDataChangeMessageList.get(i).getLong("id");
						if (i < brandDataChangeMessageList.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【品牌数据ids生成】ids=" + ids);

					JSONArray brandJSONArray = JSONArray
							.parseArray(eshopProductService.findBrandByIds(ids));

					System.out.println("【通过批量调用获取到品牌数据】jsonArray="
							+ brandJSONArray.toJSONString());

					for (int i = 0; i < brandJSONArray.size(); i++) {
						JSONObject dataJSONObject = brandJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set("brand_" + dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out.println("【将品牌数据写入redis】brandId="
								+ dataJSONObject.getLong("id"));

						dimDataChangeMessageSet
								.add("{\"dim_type\": \"brand\", \"id\": "
										+ dataJSONObject.getLong("id") + "}");

						System.out.println("【将品牌数据写入内存去重set中】brandId="
								+ dataJSONObject.getLong("id"));
					}

					brandDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("brand_" + id);
			dimDataChangeMessageSet.add("{\"dim_type\": \"brand\", \"id\": "
					+ id + "}");
		}
	}

	private void processCategoryDataChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");

		if ("add".equals(eventType) || "update".equals(eventType)) {
			categoryDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将category数据放入内存list中】,list.size="
					+ categoryDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| categoryDataChangeMessageList.size() >= 2) {
					if (categoryDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out.println("【将category数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < categoryDataChangeMessageList.size(); i++) {
						ids += categoryDataChangeMessageList.get(i).getLong(
								"id");
						if (i < categoryDataChangeMessageList.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【category数据ids生成】ids=" + ids);

					JSONArray categoryJSONArray = JSONArray
							.parseArray(eshopProductService
									.findCategoryByIds(ids));

					System.out.println("【通过批量调用获取到category数据】jsonArray="
							+ categoryJSONArray.toJSONString());

					for (int i = 0; i < categoryJSONArray.size(); i++) {
						JSONObject dataJSONObject = categoryJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set("category_" + dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out.println("【将category数据写入redis】categoryId="
								+ dataJSONObject.getLong("id"));

						dimDataChangeMessageSet
								.add("{\"dim_type\": \"category\", \"id\": "
										+ id + "}");

						System.out.println("【将category数据写入内存去重set中】categoryId="
								+ dataJSONObject.getLong("id"));
					}

					categoryDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("category_" + id);
		}

	}

	private void processProductIntroDataChangeMessage(
			JSONObject messageJSONObject) {
		// Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");

		if ("add".equals(eventType) || "update".equals(eventType)) {
			productIntroDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将productIntro数据放入内存list中】,list.size="
					+ productIntroDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| productIntroDataChangeMessageList.size() >= 2) {
					if (productIntroDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out
							.println("【将productIntro数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < productIntroDataChangeMessageList
							.size(); i++) {
						ids += productIntroDataChangeMessageList.get(i)
								.getLong("id");
						if (i < productIntroDataChangeMessageList.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【productIntro数据ids生成】ids=" + ids);

					JSONArray productIntroJSONArray = JSONArray
							.parseArray(eshopProductService
									.findProductIntroByIds(ids));

					System.out.println("【通过批量调用获取到productIntro数据】jsonArray="
							+ productIntroJSONArray.toJSONString());

					for (int i = 0; i < productIntroJSONArray.size(); i++) {
						JSONObject dataJSONObject = productIntroJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set(
								"productIntro_" + dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out
								.println("【将productIntro数据写入redis】productIntroId="
										+ dataJSONObject.getLong("id"));

						dimDataChangeMessageSet
								.add("{\"dim_type\": \"product_intro\", \"id\": "
										+ productId + "}");

						System.out
								.println("【将productIntro数据写入内存去重set中】productIntroId="
										+ dataJSONObject.getLong("id"));
					}

					productIntroDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("product_intro_" + productId);
		}

	}

	private void processProductDataChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");

		if ("add".equals(eventType) || "update".equals(eventType)) {
			productDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将product数据放入内存list中】,list.size="
					+ productDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| productDataChangeMessageList.size() >= 2) {
					if (productDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out.println("【将product数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < productDataChangeMessageList.size(); i++) {
						ids += productDataChangeMessageList.get(i)
								.getLong("id");
						if (i < productDataChangeMessageList.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【product数据ids生成】ids=" + ids);

					JSONArray productJSONArray = JSONArray
							.parseArray(eshopProductService
									.findProductByIds(ids));

					System.out.println("【通过批量调用获取到product数据】jsonArray="
							+ productJSONArray.toJSONString());

					for (int i = 0; i < productJSONArray.size(); i++) {
						JSONObject dataJSONObject = productJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set("product_" + dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out.println("【将product数据写入redis】productId="
								+ dataJSONObject.getLong("id"));

						dimDataChangeMessageSet
								.add("{\"dim_type\": \"product\", \"id\": "
										+ id + "}");

						System.out.println("【将product数据写入内存去重set中】productId="
								+ dataJSONObject.getLong("id"));
					}

					productDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("product_" + id);
		}

	}

	private void processProductPropertyDataChangeMessage(
			JSONObject messageJSONObject) {
		// Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");

		if ("add".equals(eventType) || "update".equals(eventType)) {
			productPropertyDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将productProperty数据放入内存list中】,list.size="
					+ productPropertyDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| productPropertyDataChangeMessageList.size() >= 2) {
					if (productPropertyDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out
							.println("【将productProperty数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < productPropertyDataChangeMessageList
							.size(); i++) {
						ids += productPropertyDataChangeMessageList.get(i)
								.getLong("id");
						if (i < productPropertyDataChangeMessageList.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【productProperty数据ids生成】ids=" + ids);

					JSONArray productJSONArray = JSONArray
							.parseArray(eshopProductService
									.findProductPropertyByIds(ids));

					System.out.println("【通过批量调用获取到productProperty数据】jsonArray="
							+ productJSONArray.toJSONString());

					for (int i = 0; i < productJSONArray.size(); i++) {
						JSONObject dataJSONObject = productJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set(
								"product_property_"
										+ dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out
								.println("【将productProperty数据写入redis】productPropertyId="
										+ dataJSONObject.getLong("id"));

						dimDataChangeMessageSet
								.add("{\"dim_type\": \"product\", \"id\": "
										+ productId + "}");

						System.out
								.println("【将productProperty数据写入内存去重set中】productPropertyId="
										+ dataJSONObject.getLong("id"));
					}

					productPropertyDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("product_property_" + productId);
		}

	}

	private void processProductSpecificationDataChangeMessage(
			JSONObject messageJSONObject) {
//		Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");

		if ("add".equals(eventType) || "update".equals(eventType)) {
			productSpecificationDataChangeMessageList.add(messageJSONObject);

			System.out.println("【将productSpecification数据放入内存list中】,list.size="
					+ productSpecificationDataChangeMessageList.size());
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - startTime > 200
						|| productSpecificationDataChangeMessageList.size() >= 2) {
					if (productSpecificationDataChangeMessageList.isEmpty()) {
						break;
					}
					System.out
							.println("【将productSpecification数据内存list大小大于等于2，开始执行批量调用】");

					String ids = "";

					for (int i = 0; i < productSpecificationDataChangeMessageList
							.size(); i++) {
						ids += productSpecificationDataChangeMessageList.get(i)
								.getLong("id");
						if (i < productSpecificationDataChangeMessageList
								.size() - 1) {
							ids += ",";
						}
					}

					System.out.println("【productSpecification数据ids生成】ids="
							+ ids);

					JSONArray productJSONArray = JSONArray
							.parseArray(eshopProductService
									.findProductSpecificationByIds(ids));

					System.out
							.println("【通过批量调用获取到productSpecification数据】jsonArray="
									+ productJSONArray.toJSONString());

					for (int i = 0; i < productJSONArray.size(); i++) {
						JSONObject dataJSONObject = productJSONArray
								.getJSONObject(i);

						Jedis jedis = jedisPool.getResource();
						jedis.set(
								"product_specification_"
										+ dataJSONObject.getLong("id"),
								dataJSONObject.toJSONString());

						System.out
								.println("【将productSpecification数据写入redis】productSpecificationId="
										+ dataJSONObject.getLong("id"));
						dimDataChangeMessageSet
								.add("{\"dim_type\": \"product\", \"id\": "
										+ productId + "}");

						System.out
								.println("【将productSpecification数据写入内存去重set中】productSpecificationId="
										+ dataJSONObject.getLong("id"));
					}

					productPropertyDataChangeMessageList.clear();
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if ("delete".equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("product_specification_" + productId);
		}

	}

	private class SendThread extends Thread {

		@Override
		public void run() {
			while (true) {
				if (!dimDataChangeMessageSet.isEmpty()) {
					for (String message : dimDataChangeMessageSet) {
						rabbitMQSender
								.send("high-priority-aggr-data-change-queue",
										message);
						LOGGER.info("【将去重后的维度数据变更消息发送到下一个queue】,message="
								+ message);
					}
					dimDataChangeMessageSet.clear();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
