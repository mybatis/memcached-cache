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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.DefaultConnectionFactory;

import org.junit.jupiter.api.Test;

class PropertySettersTest {

  @Test
  void shouldApplyStringIntegerBooleanAndTimeUnitProperties() {
    MemcachedConfiguration configuration = new MemcachedConfiguration();
    Properties properties = new Properties();
    properties.setProperty("k", "prefix");
    properties.setProperty("i", "10");
    properties.setProperty("b", "true");
    properties.setProperty("org.mybatis.caches.memcached.timeoutunit", "minutes");

    new StringPropertySetter("k", "keyPrefix", "default").set(properties, configuration);
    new IntegerPropertySetter("i", "timeout", 5).set(properties, configuration);
    new BooleanPropertySetter("b", "usingAsyncGet", false).set(properties, configuration);
    new TimeUnitSetter().set(properties, configuration);

    assertEquals("prefix", configuration.getKeyPrefix());
    assertEquals(10, configuration.getTimeout());
    assertTrue(configuration.isUsingAsyncGet());
    assertEquals(TimeUnit.MINUTES, configuration.getTimeUnit());
  }

  @Test
  void shouldUseDefaultValueWhenConversionFailsOrPropertyMissing() {
    MemcachedConfiguration configuration = new MemcachedConfiguration();
    Properties properties = new Properties();
    properties.setProperty("i", "not-a-number");
    properties.setProperty("org.mybatis.caches.memcached.timeoutunit", "invalid");

    new IntegerPropertySetter("i", "timeout", 5).set(properties, configuration);
    new TimeUnitSetter().set(properties, configuration);

    assertEquals(5, configuration.getTimeout());
    assertEquals(TimeUnit.SECONDS, configuration.getTimeUnit());
  }

  @Test
  void shouldSetSocketListAndConnectionFactory() {
    MemcachedConfiguration configuration = new MemcachedConfiguration();
    Properties properties = new Properties();
    properties.setProperty("org.mybatis.caches.memcached.servers", "127.0.0.1:11211,localhost:11212");
    properties.setProperty("org.mybatis.caches.memcached.connectionfactory", TestConnectionFactory.class.getName());

    new InetSocketAddressListPropertySetter().set(properties, configuration);
    new ConnectionFactorySetter().set(properties, configuration);

    List<InetSocketAddress> addresses = configuration.getAddresses();
    assertEquals(2, addresses.size());
    assertEquals(11211, addresses.get(0).getPort());
    assertEquals(11212, addresses.get(1).getPort());
    assertInstanceOf(TestConnectionFactory.class, configuration.getConnectionFactory());
  }

  @Test
  void shouldFallBackToDefaultConnectionFactoryForInvalidType() {
    MemcachedConfiguration configuration = new MemcachedConfiguration();
    Properties properties = new Properties();
    properties.setProperty("org.mybatis.caches.memcached.connectionfactory", String.class.getName());

    new ConnectionFactorySetter().set(properties, configuration);

    assertInstanceOf(DefaultConnectionFactory.class, configuration.getConnectionFactory());
  }

  @Test
  void shouldThrowWhenSetterTargetsUnknownConfigurationProperty() {
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> new StringPropertySetter("k", "unknownProperty", "default"));

    assertTrue(exception.getMessage().contains("doesn't contain a property"));
  }

  @Test
  void shouldExposeDummyLockBehavior() throws InterruptedException {
    DummyReadWriteLock readWriteLock = new DummyReadWriteLock();

    assertEquals(readWriteLock.readLock(), readWriteLock.writeLock());
    assertTrue(readWriteLock.readLock().tryLock());
    assertTrue(readWriteLock.readLock().tryLock(1, TimeUnit.MILLISECONDS));
    readWriteLock.readLock().lock();
    readWriteLock.readLock().unlock();
    readWriteLock.readLock().lockInterruptibly();
    assertNull(readWriteLock.readLock().newCondition());
  }

  static final class TestConnectionFactory extends DefaultConnectionFactory {
  }
}
