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

/**
 * Setter from String to Boolean representation.
 *
 * @version $Id$
 */
final class BooleanPropertySetter extends AbstractPropertySetter<Boolean> {

    /**
     * Instantiates a String to Boolean setter.
     *
     * @param propertyKey the OSCache Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public BooleanPropertySetter(final String propertyKey, final String propertyName, final Boolean defaultValue) {
        super(propertyKey, propertyName, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean convert(String property) throws Throwable {
        return Boolean.valueOf(property);
    }

}
