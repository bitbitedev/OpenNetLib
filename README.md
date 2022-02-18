# OpenNetLib - A Java Networking Library

[![Build](https://github.com/bitbitedev/OpenNetLib/actions/workflows/gradle.yml/badge.svg)](https://github.com/bitbitedev/OpenNetLib/actions/workflows/gradle.yml)
[![Version](https://img.shields.io/github/v/release/bitbitedev/OpenNetLib?include_prereleases)](https://github.com/bitbitedev/OpenNetLib/releases)
[![Discord channel](https://img.shields.io/discord/411109318511820800?logo=discord)](https://discord.gg/MdsuFg2bPC)

`OpenNetLib` is a lightweight, easy-to-use library to simplify socket communication. It is written in and for Java. It's still in an early stage but saves a lot of time already.
Start using `OpenNetLib` today or help improving it by posting issues, questions and feature requests [here](https://github.com/bitbitedev/OpenNetLib/issues).

---

Do you have questions on how to use the library or some of its functionality? Feel free to join our [discord Server](https://discord.gg/MdsuFg2bPC) and ask for help.
Also dont forget to check out the [wiki](https://github.com/bitbitedev/OpenNetLib/wiki)

## How to add as dependency
To use it unpack the archive file and add the Jar file to your projects dependencies.

### Gradle
If you are using gradle add the following line to your dependencies:
```
implementation 'dev.bitbite:OpenNetLib:1.0.1'
```

### Maven
For Maven use the following:
```
<dependency>
  <groupId>dev.bitbite</groupId>
  <artifactId>OpenNetLib</artifactId>
  <version>1.0.1</version>
</dependency>
```

### Other build tools
Vistit [maven central](https://search.maven.org/artifact/dev.bitbite/OpenNetLib) and choose the latest release to find the code you need to add

## Features
- Byte-based socket communication
- Data-pre- and -post-processing
- Eventlisteners

## How to use
Check the [Getting started](https://github.com/bitbitedev/OpenNetLib/wiki/Getting-started) guide

## Motivation
We've been having a lot of projects in the last years where sending data between instances was a part of. Most of the time we build the whole communication system from scratch, including basic connection stuff, command parsing, security features and so on. Over the time we have learned a lot, especially about occuring problems and how to solve them. Now we felt like it's time to use all what we have learned to not write basic stuff over and over again. So we started developing this library. In the same step decided to share it with everyone because we know how frustrating it can be to develop systems like this from scratch. We know that what we have done might not always be the best solution, but is there even any? We think what we have done is a good basis to extend and build upon. And we kindly ask everyone to share their problems and ask for features they think that need to be a part of this library.
