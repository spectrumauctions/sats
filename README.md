# sats-clt

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

#### BVM and MBVM
| Option              | Description |
| ------------------- | ----------- |
| `--bidders`    | The number of bidders |

#### SRVM
| Option              | Description |
| ------------------- | ----------- |
| `--smallb`    | The number of Small Bidders |
| `--highb` | The number of High Frequency Bidders |
| `--primaryb` | The number of Primary Bidders |
| `--secondaryb` | The number of Secondary Bidders |

#### MRVM
| Option              | Description |
| ------------------- | ----------- |
| `--localbidders`    | The number of Local Bidders |
| `--nationalbidders` | The number of National Bidders |
| `--regionalbidders` | The number of Regional Bidders |

#### LSVM and GSVM
| Option              | Description |
| ------------------- | ----------- |
| `--nationalb`    | The number of National Bidders |
| `--regionalb` | The number of Regional Bidders |

#### CATS
| Option              | Description |
| ------------------- | ----------- |
| `--bidders`    | The number of bidders |
| `--goods` | The number of goods |