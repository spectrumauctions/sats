# Spectrum Auction Test Suite: Optimization API (sats-opt)

### Getting Started
This getting started guide helps you to set up `sats-opt` to use it as a library in your own java project afterwards. 
As opposed to `sats-opt` extends ``sats-core`` and adds winner determination problem (WDP) solvers to the list of provided features.
As the WDP solvers require more external libraries, we recommend to just use ``sats-core``, should you not (yet) want to use the WDP solvers. An upgrade is easily possible any time.

Should you not want to use SATS it in your own java code, but want to create problem instances in a simpler way, you may want to use the command line tool (`sats-clt`) or the web application (`sats-web`) instead. See 
http://spectrumauctions.org for more information.
##### Prerequisites
Make sure to have `git`, `maven`, `JDK 1.8 (or later)` and `cplex` installed. 
* Include your local cplex JAR in your maven repository by the following command:
```
mvn install:install-file -Dfile=<path-to-cplex-jar> -DgroupId=cplex -DartifactId=cplex -Dversion=12.6 -Dpackaging=jar
```
* Note: If you have an early JDK 1.8 version and it doesn't compile, please update to a newer version. 1.8.0_91 is confirmed to be new enough.
##### Step 1: Install jOpt dependency
* ``git clone`` the jOPT repository (https://github.com/blubin/JOpt) and locally install it as explained in the jOPT getting-started guide.
##### Step 2: Install sats-opt
* ``git clone`` this repository and locally install it by running ``mvn clean install`` in the cloned repository.
  * Note: If your cplex installation didn't add the necessary bindings to the native libraries (e.g. by setting the
  CPLEX environment variables), the tests of sats-opt and therefore the installation will fail with an
  ``UnsatisfiedLinkError``. In this case, add the flag ``-DskipTests`` to the install command:
  ``mvn clean install -DskipTests`` and fix the bindings later by setting the java library path correctly.
##### Step 3: Add the maven dependency to your simulation project
```
<dependency>
    <groupId>org.spectrumauctions</groupId>
    <artifactId>sats-opt</artifactId>
    <version>0.5.1</version>
</dependency>
```
You can now use sats-opt, which allows you to use all the features of ``sats-core`` and solve the winner determination problems using `cplex`.

### Code examples
Code examples can be found in the
[`org.spectrumauctions.sats.opt.examples`](https://github.com/spectrumauctions/sats-opt/tree/master/src/test/java/org/spectrumauctions/sats/opt/examples)
package in the test folder of this repository.

### Bug Reports, Feature Requests and Contribution Guidelines
We are grateful for bug reports and other feedback about SATS and are welcoming everyone to contribute to the project, too. 
If you do have a bug report or have code you want to add to SATS, please follow the following guidelines.
* To report bugs or to propose new features, please open a new issue in this repositories issue tracker. 
* To contribute code, please open a pull request to merge into develop. Small bugfixes will be accepted and merged very quickly. 
For larger contributions, we recommend to discuss and plan the contribution in the issue tracker beforehand.

A list of contributors can be found at 
https://github.com/spectrumauctions/sats-core/blob/master/CONTRIBUTORS.md

### Copyright
Copyright by
* Michael Weiss
* Sven Seuken and the University of Zurich
* Ben Lubin and the Boston University

SATS is licenced under AGPL. You find the license in this repository. 
