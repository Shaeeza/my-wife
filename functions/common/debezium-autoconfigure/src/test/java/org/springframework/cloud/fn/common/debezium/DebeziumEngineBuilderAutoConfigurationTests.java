/*
 * Copyright 2023-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.fn.common.debezium;

import io.debezium.engine.DebeziumEngine;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DebeziumEngineBuilderAutoConfiguration}.
 *
 * @author Christian Tzolov
 */
public class DebeziumEngineBuilderAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(DebeziumEngineBuilderAutoConfiguration.class));

	// We have the debezium connectors on the classpath by default.

	@Test
	void noConnectorNoProperty() {
		this.contextRunner.run((context) -> {
			assertThat(context).doesNotHaveBean(DebeziumEngine.Builder.class);
		});
	}

	@Test
	void noConnectorWithProperty() {
		this.contextRunner.withPropertyValues("debezium.properties.connector.class=Dummy")
				.withClassLoader(new FilteredClassLoader("io.debezium.connector"))
				.run((context) -> {
					assertThat(context).doesNotHaveBean(DebeziumEngine.Builder.class);
				});
	}

	@Test
	void withConnectorWithProperty() {
		this.contextRunner.withPropertyValues("debezium.properties.connector.class=Dummy").run((context) -> {
			assertThat(context).hasSingleBean(DebeziumEngine.Builder.class);
		});
	}

}
