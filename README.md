# Crazyflie Java library

The intention of this library is to be the Java equivalent of
the **cflib** contained in the [Crazyflie Python client](https://github.com/bitcraze/crazyflie-clients-python).
 
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
mvn clean install
```

The compiled JAR can then be found in the **target** directory (e.g. ``se.bitcraze.crazyflie.lib-0.0.1-SNAPSHOT.jar``).


Dependencies
------------

* [usb4java](http://usb4java.org)
* [Jackson](https://github.com/FasterXML/jackson)
* [SLF4J](http://www.slf4j.org)
* [JUnit](http://junit.org)

Examples
--------

Examples are included in the project **se.bitcraze.crazyflie.lib.examples**.


Known issues
------------

* Bootloader not yet supported 
* Debug driver incomplete (Tests currently need Crazyflie/Crazyradio to run successfully)
* Too many Thread.sleep()s ;)



