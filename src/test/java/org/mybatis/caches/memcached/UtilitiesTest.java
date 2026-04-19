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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import net.spy.memcached.CachedData;

import org.apache.ibatis.cache.CacheException;
import org.junit.jupiter.api.Test;

class UtilitiesTest {

  @Test
  void shouldComputeSha1Hex() {
    assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", StringUtils.sha1Hex("abc"));
  }

  @Test
  void shouldRejectNullWhenComputingSha1Hex() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> StringUtils.sha1Hex(null));
    assertEquals("data must not be null", exception.getMessage());
  }

  @Test
  void shouldEncodeAndDecodeCompressedPayload() {
    CompressorTranscoder transcoder = new CompressorTranscoder();
    TestPayload payload = new TestPayload("v");

    CachedData encoded = transcoder.encode(payload);

    assertEquals(3, encoded.getFlags());
    assertEquals(payload, transcoder.decode(encoded));
    assertFalse(transcoder.asyncDecode(encoded));
    assertEquals(Integer.MAX_VALUE, transcoder.getMaxSize());
  }

  @Test
  void shouldThrowCacheExceptionWhenDecodeFails() {
    CompressorTranscoder transcoder = new CompressorTranscoder();
    CachedData invalid = new CachedData(0, "not-gzip".getBytes(StandardCharsets.UTF_8), CachedData.MAX_SIZE);

    assertThrows(CacheException.class, () -> transcoder.decode(invalid));
  }

  @Test
  void shouldThrowCacheExceptionWhenEncodeFailsForNonSerializableObject() {
    CompressorTranscoder transcoder = new CompressorTranscoder();

    assertThrows(CacheException.class, () -> transcoder.encode(new Object()));
  }

  static final class TestPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    TestPayload(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof TestPayload)) {
        return false;
      }
      return value.equals(((TestPayload) obj).value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }
}
