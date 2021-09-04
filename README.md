# PLANK

![GitHub Repo stars](https://img.shields.io/github/stars/LorenzooG/jplank?color=orange&style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/LorenzooG/jplank?color=orange&style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/LorenzooG/jplank?color=orange&style=for-the-badge)

Plank is a simple language made with LLVM and ANTLR in Kotlin. Need help? contact me
on [twitter](https://twitter.com/gabrielleeg1) or message me on discord **gabii#9722**.

### Content

* [Example](#example)
* [Modules](#modules)
* [Dependencies](#dependencies)
* [CLI](#cli)
* [Building](#building)
* Documentation (WIP)
* [Pull Requests](#pull-requests)
* [Ending](#ending)

## Example

Hello world in plank:

```kotlin
import Std.IO;

fun main(argc: Int, argv: **Char): Void {
  println("Hello, world");
}
```

You can find more examples [here](samples)

## Modules

| Name                                 | Description                                                          |
| ------------------------------------ | -------------------------------------------------------------------- |
| [cli](cli)                           | Holds all command-line stuff                                         | 
| [grammar](grammar)                   | Implements ANTLR/grammar stuff                                       |
| [composite-build](composite-build)   | Implements the build plugin with dependencies, version, etc          |
| [compiler](compiler)                 | Implements the LLVM core that compiles to IR representation          |
| [runtime](runtime)                   | Implements the engine runtime functions                              |
| [stdlib](stdlib)                     | Implements the language stdlib                                       |
| [intellij-plugin](stdlib)            | Implements the intellij tooling plugin                               |

## Dependencies

| Name       | Link                              | Version    |
| ---------- | --------------------------------- | ---------- |
| kotlin     | https://kotlinlang.org            | v1.4.30-M1 |
| java       | https://openjdk.java.net/         | v11        |
| clang++    | https://clang.llvm.org            | v10.0.0    |
| make       | https://www.gnu.org/software/make | v4.3       |

## CLI

```
Usage: plank [OPTIONS] file

Options:
  --target VALUE
  --pkg-name TEXT      The package name
  --pkg-kind VALUE     The package kind
  -O, --output VALUE   Output file
  -D, --debug          Sets the compiler on debug mode
  --emit-ir            Emits the ir code when compiling
  -I, --include VALUE  Include files
  -h, --help           Show this message and exit

Arguments:
  file  The target file
```

## Building

You should execute the following commands:

```shell
$ git clone git@github.com:plank-lang/plank.git
$ cd plank
# if you are in windows
# PS gradlew.bat cli:assembleDist
$ ./gradlew cli:assembleDist
```

The zipped distributions are located in `cli/build/distributions`

## Install

This is the [linux install instructions](#Install Linux) that is tested in ubuntu;
if you want the distributions for your operational system, you must follow the 
[Build instructions](#Building).

## Install Linux

You must download a release in [releases](https://github.com/plank-lang/plank/releases/tag/1.0.1-SNAPSHOT)
or build your one in [Building](#Building). After that you must define the environment variable `PLANK_HOME`
to the unzipped release folder, and set `PATH` to `$PLANK_HOME/bin`. So the plank is installed, make good use.
🙂

## Pull Requests

PRs are welcome as long as they are well explained and only change one feature at a time.  
This doesn't mean i'm gonna accept all PRs, it just means i'm gonna consider it.

## Ending

If you liked and/or use the language consider leaving a star on the repo and following me on twitter :)

