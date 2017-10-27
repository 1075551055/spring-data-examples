/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.jdbc.basics;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.mapping.event.BeforeSave;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import example.springdata.jdbc.basics.domain.LegoSet;
import example.springdata.jdbc.basics.domain.LegoSetRepository;

/**
 * Demonstrates non trivial usage of Spring Data JDBC especially handling of collections and references crossing aggregate boundaries.
 * It tries to showcase the following
 * <ul>
 * <li>Custom Names for columns and tables via NamingStrategy</li>
 * <li>Manual id generation</li>
 * </ul>
 *
 * @author Jens Schauder
 */
@EnableJdbcRepositories
public class AggregatesApplication implements CommandLineRunner {

	private static final AtomicInteger id = new AtomicInteger(0);

	@Autowired
	private LegoSetRepository repository;

	@Override
	public void run(String... args) throws Exception {

		repository.save(new LegoSet());
		//Output.list(repository.findAll(), "LegoSet");
	}

	public static void main(String[] args) {
		SpringApplication.run(AggregatesApplication.class, args);
	}

	@Bean
	DataSource dataSource() {

		return new EmbeddedDatabaseBuilder() //
				.generateUniqueName(true) //
				.setType(EmbeddedDatabaseType.HSQL) //
				.setScriptEncoding("UTF-8") //
				.ignoreFailedDrops(true) //
				.addScript("createLegoSet.sql") //
				.build();
	}

	@Bean
	public ApplicationListener<?> timeStampingSaveTime() {

		return (ApplicationListener<BeforeSave>) event -> {

			Object entity = event.getEntity();
			if (entity instanceof LegoSet) {
				LegoSet legoSet = (LegoSet) entity;
				legoSet.setId(id.incrementAndGet());
			}
		};
	}
}
