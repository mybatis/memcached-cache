MyBatis Memcached Extension
===========================

[![Java CI](https://github.com/mybatis/memcached-cache/actions/workflows/ci.yaml/badge.svg)](https://github.com/mybatis/memcached-cache/actions/workflows/ci.yaml)
[![Coverage Status](https://coveralls.io/repos/mybatis/memcached-cache/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/memcached-cache?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/org.mybatis.caches/mybatis-memcached?label=maven%20central)](https://central.sonatype.com/artifact/org.mybatis.caches/mybatis-memcached)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-memcached](https://mybatis.org/images/mybatis-logo.png)

MyBatis-Memcached extension Memcached support for MyBatis Cache.

Essentials
----------

* [See the docs](https://mybatis.org/memcached-cache/)

Releasing
---------

To release this library, use the maven release plugin.

If no memcache installed ensure to set the following profile ```-PnoTest```.

Typical maven release is done as follows where tests ignored.

mvn release:clean
mvn release:prepare -PnoTest
mvn release:perform -PnoTest
