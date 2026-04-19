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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HOW TO RUN THE TEST
 * <p>
 * Install memcached:
 * <ul>
 * <li>on ubuntu: open the shell and type <code>sudo apt-get install memcached</code></li>
 * <li>on mac os x: open the terminal and type <code>sudo port install memcached</code></li>
 * </ul>
 * launch <code>mvn test</code>.
 */
public final class MemcachedTestCase {

  private static final String DEFAULT_ID = "MEMCACHED";

  private MemcachedCache cache;

  @BeforeEach
  public void newCache() {
    cache = new MemcachedCache(DEFAULT_ID);
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    for (int i = 0; i < 100; i++) {
      cache.putObject(i, i);
      assertEquals(i, cache.getObject(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    cache.putObject(0, 0);
    assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Object o = cache.getObject(0);
    assertNull(o);
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    assertNotNull(cache.getObject(0));
    assertNotNull(cache.getObject(4));
    cache.clear();
    assertNull(cache.getObject(0));
    assertNull(cache.getObject(4));
  }

  @Test
  public void shouldAcceptAKeyBiggerThan250() {
    char[] keyChar = new char[1024];
    Arrays.fill(keyChar, 'X');
    String key = new String(keyChar);
    String value = "value";
    cache.putObject(key, value);
    assertEquals(value, cache.getObject(key));
  }

  /**
   * The group should contain all keys even if race conditions are present
   */
  @Test
  public void groupShouldContainAllKeys() {

    long threadTestCount = 20;
    long valuesPerThread = 100;
    String mapperName = "GroupTest";

    MemcachedCache newCache = new MemcachedCache(mapperName);
    newCache.clear();

    /*
     * Create, run & wait 'threadTestCount' concurrent threads
     */
    long i = 0;

    List<Thread> threads = new ArrayList<Thread>();

    while (i < threadTestCount) {
      Thread thread = new GroupTestThread(newCache, valuesPerThread);
      thread.start();
      threads.add(thread);
      i++;
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
      }
    }

    /*
     * Since each thread will create 'threadTestCount' values there should be ( 'valuesPerThread' * 'threadTestCount' )
     * elements in the group, independently of any race condition.
     */
    @SuppressWarnings("unchecked")
    Set<String> keys = (Set<String>) cache.getObject(newCache.getId());
    assertNotNull(keys);

    long count = 0;

    for (@SuppressWarnings("unused")
    String key : keys) {
      count++;
    }

    assertEquals(count, valuesPerThread * threadTestCount);
  }

}
