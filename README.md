# Java samples for PinMAME NVRAM Maps

This project contains some Java code to parse NVRAM (`.nv`)
files from PinMAME, using JSON-based mapping files from the [PinMAME
NVRAM Maps](https://github.com/tomlogic/pinmame-nvram-maps) and [Superhac's Score Parser](https://github.com/superhac/pinmame-score-parser) projects.


## Requirements

The projects needs 
- Java 11
- Maven

## Setup

The NVRAM maps are included as a Git submodule under `maps/`. Clone the repository with submodules initialized:

```bash
git clone --recurse-submodules https://github.com/syd711/java-pinmame-nvmaps.git
```

## Updating the maps submodule

```bash
git pull --recurse-submodules    
```

## Building the project

```mvn clean install```