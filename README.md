# Spectrum Auction Test Suite (SATS)

SATS is ... // TODO

## Prerequisites
* Java 8 (or later)

## Getting started with the SATS Core API
You can use the SATS Core API as a library in your own java project.
It allows you to access and modify all of sats value models to generate value function instances for your simulations.

The complete `sats` package is deployed to Maven Central, so including it in your maven project
(or analogously with a gradle project following the gradle syntax) is as easy as 
```
<dependency>
    <groupId>org.spectrumauctions</groupId>
    <artifactId>sats</artifactId>
    <version>0.6.0</version>
</dependency>
```

Alternatively, if you don't use Maven nor Gradle, you can also simply download the newest version's JAR and include it in your project. The JAR is published along with the [release](https://github.com/spectrumauctions/sats-core/releases/).

#### Code examples
Code examples can be found in the
[`org.spectrumauctions.sats.core.examples`](https://github.com/spectrumauctions/sats/tree/master/src/test/java/org/spectrumauctions/sats/core/examples)
package. 


## Getting started with the SATS Optimization API
To access the SATS Optimization API, you first have to include `sats` analogously as for the SATS Core API.  
The SATS Optimization API is based on the SATS Core API and provides winner determination problem (WDP) solvers.

#### Highly recommended dependency: `CPLEX`
* When using `maven`, include your local `cplex.jar` in your maven repository by the following command:
  ```
  mvn install:install-file -Dfile=<path-to-cplex-jar> -DgroupId=cplex -DartifactId=cplex -Dversion=12.6 -Dpackaging=jar
  ```
* Make sure to add your native CPLEX binaries to your `PATH` (Windows) / `LD_LIBRARY_PATH` (Unix) environment variable so sats-opt can find it! This may be done automatically when installing CPLEX. 
* If you don't provide a CPLEX JAR, sats-opt will use LPSolve as a solver, which is considerably less performant.

#### Code examples
Code examples can be found in the
[`org.spectrumauctions.sats.opt.examples`](https://github.com/spectrumauctions/sats/tree/master/src/test/java/org/spectrumauctions/sats/opt/examples)
package in the test folder of this repository.


## Getting started with SATS as a Command Line Tool
You find the latest version of the SATS jar in the [latest release](https://github.com/spectrumauctions/sats/releases/latest).

### Usage example
`java -jar sats.jar --model MRVM --nationalbidders 3 --iterator RANDOM --bidsPerBidder 10`

### Options applicable for all models

| Option (* = required)           | Description |
| ------------------------------- | ----------- |
| `--model <Model>`*              | Chose which model to use to generate your value function. Possible models: BVM, MBVM, SRVM, MRVM, LSVM, GSVM, CATS |
| `--bidsPerBidder`               | The number of atomic XOR bids per bidder to be written to the output file |
| `--bidspath`                    | Path to the folder where the bid files should be stored. Default is a folder bidfiles |
| `--filetype <FileType>`         | Decide for a File Type in which the bids are returned. Options are JSON and CATS      |
| `--iterator <BiddingLanguage>`  | Define an order in which the atomic bids should be returned. Options are: SIZE_INCREASING, SIZE_DECREASING, RANDOM, BIDDER_SPECIFIC |
| `--multiplefiles`               | Define if a separate file should be created for every bidder |
| `--mute`                        | Disables notification about successful creation of files |
| `--seed <Long>`                 | Specify the seeds used for the creation of the random instances. If two seeds (e.g. --seed 123 --seed 345) are passed, one is used for the creation of a non-bidder specific parameters (aka. world) and the second one for the bidders. If only one seed is defined, it is used to generate two seeds (for world and bidders) |
| `--xorq`                        | if flag is set, the returned bids are XOR-Q (And file format JSON) |
| `--help`                        | Gives a list of all possible Options. If used with the --model tag, the options for the specified model are also printed. |

### Model-specific options

##### Base and Multi Band Value Model (BVM and MBVM) [Bichler et al, 2013]
| Option              | Description |
| ------------------- | ----------- |
| `--bidders`    | The number of bidders |

##### Single Region Value Model (SRVM) [Kroemer et al., 2016]
| Option              | Description |
| ------------------- | ----------- |
| `--smallb`    | The number of Small Bidders |
| `--highb` | The number of High Frequency Bidders |
| `--primaryb` | The number of Primary Bidders |
| `--secondaryb` | The number of Secondary Bidders |

##### Multi Region Value Model (MRVM) [Weiss et al., 2017]
| Option              | Description |
| ------------------- | ----------- |
| `--localbidders`    | The number of Local Bidders |
| `--nationalbidders` | The number of National Bidders |
| `--regionalbidders` | The number of Regional Bidders |

##### Local Synergy Value Model (LSVM)  [Scheffel et al., 2010] 
| Option              | Description |
| ------------------- | ----------- |
| `--nationalb`    | The number of National Bidders |
| `--regionalb` | The number of Regional Bidders |

##### Global Synergy Value Model (GSVM) [Goeree et al., 2008]
| Option              | Description |
| ------------------- | ----------- |
| `--nationalb`    | The number of National Bidders |
| `--regionalb` | The number of Regional Bidders |

##### CATS-Regions (CATS) [Leyton-Brown et al., 2000]
| Option              | Description |
| ------------------- | ----------- |
| `--bidders`    | The number of bidders |
| `--goods` | The number of goods |

## Bug Reports, Feature Requests and Contribution Guidelines
We are grateful for bug reports and other feedback about SATS and are welcoming everyone to contribute to the project, too. 
If you do have a bug report or have code you want to add to SATS, please follow the following guidelines.
* To report bugs or to propose new features, please open a new issue in this repositories issue tracker. 
* To contribute code, please open a pull request to merge into develop. Small bugfixes will be accepted and merged very quickly. 
For larger contributions, we recommend to discuss and plan the contribution in the issue tracker beforehand.

A list of contributors can be found at 
https://github.com/spectrumauctions/sats/blob/master/CONTRIBUTORS.md

## Copyright
Copyright by
* Michael Weiss
* Sven Seuken and the University of Zurich
* Ben Lubin and the Boston University

SATS is licenced under AGPL. You find the license in this repository. 
