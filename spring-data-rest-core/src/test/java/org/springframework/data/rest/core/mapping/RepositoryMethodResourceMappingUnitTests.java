/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.data.rest.core.mapping;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;

/**
 * Unit tests for {@link RepositoryMethodResourceMapping}.
 * 
 * @author Oliver Gierke
 */
public class RepositoryMethodResourceMappingUnitTests {

	RepositoryMetadata metadata = new DefaultRepositoryMetadata(PersonRepository.class);
	RepositoryCollectionResourceMapping resourceMapping = new RepositoryCollectionResourceMapping(metadata,
			RepositoryDetectionStrategies.DEFAULT);

	@Test
	public void defaultsMappingToMethodName() throws Exception {

		Method method = PersonRepository.class.getMethod("findByLastname", String.class);
		ResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getPath(), is(new Path("findByLastname")));
	}

	@Test
	public void usesConfiguredNameWithLeadingSlash() throws Exception {

		Method method = PersonRepository.class.getMethod("findByFirstname", String.class);
		ResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getPath(), is(new Path("bar")));
	}

	/**
	 * @see DATAREST-31
	 */
	@Test
	public void doesNotDiscoverAnyParametersIfNotAnnotated() throws Exception {

		Method method = PersonRepository.class.getMethod("findByLastname", String.class);
		MethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getParametersMetadata().getParameterNames(), is(emptyIterable()));
	}

	/**
	 * @see DATAREST-31
	 */
	@Test
	public void resolvesParameterNamesIfNotAnnotated() throws Exception {

		Method method = PersonRepository.class.getMethod("findByFirstname", String.class);
		MethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getParametersMetadata().getParameterNames(), hasSize(1));
		assertThat(mapping.getParametersMetadata().getParameterNames(), hasItem("firstname"));
	}

	/**
	 * @see DATAREST-229
	 */
	@Test
	public void considersPagingFinderAPagingResource() throws Exception {

		Method method = PersonRepository.class.getMethod("findByEmailAddress", String.class, Pageable.class);
		MethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.isPagingResource(), is(true));
	}

	@Test
	public void usesMethodNameAsRelFallbackEvenIfPathIsConfigured() throws Exception {

		Method method = PersonRepository.class.getMethod("findByEmailAddress", String.class, Pageable.class);
		MethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getRel(), is("findByEmailAddress"));
	}

	/**
	 * @see DATAREST-384
	 */
	@Test
	public void considersResourceSortableIfSortParameterIsPresent() throws Exception {

		Method method = PersonRepository.class.getMethod("findByEmailAddress", String.class, Sort.class);
		RepositoryMethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.isSortableResource(), is(true));

		method = PersonRepository.class.getMethod("findByEmailAddress", String.class, Pageable.class);
		mapping = getMappingFor(method);

		assertThat(mapping.isSortableResource(), is(false));
	}

	/**
	 * @see DATAREST-467
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void returnsDomainTypeAsProjectionSourceType() throws Exception {

		Method method = PersonRepository.class.getMethod("findByLastname", String.class);
		MethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getReturnedDomainType(), is(equalTo((Class) Person.class)));
	}

	/**
	 * @see DATAREST-699
	 */
	@Test
	public void doesNotIncludePageableAsParameter() throws Exception {

		Method method = PersonRepository.class.getMethod("findByLastname", String.class, Pageable.class);
		RepositoryMethodResourceMapping mapping = getMappingFor(method);

		assertThat(mapping.getParametersMetadata().getParameterNames(), not(hasItem("pageable")));
	}

	private RepositoryMethodResourceMapping getMappingFor(Method method) {
		return new RepositoryMethodResourceMapping(method, resourceMapping, metadata);
	}

	static class Person {}

	interface PersonRepository extends Repository<Person, Long> {

		Iterable<Person> findByLastname(String lastname);

		@RestResource(path = "/bar")
		Iterable<Person> findByFirstname(@Param("firstname") String firstname);

		@RestResource(path = "foo")
		Iterable<Person> findByEmailAddress(String email);

		@RestResource(path = "fooPaged")
		Page<Person> findByEmailAddress(String email, Pageable pageable);

		Page<Person> findByEmailAddress(String email, Sort pageable);

		int countByLastname(String lastname);

		// Simulate pageable detected as name on Java 8
		Page<Person> findByLastname(@Param("lastname") String lastname, @Param("pageable") Pageable pageable);
	}
}
