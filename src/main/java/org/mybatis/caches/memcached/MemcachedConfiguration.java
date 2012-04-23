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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ConnectionFactory;

/**
 * The Memcached client configuration.
 *
 * @version $Id$
 */
final class MemcachedConfiguration {

    /**
     * The key prefix.
     */
    private String keyPrefix;

    /**
     * The Connection Factory used to establish the connection to Memcached
     * server(s).
     */
    private ConnectionFactory connectionFactory;

    /**
     * The Memcached servers.
     */
    private List<InetSocketAddress> addresses;

    /**
     * The flag to switch from sync to async Memcached get.
     */
    private boolean usingAsyncGet;

    /**
     * Compression enabled flag.
     */
    private boolean compressionEnabled;

    /**
     * The Memcached entries expiration time.
     */
    private int expiration;

    /**
     * The Memcached connection timeout when using async get.
     */
    private int timeout;

    /**
     * The Memcached timeout unit when using async get.
     */
    private TimeUnit timeUnit;

    /**
     * @return the keyPrefix
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * @param keyPrefix the keyPrefix to set
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return the addresses
     */
    public List<InetSocketAddress> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(List<InetSocketAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the usingAsyncGet
     */
    public boolean isUsingAsyncGet() {
        return usingAsyncGet;
    }

    /**
     * @param usingAsyncGet the usingAsyncGet to set
     */
    public void setUsingAsyncGet(boolean usingAsyncGet) {
        this.usingAsyncGet = usingAsyncGet;
    }

    /**
     * @return the compressionEnabled
     */
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    /**
     * @param compressionEnabled the compressionEnabled to set
     */
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * @return the expiration
     */
    public int getExpiration() {
        return expiration;
    }

    /**
     * @param expiration the expiration to set
     */
    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the timeUnit
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * @param timeUnit the timeUnit to set
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((addresses == null) ? 0 : addresses.hashCode());
        result = prime * result + (compressionEnabled ? 1231 : 1237);
        result = prime
                * result
                + ((connectionFactory == null) ? 0 : connectionFactory
                        .hashCode());
        result = prime * result + expiration;
        result = prime * result
                + ((keyPrefix == null) ? 0 : keyPrefix.hashCode());
        result = prime * result
                + ((timeUnit == null) ? 0 : timeUnit.hashCode());
        result = prime * result + timeout;
        result = prime * result + (usingAsyncGet ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MemcachedConfiguration other = (MemcachedConfiguration) obj;
        if (addresses == null) {
            if (other.addresses != null)
                return false;
        } else if (!addresses.equals(other.addresses))
            return false;
        if (compressionEnabled != other.compressionEnabled)
            return false;
        if (connectionFactory == null) {
            if (other.connectionFactory != null)
                return false;
        } else if (!connectionFactory.equals(other.connectionFactory))
            return false;
        if (expiration != other.expiration)
            return false;
        if (keyPrefix == null) {
            if (other.keyPrefix != null)
                return false;
        } else if (!keyPrefix.equals(other.keyPrefix))
            return false;
        if (timeUnit == null) {
            if (other.timeUnit != null)
                return false;
        } else if (!timeUnit.equals(other.timeUnit))
            return false;
        if (timeout != other.timeout)
            return false;
        if (usingAsyncGet != other.usingAsyncGet)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MemcachedConfiguration [addresses=" + addresses
                + ", compressionEnabled=" + compressionEnabled
                + ", connectionFactory=" + connectionFactory + ", expiration="
                + expiration + ", keyPrefix=" + keyPrefix + ", timeUnit="
                + timeUnit + ", timeout=" + timeout + ", usingAsyncGet="
                + usingAsyncGet + "]";
    }

}
