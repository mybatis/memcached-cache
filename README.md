MyBatis Memcached Extension
===========================

[![Build Status](https://travis-ci.org/mybatis/memcached-cache.svg?branch=master)](https://travis-ci.org/mybatis/memcached-cache)
[![Coverage Status](https://coveralls.io/repos/mybatis/memcached-cache/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/memcached-cache?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis.caches/mybatis-memcached/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis.caches/mybatis-memcached)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis.caches/mybatis-memcached.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/caches/mybatis-memcached/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-memcached](http://mybatis.github.io/images/mybatis-logo.png)

MyBatis-Memcached extension Memcached support for MyBatis Cache.

Essentials
----------

* [See the docs](http://mybatis.github.io/memcached-cache/)

Releasing
---------

To release this library, use the maven release plugin.  If no memcache installed ensure to set the following:

-DskipTests -Darguments="-DskipTests"

Doing so will make sure the forked processes involved skip tests all together.

Typical maven release is done as follows where tests ignored.

mvn release:clean
mvn release:prepare -DskipTests -Darguments="-DskipTests"
mvn release:perform -DskipTests -Darguments="-DskipTests"
