# Crazyflie Java library

The intention of this library is to be the Java equivalent of
[crazyflie-lib-python](https://github.com/bitcraze/crazyflie-lib-python), a Python based library that is used by the [Crazyflie Python client](https://github.com/bitcraze/crazyflie-clients-python).

Therefore this library is heavily based on the Python implementation.
Some components are almost identical, some differ more to accommodate different
programming concepts in Python and Java.

Over time the Java library will be adapted to make use of Java specific
features like Lambda expressions, etc.

This library should make it easy to implement Java based clients for the
Crazyflie, for example on Android or as a Eclipse RCP application.

Features
--------

* Abstract USB interface that allows to use different USB implementations,
eg. pure Java and Android

Installation
------------

It can be installed in Eclipse from the P2 update site:

[https://fredg02.github.io/se.bitcraze.crazyflie.lib/p2](https://fredg02.github.io/se.bitcraze.crazyflie.lib/p2)

Integration
-----------

The Crazyflie Java library is a Tycho project.

Run normal Maven build
```
mvn clean verify -B -f se.bitcraze.crazyflie.lib-parent/pom.xml -DskipTests
```

After the Maven build is completed a compiled JAR can be found in the ```se.bitcraze.crazyflie.lib/target``` directory, e.g. ```se.bitcraze.crazyflie.lib-0.0.1-SNAPSHOT.jar```.

The Tycho build also generates a P2 repository that can be used for Eclipse Plug-in based projects. It can be found in the ```se.bitcraze.crazyflie.lib-repository/target``` directory, e.g. ```se.bitcraze.crazyflie.lib-repository-0.0.1-SNAPSHOT.zip```.


Dependencies
------------

* [usb4java](http://usb4java.org)
* [Jackson](https://github.com/FasterXML/jackson)
* [SLF4J](http://www.slf4j.org)
* [JUnit](http://junit.org)


Tests
-----

[![Test Results](https://gist.github.com/fredg02/fa43ebf03c3a5bdaaec3aa3f899de280/raw/badge.svg)](https://gist.github.com/fredg02/fa43ebf03c3a5bdaaec3aa3f899de280/raw/badge.svg)

JUnit tests can be executed from the command line with:
```
mvn clean verify -B -f se.bitcraze.crazyflie.lib-parent/pom.xml
```

Or from within Eclipse by running ``/se.bitcraze.crazyflie.lib/src/test/java/se/bitcraze/crazyflie/lib/AllTests.java`` as a JUnit test.

**Please make sure that a Crazyradio (PA) is connected and a Crazyflie 1.0 or 2.x is switched on, when running the tests** (otherwise they will fail).


Examples
--------

Examples are included in the ```se.bitcraze.crazyflie.lib.examples``` directory.


Known issues
------------

* Bootloader not fully supported yet
* Debug driver incomplete (Tests currently need Crazyflie/Crazyradio to run successfully)
* Too many Thread.sleep()s ;)
