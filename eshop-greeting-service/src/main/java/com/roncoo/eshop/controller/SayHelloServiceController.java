package com.roncoo.eshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roncoo.eshop.service.SayHelloService;

@RestController
public class SayHelloServiceController {
	@Autowired
	SayHelloService sayHelloService;

	@RequestMapping("/sayHello")
	public String products(@RequestParam String name) {
		return sayHelloService.sayHello(name);
	}

	@Value("${server.port}")
	private String port;

	@Value("${defaultName}")
	private String defaultName;

	@RequestMapping("/greeting")
	public String greeting(@RequestParam String name) {
		String result = null;

		if (name != null && !"".equals(name)) {
			result = sayHelloService.sayHello(name);
		} else {
			result = "hello, this is default name: " + defaultName;
		}

		result += " , through greeting service from port: " + port;

		return result;
	}
}
