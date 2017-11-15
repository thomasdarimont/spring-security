/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.security.web.server.savedrequest;

import org.junit.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 * @since 5.0
 */
public class WebSessionServerRequestCacheTests {
	private WebSessionServerRequestCache cache = new WebSessionServerRequestCache();

	@Test
	public void saveRequestGetRequestWhenGetThenFound() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/secured/"));
		this.cache.saveRequest(exchange).block();

		URI saved = this.cache.getRequest(exchange).block();

		assertThat(saved).isEqualTo(exchange.getRequest().getURI());
	}

	@Test
	public void saveRequestGetRequestWhenPostThenNotFound() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/secured/"));
		this.cache.saveRequest(exchange).block();

		assertThat(this.cache.getRequest(exchange).block()).isNull();
	}

	@Test
	public void saveRequestGetRequestWhenPostAndCustomMatcherThenFound() {
		this.cache.setSaveRequestMatcher(e -> ServerWebExchangeMatcher.MatchResult.match());
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/secured/"));
		this.cache.saveRequest(exchange).block();

		URI saved = this.cache.getRequest(exchange).block();

		assertThat(saved).isEqualTo(exchange.getRequest().getURI());
	}

	@Test
	public void saveRequestRemoveRequestWhenThenFound() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/secured/"));
		this.cache.saveRequest(exchange).block();

		ServerHttpRequest saved = this.cache.removeRequest(exchange).block();

		assertThat(saved.getURI()).isEqualTo(exchange.getRequest().getURI());
	}

	@Test
	public void removeRequestGetRequestWhenDefaultThenNotFound() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/secured/"));
		this.cache.saveRequest(exchange).block();

		this.cache.removeRequest(exchange).block();

		assertThat(this.cache.getRequest(exchange).block()).isNull();
	}
}
