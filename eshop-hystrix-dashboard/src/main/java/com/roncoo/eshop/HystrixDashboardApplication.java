package com.roncoo.eshop;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

import cn.hutool.core.util.NetUtil;

@SpringBootApplication
//增加断路器视图注解
@EnableHystrixDashboard
public class HystrixDashboardApplication {
	public static void main(String[] args) {
    	int port = 8766;
		if(!NetUtil.isUsableLocalPort(port)) {
			System.err.printf("端口%d被占用了，无法启动%n", port );
    		System.exit(1);
    	}
        new SpringApplicationBuilder(HystrixDashboardApplication.class).properties("server.port=" + port).run(args);

	}

}
