package org.forwarder.demo;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAutoConfiguration
@SpringBootApplication(scanBasePackageClasses = { ApplicationStarter.class })
public class ApplicationStarter extends SpringBootServletInitializer {

	public static ApplicationContext springContext;

	@Override
	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		logger.info("ApplicationStarter.createRootApplicationContext");
		return super.createRootApplicationContext(servletContext);
	}

	@Override
	protected SpringApplicationBuilder createSpringApplicationBuilder() {
		logger.info("ApplicationStarter.createSpringApplicationBuilder");
		return super.createSpringApplicationBuilder();
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		logger.info("ApplicationStarter.onStartup");
		super.onStartup(servletContext);
	}

	@Override
	protected WebApplicationContext run(SpringApplication application) {
		logger.info("ApplicationStarter.run");
		return super.run(application);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ApplicationStarter.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ApplicationStarter.class, args);
		//log.info("Ecwalk application has started");
	}

}
