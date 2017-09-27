# Spectrum Auction Test Suite: Core API (sats-core)

### Getting Started
This getting started guide helps you to set up sats-core to use it as a library in your own java project afterwards. 
Should you not want to use it in your own code, you may want to use the command line tool (sats-clt) or the web application (sats-web) instead. See 
http://spectrumauctions.org for more information.

#### Add the dependency to your simulation project
sats-core is deployed to Maven Central, so including it in your maven project (or analogously with a gradle project following the gradle syntax) is as easy as 
```
<dependency>
    <groupId>org.spectrumauctions</groupId>
    <artifactId>sats-core</artifactId>
    <version>0.5.1</version>
</dependency>
```
You can now use sats-core, which allows you to access and modify all of sats value models to generate value function instances for your simulations.
Should you require to use solve the winner determination problems, we recommend you to give sats-opt (see https://github.com/spectrumauctions/sats-opt) a try.

#### Alternative without Maven/Gradle
If you don't base your project on maven nor gradle, you can also simply download the newest version's JAR and include it in your project. The JAR is published along with the [release](https://github.com/spectrumauctions/sats-core/releases/).

### Code examples
Code examples can be found in the
[`org.spectrumauctions.sats.core.examples`](https://github.com/spectrumauctions/sats-core/tree/master/src/test/java/org/spectrumauctions/sats/core/examples)
package. 

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
