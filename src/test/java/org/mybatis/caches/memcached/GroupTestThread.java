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

import java.util.Random;
import java.util.UUID;

/**
 * Thread to test race conditions behavior
 *
 * @author Weisz, Gustavo E.
 */
public class GroupTestThread extends Thread {

  private long itemsToCreate;
  private MemcachedCache cache;

  public GroupTestThread(MemcachedCache cache, long itemsToCreate) {
    this.setCache(cache);
    this.setItemsToCreate(itemsToCreate);
  }

  public long getItemsToCreate() {
    return itemsToCreate;
  }

  public void setItemsToCreate(long itemsToCreate) {
    this.itemsToCreate = itemsToCreate;
  }

  public MemcachedCache getCache() {
    return cache;
  }

  public void setCache(MemcachedCache cache) {
    this.cache = cache;
  }

  @Override
  public void run() {
    Random random = new Random();

    for (int i = 0; i < itemsToCreate; i++) {
      cache.putObject(UUID.randomUUID().toString(), "TEST");

      // Wait between 1 and 10 milliseconds between each insertion
      try {
        Thread.sleep(random.nextInt(10) + 1);
      } catch (InterruptedException e) {
      }
    }
  }

}
