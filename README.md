# System for Remote File Access
Course Project for CX4013 Distributed Systems at Nanyang Technological University, Singapore.

Video Demo: https://youtu.be/UROoUfE-oF0

![Java Client](https://github.com/zhangks98/CX4013-NFS/workflows/Java%20Client/badge.svg)
![Python Server](https://github.com/zhangks98/CX4013-NFS/workflows/Python%20Server/badge.svg)

## Prerequisites
- Java >= 11
- Python >= 3.6

## Installation Instructions
See https://github.com/zhangks98/CX4013-NFS/releases

## Usage
### Server
```
usage: nfs-server [-h] -m {ALO,AMO} [-l LOSS_PROB] port path

Server for Remote File Access

positional arguments:
  port                  Server port.
  path                  Root directory for the server.

optional arguments:
  -h, --help            show this help message and exit
  -m {ALO,AMO}, --mode {ALO,AMO}
                        Invocation semantic.
  -l LOSS_PROB, --loss-prob LOSS_PROB
                        Probability of a response loss; default is 0.0
```

### Client
```
Usage: nfs-client [-hV] [-f=<freshInterval>] [-l=<lossProb>] <address> <port>
The client for remote file access.
      <address>   The address of the file server.
      <port>      The port of the file server.
  -f, --fresh-interval=<freshInterval>
                  Freshness interval (in ms) of the client cache. Default
                    value: 15000
  -h, --help      Show this help message and exit.
  -l, --loss-prob=<lossProb>
                  Probability of a request loss. Default value: 0
  -V, --version   Print version information and exit.
```

### Commands for Client
```
The following commands are available:     
<> - required arguments
[] - optional arguments

| read <file path> [offset] [count]                  |
| insert <file path> <offset> <data>                 |
| append <file path> <data>                          |
| register <file path> <monitor interval (ms)>       |
| touch <new file path>                              |
| ls [dir]                                           |
| help                                               |
| exit                                               |
```

## Build Instructions
### How to build and run the Java Client
- Assemble the executable
  - On Linux/macOS: `./gradlew distZip`
  - On Windows: `gradlew.bat distZip`

- Go to "build/distributions" folder and unzip the "nfs-client-1.0.zip" to a desired location.

- To run the client:
  - Change to the directory: `cd nfs-client-1.0`
  - On Linux/macOS: `bin/nfs-client [-hV] [-f=<freshInterval>] [-l=<lossProb>] <address> <port>`
  - On Windows: `bin\nfs-client.bat [-hV] [-f=<freshInterval>] [-l=<lossProb>] <address> <port>


### How to build and run the Python Server
- Go to the "python" directory: `cd python`

- Create a Python (>=3.6) [virtual environment](https://virtualenv.pypa.io/en/latest): `virtualenv -p python3 ./venv`

- Activate the virtual environment:
  - On Linux/macOS: `source ./venv/bin/activate`
  - On Windows: `.\venv\Scripts\activate`
  
- Install the package
  - For production: `pip install -U .`
  - For development: `pip install -e ".[dev,test]"`

- To run the server: `nfs-server [-h] -m {ALO,AMO} [-l LOSS_PROB] port path`
