package io.nem.apps.tg;

import java.util.Arrays;

import org.nem.core.model.TransactionFeeCalculatorAfterFork;
import org.nem.core.node.NodeEndpoint;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.nem.apps.builders.ConfigurationBuilder;

@SpringBootApplication
@ComponentScan(basePackages = "io.nem.apps.tg")
public class Application {
	private static String host = "alice2.nem.ninja";
	private static String port = "8081";
	private static String network = "mainnet"; // default
	public static void main(String[] args) {

		if(args.length > 0) {
			Application.port = args[0];
			Application.network = args[1];
		}

		ConfigurationBuilder.nodeNetworkName(Application.network).nodeEndpoint(new NodeEndpoint("http", host, 7890)).setup();
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			container.setContextPath("/transgrab");
			container.setPort(Integer.valueOf(Application.port));
		});
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};
	}

}