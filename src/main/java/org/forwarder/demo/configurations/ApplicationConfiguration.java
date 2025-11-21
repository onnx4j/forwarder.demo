/**
 * 
 */
package org.forwarder.demo.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Load global configurations from property file named
 * parent-application-${spring.profiles.active}.properties
 * 
 * @author HarryLee
 * @createdOn 2019年7月10日
 *
 */
@Configuration
//@PropertySource(value = "classpath:application-${spring.profiles.active}.properties")
public class ApplicationConfiguration {

}
