package com.noctua.huertos;

import org.springframework.boot.SpringApplication;

public class TestHuertosApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(HuertosApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
