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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import org.apache.ibatis.cache.CacheException;

/**
 * The Transcoder that compress and decompress the stored objects using the GZIP compression algorithm.
 *
 * @author Simone Tripodi
 */
final class CompressorTranscoder implements Transcoder<Object> {

  /**
   * The serialized and compressed flag.
   */
  private static final int SERIALIZED_COMPRESSED = 3;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean asyncDecode(final CachedData cachedData) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(final CachedData cachedData) {
    byte[] buffer = cachedData.getData();

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    GZIPInputStream gzis = null;
    ObjectInputStream ois = null;
    Object ret = null;

    try {
      gzis = new GZIPInputStream(bais);
      ois = new ObjectInputStream(gzis);
      ret = ois.readObject();
    } catch (Exception e) {
      throw new CacheException("Impossible to decompress cached object, see nested exceptions", e);
    } finally {
      closeQuietly(ois);
      closeQuietly(gzis);
      closeQuietly(bais);
    }
    return ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CachedData encode(final Object object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzops = null;
    ObjectOutputStream oos = null;

    try {
      gzops = new GZIPOutputStream(baos);
      oos = new ObjectOutputStream(gzops);
      oos.writeObject(object);
    } catch (IOException e) {
      throw new CacheException("Impossible to compress object [" + object + "], see nested exceptions", e);
    } finally {
      closeQuietly(oos);
      closeQuietly(gzops);
      closeQuietly(baos);
    }

    byte[] buffer = baos.toByteArray();
    return new CachedData(SERIALIZED_COMPRESSED, buffer, CachedData.MAX_SIZE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMaxSize() {
    return Integer.MAX_VALUE;
  }

  /**
   * Unconditionally close an {@link InputStream}.
   *
   * @param closeable
   *          the InputStream to close, may be null or already closed.
   */
  private static void closeQuietly(final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // do nothing
      }
    }
  }

}
