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

If you already cloned without `--recurse-submodules`, initialize the submodule manually:

```bash
git submodule update --init
```

## Updating the maps submodule

To pull the latest changes from the upstream [PinMAME NVRAM Maps](https://github.com/tomlogic/pinmame-nvram-maps) project:

```bash
git submodule update --remote maps
```

Then commit the updated submodule reference:

```bash
git add maps
git commit -m "Update maps submodule"
```

## Building the project

```mvn clean install```