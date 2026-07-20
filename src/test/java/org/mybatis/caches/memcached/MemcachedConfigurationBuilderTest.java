/*
 *    Copyright 2012-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.caches.memcached;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.DefaultConnectionFactory;

import org.junit.jupiter.api.Test;

class MemcachedConfigurationBuilderTest {

  @Test
  void shouldParseDefaultConfigurationWhenPropertiesFileNotPresent() {
    MemcachedConfiguration configuration = MemcachedConfigurationBuilder.getInstance()
        .parseConfiguration(new ClassLoader(null) {
          @Override
          public InputStream getResourceAsStream(String name) {
            return null;
          }
        });

    assertEquals("_mybatis_", configuration.getKeyPrefix());
    assertEquals(60 * 60 * 24 * 30, configuration.getExpiration());
    assertEquals(5, configuration.getTimeout());
    assertEquals(TimeUnit.SECONDS, configuration.getTimeUnit());
    assertFalse(configuration.isUsingAsyncGet());
    assertFalse(configuration.isCompressionEnabled());
    assertFalse(configuration.isUsingSASL());
    assertEquals("", configuration.getUsername());
    assertEquals("", configuration.getPassword());
    assertInstanceOf(DefaultConnectionFactory.class, configuration.getConnectionFactory());
    assertEquals(1, configuration.getAddresses().size());
    assertEquals(11211, configuration.getAddresses().get(0).getPort());
  }

  @Test
  void shouldParseCustomConfigurationFromProperties() {
    String properties = String.join("\n", "org.mybatis.caches.memcached.keyprefix=test_",
        "org.mybatis.caches.memcached.username=u", "org.mybatis.caches.memcached.password=p",
        "org.mybatis.caches.memcached.expiration=120", "org.mybatis.caches.memcached.timeout=7",
        "org.mybatis.caches.memcached.timeoutunit=milliseconds", "org.mybatis.caches.memcached.asyncget=true",
        "org.mybatis.caches.memcached.compression=true", "org.mybatis.caches.memcached.sasl=true",
        "org.mybatis.caches.memcached.servers=localhost:11211,localhost:11212",
        "org.mybatis.caches.memcached.connectionfactory=" + PropertySettersTest.TestConnectionFactory.class.getName());

    MemcachedConfiguration configuration = MemcachedConfigurationBuilder.getInstance()
        .parseConfiguration(new ClassLoader(null) {
          @Override
          public InputStream getResourceAsStream(String name) {
            if ("memcached.properties".equals(name)) {
              return new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8));
            }
            return null;
          }
        });

    assertEquals("test_", configuration.getKeyPrefix());
    assertEquals("u", configuration.getUsername());
    assertEquals("p", configuration.getPassword());
    assertEquals(120, configuration.getExpiration());
    assertEquals(7, configuration.getTimeout());
    assertEquals(TimeUnit.MILLISECONDS, configuration.getTimeUnit());
    assertTrue(configuration.isUsingAsyncGet());
    assertTrue(configuration.isCompressionEnabled());
    assertTrue(configuration.isUsingSASL());
    assertEquals(2, configuration.getAddresses().size());
    assertInstanceOf(PropertySettersTest.TestConnectionFactory.class, configuration.getConnectionFactory());
  }

  @Test
  void shouldWrapIOExceptionWhenLoadingConfiguration() {
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> MemcachedConfigurationBuilder.getInstance().parseConfiguration(new ClassLoader(null) {
          @Override
          public InputStream getResourceAsStream(String name) {
            return new InputStream() {
              @Override
              public int read() throws IOException {
                throw new IOException("boom");
              }
            };
          }
        }));

    assertTrue(exception.getMessage().contains("An error occurred while reading classpath property"));
  }

  @Test
  void shouldImplementEqualsHashCodeAndToString() {
    MemcachedConfiguration left = new MemcachedConfiguration();
    MemcachedConfiguration right = new MemcachedConfiguration();
    DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory();

    left.setKeyPrefix("p");
    right.setKeyPrefix("p");
    left.setConnectionFactory(connectionFactory);
    right.setConnectionFactory(connectionFactory);
    left.setAddresses(java.util.List.of(new java.net.InetSocketAddress("localhost", 11211)));
    right.setAddresses(java.util.List.of(new java.net.InetSocketAddress("localhost", 11211)));
    left.setUsingAsyncGet(true);
    right.setUsingAsyncGet(true);
    left.setCompressionEnabled(true);
    right.setCompressionEnabled(true);
    left.setExpiration(1);
    right.setExpiration(1);
    left.setTimeout(2);
    right.setTimeout(2);
    left.setTimeUnit(TimeUnit.SECONDS);
    right.setTimeUnit(TimeUnit.SECONDS);
    left.setUsingSASL(true);
    right.setUsingSASL(true);
    left.setUsername("u");
    right.setUsername("u");
    left.setPassword("p");
    right.setPassword("p");

    assertEquals(left, right);
    assertEquals(left.hashCode(), right.hashCode());
    assertNotEquals(left, null);
    assertNotEquals(left, "x");
    assertTrue(left.toString().contains("MemcachedConfiguration"));
    assertEquals(31 * 1 + 0, MemcachedConfiguration.hash(1, 31, (Object) null));

    right.setTimeout(3);
    assertNotEquals(left, right);
  }
}
