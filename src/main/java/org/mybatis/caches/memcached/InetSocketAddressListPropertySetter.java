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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import net.spy.memcached.AddrUtil;

/**
 * Setter from String to list of InetSocketAddress representation.
 *
 * @author Simone Tripodi
 */
final class InetSocketAddressListPropertySetter extends AbstractPropertySetter<List<InetSocketAddress>> {

  private static final List<InetSocketAddress> SOCKET_LIST = Arrays.asList(new InetSocketAddress("localhost", 11211));

  /**
   * Instantiates a String to List&lt;InetSocketAddress&gt; setter.
   */
  public InetSocketAddressListPropertySetter() {
    super("org.mybatis.caches.memcached.servers", "addresses", SOCKET_LIST);
  }

  @Override
  protected List<InetSocketAddress> convert(String property) throws Exception {
    return AddrUtil.getAddresses(property);
  }

}
