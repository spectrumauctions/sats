# Spectrum Auction Test Suite: Command Line Tool (sats-clt)

### Download
Find the latest version of the SATS command line tool at 
https://github.com/spectrumauctions/sats-clt/blob/master/executable/sats.jar
Older versions can be downloaded in the `executable` folder of the accordingly tagged commit in this repository.

### Usage example

`java -jar sats.jar --model MRVM --nationalbidders 3 --iterator RANDOM --bidsPerBidder 10`

SATS requires Java 8 (or later). 

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

### Bug Reports, Feature Requests and Contribution Guidelines
We are grateful for bug reports and other feedback about SATS and are welcoming everyone to contribute to the project, too. 
If you do have a bug report or have code you want to add to SATS, please follow the following guidelines.
* To report bugs or to propose new features, please open a new issue in this repositories issue tracker. 
* To contribute code, please open a pull request to merge into develop. Small bugfixes will be accepted and merged very quickly. 
For larger contributions, we recommend to discuss and plan the contribution in the issue tracker beforehand.

A list of contibutors can be found at 
https://github.com/spectrumauctions/sats-core/blob/master/CONTRIBUTORS.md

### Copyright
Copyright by
* Michael Weiss
* Sven Seuken and the University of Zurich
* Ben Lubin and the Boston University

SATS is licenced under AGPL. You find the license in this repository. 