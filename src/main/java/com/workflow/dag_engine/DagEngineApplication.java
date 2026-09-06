package com.workflow.dag_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DagEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DagEngineApplication.class, args);
	}

}
