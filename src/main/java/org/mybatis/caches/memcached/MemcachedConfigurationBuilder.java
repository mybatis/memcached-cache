/*
 *    Copyright 2010 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.caches.memcached;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Converter from the Config to a proper {@link MemcachedConfiguration}.
 *
 * @version $Id$
 */
final class MemcachedConfigurationBuilder {

    /**
     * This class instance.
     */
    private static final MemcachedConfigurationBuilder INSTANCE = new MemcachedConfigurationBuilder();

    /**
     *
     */
    private static final String MEMCACHED_RESOURCE = "/memcached.properties";

    /**
     * Return this class instance.
     *
     * @return this class instance.
     */
    public static MemcachedConfigurationBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * The setters used to extract properties.
     */
    private final List<AbstractPropertySetter<?>> settersRegistry = new ArrayList<AbstractPropertySetter<?>>();

    /**
     * Hidden constructor, this class can't be instantiated.
     */
    private MemcachedConfigurationBuilder() {
        this.settersRegistry.add(new StringPropertySetter("org.mybatis.caches.memcached.keyprefix", "keyPrefix", "_mybatis_"));

        this.settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.expiration", "expiration", 60 * 60 * 24 * 30));
        this.settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.timeout", "timeout", 5));
        this.settersRegistry.add(new TimeUnitSetter());

        this.settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.asyncget", "usingAsyncGet", false));
        this.settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.compression", "compressionEnabled", false));

        this.settersRegistry.add(new InetSocketAddressListPropertySetter());
        this.settersRegistry.add(new ConnectionFactorySetter());
    }

    /**
     * Pares the Config and builds a new {@link MemcachedConfiguration}.
     *
     * @param config the Config.
     * @return the converted {@link MemcachedConfiguration}.
     */
    public MemcachedConfiguration parseConfiguration() {
        Properties config = new Properties();

        // load the properties specified from /memcached.properties, if present
        InputStream input = this.getClass().getResourceAsStream(MEMCACHED_RESOURCE);
        if (input != null) {
            try {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading classpath property '"
                        + MEMCACHED_RESOURCE
                        + "', see nested exceptions", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }

        MemcachedConfiguration memcachedConfiguration = new MemcachedConfiguration();

        for (AbstractPropertySetter<?> setter : this.settersRegistry) {
            setter.set(config, memcachedConfiguration);
        }

        return memcachedConfiguration;
    }

}
