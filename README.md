# Spectrum Auction Test Suite (SATS)

SATS is a universal "Spectrum Auction Test Suite". SATS contains seven value models (some stylized and some realistic)
for spectrum auctions. The SATS software provides auction instance generators for each of the value models, i.e.,
it allows a user to generate an arbitrary number of auction instances for any of the spectrum value models.
For four of the models, SATS also contains a MIP formulation for the winner determination problem,
which enables users to quickly find the efficient allocation of the auction (and without restriction to a small number of sampled bundles, as occurs in CATS [Leyton-Brown et al., 2000]).

More information about SATS, and the ability to run SATS as a web service, is available at the SATS web page [spectrumauctions.org](http://spectrumauctions.org/).

## Citation

SATS was developed in a [collaboration](https://github.com/spectrumauctions/sats/blob/master/CONTRIBUTORS.md) between the [University of Zurich](http://www.uzh.ch/) and [Boston University](http://www.bu.edu/).

The System is described in detail in the following paper: 

**SATS: A Universal Spectrum Auction Test Suite**
Michael Weiss, Benjamin Lubin, and Sven Seuken. 
In Proceedings of the 16th International Conference on Autonomous Agents and Multiagent Systems (AAMAS), São Paulo, Brazil, May 2017.
[[pdf](http://www.ifi.uzh.ch/ce/publications/SATS_Weiss_et_al_AAMAS_2017.pdf)]


If you use this software for academic purposes, please cite the above in your work.  Bibtex for this reference is as follows:

```
@inproceedings{weiss2017sats,
  title={Sats: A universal spectrum auction test suite},
  author={Weiss, Michael and Lubin, Benjamin and Seuken, Sven},
  booktitle={Proceedings of the 16th Conference on Autonomous Agents and MultiAgent Systems},
  address={São Paulo, Brazil},
  pages={51--59},
  year={2017},
}
```

## Code Modules

The SATS source code consists of three sub-modules:
* **[Core API](#getting-started-with-the-sats-core-api)**: Contains all value models and all features of SATS
* **[Optimization API](#getting-started-with-the-sats-optimization-api)**: Contains the winner determination solvers
* **[Command Line Tool](#getting-started-with-sats-as-a-command-line-tool)**: Allows generation of auction instances (i.e., value files) via command line with limited parametrization options

Since v0.6.0, these sub-modules are merged into a single module called `sats`.

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
    <version>0.6.2</version>
</dependency>
```

Alternatively, if you don't use Maven nor Gradle, you can also simply download the newest version's JAR and include it in your project.
The JAR is published along with the [release](https://github.com/spectrumauctions/sats/releases/).

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
  mvn install:install-file -Dfile=<path-to-cplex-jar> -DgroupId=cplex -DartifactId=cplex -Dversion=12.8 -Dpackaging=jar
  ```
* Make sure to add your native CPLEX binaries to your `PATH` (Windows) / `LD_LIBRARY_PATH` (Unix) environment variable so sats-opt can find it! This may be done automatically when installing CPLEX. 
* If you don't provide a `cplex.jar`, sats-opt will use LPSolve as a solver, which is considerably less performant.

#### Code examples
Code examples can be found in the
[`org.spectrumauctions.sats.opt.examples`](https://github.com/spectrumauctions/sats/tree/master/src/test/java/org/spectrumauctions/sats/opt/examples)
package in the test folder of this repository.


## Getting started with SATS as a Command Line Tool
You find the latest version of the `sats.jar` in the [latest release](https://github.com/spectrumauctions/sats/releases/latest).

### Usage example
`java -jar sats.jar --model MRVM --nationalbidders 3 --iterator RANDOM --bidsPerBidder 10`

### Options applicable for all models

| Option (* = required)           | Description |
| ------------------------------- | ----------- |
| `--model <Model>`*              | Chose which model to use to generate your value function. Possible models: BVM, MBVM, SRVM, MRVM, LSVM, GSVM, CATS |
| `--bidsPerBidder`               | The number of atomic XOR bids per bidder to be written to the output file |
| `--bidspath`                    | Path to the folder where the bid files should be stored. Default is a folder bidfiles |
| `--filetype <FileType>`         | Decide for a File Type in which the bids are returned. Options are JSON and CATS      |
| `--iterator <BiddingLanguage>`  | Define an order in which the atomic bids should be returned. Options are: SIZE_INCREASING, SIZE_DECREASING, RANDOM, BIDDER_SPECIFIC, CATS_SPECIFIC |
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

### Usage example to get the same output as the [CATS Region](http://www.cs.ubc.ca/~kevinlb/CATS/)
If no `iterator` option is set when choosing the CATS Region model, SATS defaults to the original, CATS-specific iterator.
The same goes for the file type.

In the original CATS, the number of bidders are derived from the requested number of bids.
In SATS, the user can specify the number of bidders, which results in a certain number of bids.

So, creating bids the following way in CATS:

`cats -d "regions-npv" -bids 120 -goods 256`

is the same as creating bids the following way in SATS:

`java -jar sats.jar --model CATS --bidders 20 --goods 256`

Note that there are slight statistical deviations (e.g. average number of bids per bidder, average bundle size, etc.) if the SATS output is compared to the CATS output.
See the corresponding [tests](https://github.com/spectrumauctions/sats/blob/master/src/test/java/org/spectrumauctions/sats/core/bidlang/xor/CatsXORTest.java) for details.
Those deviations are very small and probably linked to the differences in the detailed implementations, therefore can be ignored in most cases.

## Bug Reports, Feature Requests and Contribution Guidelines
We are grateful for bug reports and other feedback about SATS and are welcoming everyone to contribute to the project, too. 
If you do have a bug report or have code you want to add to SATS, please follow the following guidelines.
* To report bugs or to propose new features, please open a new issue in this repository's issue tracker. 
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
