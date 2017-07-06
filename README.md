# Spectrum Auction Test Suite: Optimization API (sats-opt)

### Getting Started
This getting started guide helps you to set up `sats-opt` to use it as a library in your own java project afterwards. 
As opposed to `sats-opt` extends ``sats-core`` and adds winner determination problem (WDP) solvers to the list of provided features.
As the WDP solvers require more external libraries, we recommend to install ``sats-core``, should you not (yet) want to use the WDP solvers. An upgrade is easily possible any time.

Should you not want to use SATS it in your own java code, but want to create problem instances in a simpler way, you may want to use the command line tool (`sats-clt`) or the web application (`sats-web`) instead. See 
http://spectrumauctions.org for more information.
##### Prerequisites
Make sure to have `git`, `maven`, `JDK 1.8 (or later)` and `cplex` installed. 
##### Step 1: Install dependency
Install `sats-core` (https://github.com/spectrumauctions/sats-core), following its getting-started guide.
##### Step 2: Install jOPT
``git clone`` the jOPT repository (https://github.com/blubin/JOpt) and locally install it as explained in the jOPT getting-started guide.
##### Step 3: Add the maven dependency to your simulation project
````
<dependency>
    <groupId>org.spectrumauctions</groupId>
    <artifactId>sats-opt</artifactId>
    <version>0.3.2</version>
</dependency>
````
You can now use sats-opt, which allows you to use all the features of ``sats-core`` and solve the winner determination problems using `cplex`.

Note: We are currently planning to deploy ``sats-core`` to maven central, which will make step 1 of this getting-started guide obsolete and installing `sats-opt` even simpler.

### Code examples
Code examples can be found in the
`org.spectrumauctions.sats.opt.examples`
package in the test folder of this repository.
https://github.com/spectrumauctions/sats-core/tree/master/src/test/java/org/spectrumauctions/sats/opt/examples

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