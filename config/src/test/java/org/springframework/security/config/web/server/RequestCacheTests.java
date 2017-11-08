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

package org.springframework.security.config.web.server;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.annotation.web.reactive.ServerHttpSecurityConfigurationBuilder;
import org.springframework.security.config.web.server.FormLoginTests.DefaultLoginPage;
import org.springframework.security.config.web.server.FormLoginTests.HomePage;
import org.springframework.security.htmlunit.server.WebTestClientHtmlUnitDriverBuilder;
import org.springframework.security.test.web.reactive.server.WebTestClientBuilder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 * @since 5.0
 */
public class RequestCacheTests {
	private ServerHttpSecurity http = ServerHttpSecurityConfigurationBuilder.httpWithDefaultAuthentication();

	@Test
	public void defaultFormLoginRequestCache() {
		SecurityWebFilterChain securityWebFilter = this.http
			.authorizeExchange()
			.anyExchange().authenticated()
			.and()
			.formLogin().and()
			.build();

		WebTestClient webTestClient = WebTestClient
			.bindToController(new SecuredPageController(), new WebTestClientBuilder.Http200RestController())
			.webFilter(new WebFilterChainProxy(securityWebFilter))
			.build();

		WebDriver driver = WebTestClientHtmlUnitDriverBuilder
			.webTestClientSetup(webTestClient)
			.build();

		DefaultLoginPage loginPage = SecuredPage.to(driver, DefaultLoginPage.class)
			.assertAt();

		SecuredPage securedPage = loginPage.loginForm()
			.username("user")
			.password("password")
			.submit(SecuredPage.class);

		securedPage.assertAt();
	}

	@Test
	public void requestCacheNoOp() {
		SecurityWebFilterChain securityWebFilter = this.http
			.authorizeExchange()
				.anyExchange().authenticated()
				.and()
			.formLogin().and()
			.requestCache()
				.requestCache(NoOpServerRequestCache.getInstance())
				.and()
			.build();

		WebTestClient webTestClient = WebTestClient
			.bindToController(new SecuredPageController(), new WebTestClientBuilder.Http200RestController())
			.webFilter(new WebFilterChainProxy(securityWebFilter))
			.build();

		WebDriver driver = WebTestClientHtmlUnitDriverBuilder
			.webTestClientSetup(webTestClient)
			.build();

		DefaultLoginPage loginPage = SecuredPage.to(driver, DefaultLoginPage.class)
			.assertAt();

		HomePage securedPage = loginPage.loginForm()
			.username("user")
			.password("password")
			.submit(HomePage.class);

		securedPage.assertAt();
	}

	public static class SecuredPage {
		private WebDriver driver;

		public SecuredPage(WebDriver driver) {
			this.driver = driver;
		}

		public void assertAt() {
			assertThat(this.driver.getTitle()).isEqualTo("Secured");
		}

		static <T> T to(WebDriver driver, Class<T> page) {
			driver.get("http://localhost/secured");
			return PageFactory.initElements(driver, page);
		}
	}

	@Controller
	public static class SecuredPageController {
		@ResponseBody
		@GetMapping("/secured")
		public String login(ServerWebExchange exchange) {
			CsrfToken token = exchange.getAttribute(CsrfToken.class.getName());
			return
				"<!DOCTYPE html>\n"
					+ "<html lang=\"en\">\n"
					+ "  <head>\n"
					+ "    <title>Secured</title>\n"
					+ "  </head>\n"
					+ "  <body>\n"
					+ "    <h1>Secured</h1>\n"
					+ "  </body>\n"
					+ "</html>";
		}
	}
}
