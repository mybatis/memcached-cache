/*
 *    Copyright 2012 The MyBatis Team
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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Converts a keyed property string in the Config to a proper Java
 * object representation.
 *
 * @version $Id$
 * @param <T>
 */
abstract class AbstractPropertySetter<T> {

    /**
     * 'propertyName'='writermethod' index of {@link MemcachedConfiguration}
     * properties.
     */
    private static Map<String, Method> WRITERS = new HashMap<String, Method>();

    static {
        try {
            BeanInfo memcachedConfigInfo = Introspector.getBeanInfo(MemcachedConfiguration.class);
            for (PropertyDescriptor descriptor : memcachedConfigInfo.getPropertyDescriptors()) {
                WRITERS.put(descriptor.getName(), descriptor.getWriteMethod());
            }
        } catch (IntrospectionException e) {
            // handle quietly
        }
    }

    /**
     * The Config property key.
     */
    private final String propertyKey;

    /**
     * The {@link MemcachedConfiguration} property name.
     */
    private final String propertyName;

    /**
     * The {@link MemcachedConfiguration} property method writer.
     */
    private final Method propertyWriterMethod;

    /**
     * The default value used if something goes wrong during the conversion or
     * the property is not set in the config.
     */
    private final T defaultValue;

    /**
     * Build a new property setter.
     *
     * @param propertyKey the Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public AbstractPropertySetter(final String propertyKey, final String propertyName, final T defaultValue) {
        this.propertyKey = propertyKey;
        this.propertyName = propertyName;

        this.propertyWriterMethod = WRITERS.get(propertyName);
        if (this.propertyWriterMethod == null) {
            throw new RuntimeException("Class '"
                    + MemcachedConfiguration.class.getName()
                    + "' doesn't contain a property '"
                    + propertyName
                    + "'");
        }

        this.defaultValue = defaultValue;
    }

    /**
     * Extract a property from the, converts and puts it to the
     * {@link MemcachedConfiguration}.
     *
     * @param config the Config
     * @param memcachedConfiguration the {@link MemcachedConfiguration}
     */
    public final void set(Properties config, MemcachedConfiguration memcachedConfiguration) {
        String propertyValue = config.getProperty(propertyKey);
        T value;

        try {
            value = this.convert(propertyValue);
            if (value == null) {
                value = defaultValue;
            }
        } catch (Throwable e) {
            value = defaultValue;
        }

        try {
            propertyWriterMethod.invoke(memcachedConfiguration, value);
        } catch (Exception e) {
            throw new RuntimeException("Impossible to set property '"
                    + propertyName
                    + "' with value '"
                    + value
                    + "', extracted from ('"
                    + propertyKey
                    + "'="
                    + propertyValue
                    + ")", e);
        }
    }

    /**
     * Convert a string representation to a proper Java Object.
     *
     * @param value the value has to be converted.
     * @return the converted value.
     * @throws Throwable if any error occurs.
     */
    protected abstract T convert(String value) throws Throwable;

}
