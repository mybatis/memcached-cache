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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import net.spy.memcached.internal.OperationFuture;

import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * @author Simone Tripodi
 */
final class MemcachedClientWrapper {

  /**
   * This class log.
   */
  private static final Log LOG = LogFactory.getLog(MemcachedCache.class);

  private final MemcachedConfiguration configuration;

  private final MemcachedClient client;

  /**
   * Used to represent an object retrieved from Memcached along with its CAS information
   *
   * @author Weisz, Gustavo E.
   */
  private class ObjectWithCas {

    Object object;
    long cas;

    ObjectWithCas(Object object, long cas) {
      this.setObject(object);
      this.setCas(cas);
    }

    public Object getObject() {
      return object;
    }

    public void setObject(Object object) {
      this.object = object;
    }

    public long getCas() {
      return cas;
    }

    public void setCas(long cas) {
      this.cas = cas;
    }

  }

  public MemcachedClientWrapper() {
    configuration = MemcachedConfigurationBuilder.getInstance().parseConfiguration();
    try {
      if (configuration.isUsingSASL()) {
        AuthDescriptor ad = new AuthDescriptor(new String[] { "PLAIN" },
            new PlainCallbackHandler(configuration.getUsername(), configuration.getPassword()));
        client = new MemcachedClient(new ConnectionFactoryBuilder()
            .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY).setAuthDescriptor(ad).build(),
            configuration.getAddresses());
      } else {
        client = new MemcachedClient(configuration.getConnectionFactory(), configuration.getAddresses());
      }
    } catch (IOException e) {
      String message = "Impossible to instantiate a new memecached client instance, see nested exceptions";
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Running new Memcached client using " + configuration);
    }
  }

  /**
   * Converts the MyBatis object key in the proper string representation.
   *
   * @param key
   *          the MyBatis object key.
   *
   * @return the proper string representation.
   */
  private String toKeyString(final Object key) {
    // issue #1, key too long
    String keyString = configuration.getKeyPrefix() + StringUtils.sha1Hex(key.toString());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Object key '" + key + "' converted in '" + keyString + "'");
    }
    return keyString;
  }

  /**
   * @param key
   *
   * @return
   */
  public Object getObject(Object key) {
    String keyString = toKeyString(key);
    Object ret = retrieve(keyString);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrived object (" + keyString + ", " + ret + ")");
    }

    return ret;
  }

  /**
   * Return the stored group in Memcached identified by the specified key.
   *
   * @param groupKey
   *          the group key.
   *
   * @return the group if was previously stored, null otherwise.
   */
  private ObjectWithCas getGroup(String groupKey) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieving group with id '" + groupKey + "'");
    }

    ObjectWithCas groups = null;
    try {
      groups = retrieveWithCas(groupKey);
    } catch (Exception e) {
      LOG.error("Impossible to retrieve group '" + groupKey + "' see nested exceptions", e);
    }

    if (groups == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Group '" + groupKey + "' not previously stored");
      }
      return null;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("retrieved group '" + groupKey + "' with values " + groups);
    }

    return groups;
  }

  /**
   * @param keyString
   *
   * @return
   *
   * @throws Exception
   */
  private Object retrieve(final String keyString) {
    Object retrieved = null;

    if (configuration.isUsingAsyncGet()) {
      Future<Object> future;
      if (configuration.isCompressionEnabled()) {
        future = client.asyncGet(keyString, new CompressorTranscoder());
      } else {
        future = client.asyncGet(keyString);
      }

      try {
        retrieved = future.get(configuration.getTimeout(), configuration.getTimeUnit());
      } catch (Exception e) {
        future.cancel(false);
        throw new CacheException(e);
      }
    } else {
      if (configuration.isCompressionEnabled()) {
        retrieved = client.get(keyString, new CompressorTranscoder());
      } else {
        retrieved = client.get(keyString);
      }
    }

    return retrieved;
  }

  /**
   * Retrieves an object along with its cas using the given key
   *
   * @param keyString
   *
   * @return
   *
   * @throws Exception
   */
  private ObjectWithCas retrieveWithCas(final String keyString) {
    CASValue<Object> retrieved = null;

    if (configuration.isUsingAsyncGet()) {
      Future<CASValue<Object>> future;
      if (configuration.isCompressionEnabled()) {
        future = client.asyncGets(keyString, new CompressorTranscoder());
      } else {
        future = client.asyncGets(keyString);
      }

      try {
        retrieved = future.get(configuration.getTimeout(), configuration.getTimeUnit());
      } catch (Exception e) {
        future.cancel(false);
        throw new CacheException(e);
      }
    } else {
      if (configuration.isCompressionEnabled()) {
        retrieved = client.gets(keyString, new CompressorTranscoder());
      } else {
        retrieved = client.gets(keyString);
      }
    }

    if (retrieved == null) {
      return null;
    }

    return new ObjectWithCas(retrieved.getValue(), retrieved.getCas());
  }

  @SuppressWarnings("unchecked")
  public void putObject(Object key, Object value, String id) {
    String keyString = toKeyString(key);
    String groupKey = toKeyString(id);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Putting object (" + keyString + ", " + value + ")");
    }

    storeInMemcached(keyString, value);

    // add namespace key into memcached
    // Optimistic lock approach...
    boolean jobDone = false;

    while (!jobDone) {
      ObjectWithCas group = getGroup(groupKey);
      Set<String> groupValues;

      if (group == null || group.getObject() == null) {
        groupValues = new HashSet<String>();
        groupValues.add(keyString);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Insert/Updating object (" + groupKey + ", " + groupValues + ")");
        }

        jobDone = tryToAdd(groupKey, groupValues);
      } else {
        groupValues = (Set<String>) group.getObject();
        groupValues.add(keyString);

        jobDone = storeInMemcached(groupKey, group);
      }
    }
  }

  /**
   * Stores an object identified by a key in Memcached.
   *
   * @param keyString
   *          the object key
   * @param value
   *          the object has to be stored.
   */
  private void storeInMemcached(String keyString, Object value) {
    if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
      throw new CacheException(
          "Object of type '" + value.getClass().getName() + "' that's non-serializable is not supported by Memcached");
    }

    if (configuration.isCompressionEnabled()) {
      client.set(keyString, configuration.getExpiration(), value, new CompressorTranscoder());
    } else {
      client.set(keyString, configuration.getExpiration(), value);
    }
  }

  /**
   * Tries to update an object value in memcached considering the cas validation.
   * <p>
   * Returns true if the object passed the cas validation and was modified.
   *
   * @param keyString
   * @param value
   *
   * @return
   */
  private boolean storeInMemcached(String keyString, ObjectWithCas value) {
    if (value != null && value.getObject() != null
        && !Serializable.class.isAssignableFrom(value.getObject().getClass())) {
      throw new CacheException("Object of type '" + value.getObject().getClass().getName()
          + "' that's non-serializable is not supported by Memcached");
    }

    CASResponse response;

    if (configuration.isCompressionEnabled()) {
      response = client.cas(keyString, value.getCas(), value.getObject(), new CompressorTranscoder());
    } else {
      response = client.cas(keyString, value.getCas(), value.getObject());
    }

    return (response.equals(CASResponse.OBSERVE_MODIFIED) || response.equals(CASResponse.OK));
  }

  /**
   * Tries to store an object identified by a key in Memcached.
   * <p>
   * Will fail if the object already exists.
   *
   * @param keyString
   * @param value
   *
   * @return
   */
  private boolean tryToAdd(String keyString, Object value) {
    if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
      throw new CacheException(
          "Object of type '" + value.getClass().getName() + "' that's non-serializable is not supported by Memcached");
    }

    boolean done;
    OperationFuture<Boolean> result;

    if (configuration.isCompressionEnabled()) {
      result = client.add(keyString, configuration.getExpiration(), value, new CompressorTranscoder());
    } else {
      result = client.add(keyString, configuration.getExpiration(), value);
    }

    try {
      done = result.get();
    } catch (InterruptedException e) {
      done = false;
    } catch (ExecutionException e) {
      done = false;
    }

    return done;
  }

  public Object removeObject(Object key) {
    String keyString = toKeyString(key);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing object '" + keyString + "'");
    }

    Object result = getObject(key);
    if (result != null) {
      client.delete(keyString);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public void removeGroup(String id) {
    String groupKey = toKeyString(id);

    // remove namespace key into memcached
    // Optimistic lock approach...
    boolean jobDone = false;

    while (!jobDone) {
      ObjectWithCas group = getGroup(groupKey);
      Set<String> groupValues;

      if (group == null || group.getObject() == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("No need to flush cached entries for group '" + id + "' because is empty");
        }
        return;
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Flushing keys: " + group);
      }

      groupValues = (Set<String>) group.getObject();

      for (String key : groupValues) {
        client.delete(key);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Flushing group: " + groupKey);
      }

      groupValues = (Set<String>) group.getObject();
      groupValues.clear();

      jobDone = storeInMemcached(groupKey, group);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    client.shutdown(configuration.getTimeout(), configuration.getTimeUnit());
    super.finalize();
  }

}
