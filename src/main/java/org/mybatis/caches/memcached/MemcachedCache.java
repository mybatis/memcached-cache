/*
 *    Copyright 2012-2022 the original author or authors.
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

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

/**
 * The Memcached-based Cache implementation.
 *
 * @author Simone Tripodi
 */
public final class MemcachedCache implements Cache {

  private static final MemcachedClientWrapper MEMCACHED_CLIENT = new MemcachedClientWrapper();

  /**
   * The {@link ReadWriteLock}.
   */
  private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

  /**
   * The cache id.
   */
  private final String id;

  /**
   * Builds a new Memcached-based Cache.
   *
   * @param id
   *          the Mapper id.
   */
  public MemcachedCache(final String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    MEMCACHED_CLIENT.removeGroup(this.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getObject(Object key) {
    return MEMCACHED_CLIENT.getObject(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ReadWriteLock getReadWriteLock() {
    return this.readWriteLock;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSize() {
    return Integer.MAX_VALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void putObject(Object key, Object value) {
    MEMCACHED_CLIENT.putObject(key, value, this.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object removeObject(Object key) {
    return MEMCACHED_CLIENT.removeObject(key);
  }

}
