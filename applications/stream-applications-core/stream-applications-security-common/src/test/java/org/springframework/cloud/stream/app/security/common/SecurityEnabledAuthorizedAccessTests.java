/*
 * Copyright 2019-2020 the original author or authors.
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

package org.springframework.cloud.stream.app.security.common;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 * @author David Turanski
 * @since 3.0
 */
@TestPropertySource(properties = {
		"spring.main.web-application-type=servlet",
		"management.endpoints.web.exposure.include=health,info,bindings,env",
		"management.info.env.enabled=true",
		"spring.cloud.streamapp.security.admin-user=admin",
		"spring.cloud.streamapp.security.admin-password=binding",
		"info.name=MY TEST APP" })
public class SecurityEnabledAuthorizedAccessTests extends AbstractSecurityCommonTests {

	@Autowired
	private SecurityProperties securityProperties;

	@Autowired
	ApplicationContext applicationContext;

	@BeforeEach
	public void authenticate() {
		restTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(
				securityProperties.getUser().getName(), securityProperties.getUser().getPassword()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testHealthEndpoint() {
		ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/health", Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.hasBody()).isTrue();
		Map health = response.getBody();
		assertThat(health.get("status")).isEqualTo("UP");
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testInfoEndpoint() {
		restTemplate.getRestTemplate().getInterceptors().clear();
		ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/info", Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.hasBody()).isTrue();
		Map info = response.getBody();
		assertThat(info.get("name")).isEqualTo("MY TEST APP");
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testBindingsEndpoint() {
		ResponseEntity<?> response = this.restTemplate.getForEntity("/actuator/bindings", Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testPostBindingsEndpoint() {
		restTemplate.getRestTemplate().getInterceptors().clear();
		restTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(
				"admin", "binding"));
		ResponseEntity<Void> response = this.restTemplate.postForEntity("/actuator/bindings/upper-in-0",
				Collections.singletonMap("state", "STOPPED"), Void.class);
		assertThat(response.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);
		String result = this.restTemplate.getForEntity("/actuator/bindings", String.class).getBody();
		assertThat(result.contains("\"state\":\"stopped\"")).isTrue();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testPostBindingsEndpointWithoutAdminRoleShouldFail() {
		ResponseEntity<Void> response = this.restTemplate.postForEntity("/actuator/bindings/upper-in-0",
				Collections.singletonMap("state", "STOPPED"), Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testEnvEndpoint() {
		ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/env", Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@AfterEach
	void clearAuthorization() {
		restTemplate.getRestTemplate().getInterceptors().clear();
	}

}
