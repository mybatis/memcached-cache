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

/**
 * Setter from String to Integer representation.
 *
 * @version $Id$
 */
final class IntegerPropertySetter extends AbstractPropertySetter<Integer> {

    /**
     * Instantiates a String to Integer setter.
     *
     * @param propertyKey the Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public IntegerPropertySetter(final String propertyKey, final String propertyName, final Integer defaultValue) {
        super(propertyKey, propertyName, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer convert(String property) throws Throwable {
        return Integer.valueOf(property);
    }

}
