[![Build Status](https://travis-ci.org/Glassdoor/planout4j.svg?branch=master)](https://travis-ci.org/Glassdoor/planout4j)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.glassdoor.planout4j/planout4j-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.glassdoor.planout4j/planout4j-core)

# PlanOut4J

PlanOut4J is a Java-based implementation of Facebook's [PlanOut], an A/B testing framework designed to conduct experiments on the web at scale.
PlanOut4J makes it easy to express, deploy and maintain sophisticated randomized experiments and to quickly iterate on these experiments, while satisfying the constraints of large-scale Internet services with many users.

[PlanOut]: http://facebook.github.io/planout/

PlanOut4J's emphasis is to enable persons who don't necessarily have comprehensive engineering background create and maintain experiments easily. To that extent, we utilize [PlanOut DSL](http://facebook.github.io/planout/docs/planout-language-reference.html) wrapped into an intuitive YAML format. Here is a simple config file describing a trivial *namespace* (more about namespaces below)

```yaml
namespace:
  unit: userid
  segments: 100

experiment_definitions:
  - definition: Default_Experiment
    assign: !planout |
      button_color = '#000000';
      button_text  = 'Register';
  - definition: Button_Experiment
    assign: !planout |
      button_color = uniformChoice(choices=['#ff0000', '#00ff00'], unit=userid);
      button_text  = weightedChoice(choices=['Join now!', 'Sign up.'], weights=[0.7, 0.3], unit=userid);

default_experiment: Default_Experiment

experiment_sequence:
  - action: add
    definition: Button_Experiment
    name: Button_Experiment.1
    segments: 40
```

What's going on here?

Firstly, we need a [namespace](https://facebook.github.io/planout/docs/namespaces.html). One might think of namespaces are containers used to run multiple experiments concurrently without stepping on each others' toes where such interference is undesirable. For a particular user, only a single experiment in a given namespace can be active at any given point in time. However, the same user can participate in multiple experiments from different namespaces. We put experiments that manipulate closely related parameters into the same namespace, thus avoiding something like presenting a user with white text on white background. If we save the above YAML in a file `test.yaml` we have `test` namespace.

Within namespace config, we define the name of the input parameter which represents out primary unit of traffic segmentation (`userid` in this case) as well as the number of segments to split the traffic into (`100`). Then we define two experiments: `Default_Experiment` and `Button_Experiment`. The former is required to give all parameters initial values which are used when a user is not part of any experiment. The lines following `!planout |` (which represent YAML syntax for assigning a tag and expecting multiline string) are the written in the [PlanOut DSL](http://facebook.github.io/planout/docs/planout-language-reference.html) and can be as trivial or as complicated as needed. The latter experiment involves randomly assigning button color and text, with color being uniformly distributed and text having custom weights. In the last section, `experiment_sequence`, we "instantiate" `Button_Experiment` and allocate 40% of traffic to it.

## Project Structure
The project is comprised of the following maven modules:

* `core` - the most important module. Defines all PlanOut operations, as well as the central classes `Namespace`, `NamespaceConfig`, `Experiment`, and others. Does not depend on any other modules. If one wants to use PlanOut4J programmatically (i.e. without external configuration) or one already has a parsed JSON object representing namespace based on the above "schema", then one needs `core` module only
* `compiler` - this module provide java wrapper for [PlanOut compiler](https://github.com/facebook/planout/tree/master/compiler) as well as tools and API to compile namespace YAML (see above) with embedded PlanOut DSL into JSON.
* `config` - this module defines API for reading namespace configuration data from / writing to a *backend*. Currently *file system backend* and *Redis backend* (both used internally at Glassdoor) are provided. The module also exposes `Planout4jRepository` interface which acts as a facade to one or more *backends*. It depends on `compiler` for parsing the data.
* `api` - this is the primary entry point. It provides `NamespaceFactory` interface and several implementations. It depends on `config` for loading up each individual *namespace* and maintains a cache of those keyed by name. This is what majority of developers will likely use.
* `tools` - this contains all command-line tools. Tools are described in details in the [usage](USAGE.md) document.

## Maven
Binary artifacts are hosted at Sonatype (OSSRH) repository and releases are propagated to Maven Central.
Too use PlanOut4J in a project, add the [latest version of planout4j-api](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22planout4j-api%22)
to the dependencies in `pom.xml`, for ex.:

 ```xml
 <dependency>
     <groupId>com.glassdoor.planout4j</groupId>
     <artifactId>planout4j-api</artifactId>
     <version>1.0</version>
 </dependency>
 ```

The current set of snapshot artifacts is available [here](https://oss.sonatype.org/content/groups/staging/com/glassdoor/planout4j/planout4j-api)

## Backends
Backend is an abstraction used to access (read/write) namespace configuration data without concern of the underlying storage mechanism.
Currently there are two types of backends:

* File system ([implementation](https://github.com/Glassdoor/planout4j/blob/master/config/src/main/java/com/glassdoor/planout4j/config/Planout4jConfigFileBackend.java))
	* has separate properties for read (source) and write (target) use
* Redis ([implementation](https://github.com/Glassdoor/planout4j/blob/master/config/src/main/java/com/glassdoor/planout4j/config/Planout4jConfigRedisBackend.java))

Backends come into play in two cases:

1. `NamespaceFactory` implementation uses [Planout4jConfigRepositoryImpl](https://github.com/Glassdoor/planout4j/blob/master/config/src/main/java/com/glassdoor/planout4j/config/Planout4jRepositoryImpl.java) to fetch the namespace configs which have already been compiled to JSON. Redis backend is most appropriate for this purpose
2. `Planout4jShipperTool` uses [Planout4jShipperImpl](https://github.com/Glassdoor/planout4j/blob/master/config/src/main/java/com/glassdoor/planout4j/config/Planout4jShipperImpl.java) to get all namespaces from **source** backend, compile & validate them, and store in **target** backend. File system - to - File system and File system - to - Redis are reasonable examples of the shipper setup.

Please see the [default configuration file](https://github.com/Glassdoor/planout4j/blob/master/config/src/main/resources/planout4j.conf) to learn about the settings and ways to override them.

## Using PlanOut4J
Please see detailed instructions [here](USAGE.md)

## PlanOut4J at Glassdoor

At Glassdoor, we have been using PlanOut4J extensively for several months conducting real-life production experiments at large scale (1 mil parameter lookups per day for each of around 10 namespaces). We are very grateful to Facebook for open-sourcing the original PlanOut and we hope that our java port with additional features will help to extend these robust A/B testing ideas to many teams who use Java as the primary programming language.

### More on Configuration

We actually use a pretty interesting configuration pipeline at Glassdoor. It works as following:

* A dedicated Git repository is maintained for all namespace config (yaml) files
* The repository is curated by data scientists and product managers
* When a change is made on "production" branch (presumably via a pull request), a Jenkins job picks it up, and performs the following:
  * compile affected namespace(s) from YAML to JSON (with validation)
  * store the result JSON in Redis with namespace name as a part of the key
* Our instance of `RefreshingNamespaceFactory` is configured to use Redis backend and every 2 mins pulls all namespace data from Redis.

This scheme allowed us to achieve high level of automation, good system of checks and balances (git), and fast performance in a distributed environment (Redis, getting data in JSON). We intend to expose more of that in the code but there's still some work to be done to eliminate dependencies on various internal tools and libraries.

### Overriding Parameters

Our web front-end infrastructure contains code that looks for custom planout4j headers. We use this mechanism to be able to override any parameters set in any of the experiments. This seemed to us a cleaner approach as compared to using query string (which is often transformed by existing logic). There are handy browser extensions for manipulating headers.