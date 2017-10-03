# Crazyflie Java library

![Travis CI build status](https://travis-ci.org/fredg02/se.bitcraze.crazyflie.lib.svg?branch=master)

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


Integration
-----------

The Crazyflie Java library is a [Maven](https://maven.apache.org) project and can therefore be compiled
into a simple JAR with the following command:
```
mvn clean verify -DskipTests
```

The compiled JAR can then be found in the **target** directory (e.g. ``se.bitcraze.crazyflie.lib-0.0.1-SNAPSHOT.jar``).


Dependencies
------------

* [usb4java](http://usb4java.org)
* [Jackson](https://github.com/FasterXML/jackson)
* [SLF4J](http://www.slf4j.org)
* [JUnit](http://junit.org)


Tests
-----

JUnit tests can be executed from the command line with:
```
mvn clean verify
```

Or from within Eclipse by running ``/se.bitcraze.crazyflie.lib/src/test/java/se/bitcraze/crazyflie/lib/AllTests.java`` as a JUnit test. 

**Please make sure that a Crazyradio (PA) is connected and a Crazyflie 1.0 or 2.0 is switched on, when running the tests** (otherwise they will fail).


Examples
--------

Examples are included in the project [se.bitcraze.crazyflie.lib.examples](https://github.com/fredg02/se.bitcraze.crazyflie.lib.examples).


Known issues
------------

* Bootloader not fully supported yet 
* Debug driver incomplete (Tests currently need Crazyflie/Crazyradio to run successfully)
* Too many Thread.sleep()s ;)



