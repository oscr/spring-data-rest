/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.data.rest.webmvc.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link UriUtils}.
 * 
 * @author Mark Paluch
 */
public class UriUtilsUnitTests {

	/**
	 * @see DATAREST-910
	 */
	@Test
	public void pathSegmentsShouldDiscoverPathUsingMethodMapping() throws Exception {

		Method method = ClassUtils.getMethod(MappedMethod.class, "method");
		List<String> pathSegments = UriUtils.getPathSegments(method);

		assertThat(pathSegments, hasItems("hello", "world"));
	}

	/**
	 * @see DATAREST-910
	 */
	@Test
	public void pathSegmentsShouldDiscoverPathUsingTypeAndMethodMapping() throws Exception {

		Method method = ClassUtils.getMethod(MappedClassAndMethod.class, "method");
		List<String> pathSegments = UriUtils.getPathSegments(method);

		assertThat(pathSegments, hasItems("hello", "world"));
	}

	static class MappedMethod {

		@RequestMapping("hello/world")
		public void method() {}
	}

	@RequestMapping("hello")
	static class MappedClassAndMethod {

		@RequestMapping("world")
		public void method() {}
	}
}
