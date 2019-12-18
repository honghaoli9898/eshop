package com.roncoo.eshop.price;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
@EnableEurekaClient
public class EshopPriceServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EshopPriceServiceApplication.class, args);
	}

	@Bean
	public JedisPool jedis() {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(100);
			config.setMaxIdle(5);
			config.setMaxWaitMillis(1000 * 100);
			config.setTestOnBorrow(true);
			JedisPool jedisPool = new JedisPool(config, "127.0.0.1", 6379,3000,"haojiayou@");
			return jedisPool;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
