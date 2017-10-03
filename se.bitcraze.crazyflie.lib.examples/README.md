# Crazyflie Java library examples

This project contains the Java equivalent of the examples in the [Crazyflie Python client](https://github.com/bitcraze/crazyflie-clients-python).
 
Therefore they are heavily based on the Python implementation.

## Run the examples


### In Eclipse

1. Checkout or copy the following projects into your workspace
  * **se.bitcraze.crazyflie.lib**
  * **se.bitcraze.crazyflie.lib.examples**

2. In **se.bitcraze.crazyflie.lib** build the Crazyflie Java library by running
```
mvn clean install -DskipTests
```

3. Execute a test by right-clicking on it and selecting **Run As** -> **Java Application**

**Please make sure that a Crazyradio (PA) is connected and a Crazyflie 1.0 or 2.0 is switched on, when running the examples.**
(otherwise they will fail)  

### On the command line

TODO


## Dependencies

* [se.bitcraze.crazyflie.lib](https://github.com/fredg02/se.bitcraze.crazyflie.lib)


