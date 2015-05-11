# PlanOut4J Detailed Usage Instructions

## Namespace Configuration

```yaml
namespace: 
  # some backends (e.g. Planout4jConfigFileNackend) automatically pick up the name, but it can be provided explicitly
  # name: test_ns
  # optional namespace-level salt, otherwise the name is used
  salt: smoke_test
  # the primary unit for this namespace
  unit: userid
  # how many segments to split the traffic into
  segments: 100


# define all experiments here once

experiment_definitions:

  - definition: Sample_Experiment_1  # must be unique within namespace
    # PLanOut DSL, see http://facebook.github.io/planout/docs/planout-language-reference.html
    assign: !planout |
      group_size = uniformChoice(choices=[1, 10], unit=userid);
      specific_goal = bernoulliTrial(p=0.8, unit=userid);
      if (specific_goal) {
        ratings_per_user_goal = uniformChoice(
        choices=[8, 16, 32, 64], unit=userid);
        ratings_goal = group_size * ratings_per_user_goal;
      }

  - definition: Sample_Experiment_2
    assign: !planout |
        prob_collapse = randomFloat(min=0.0, max=1.0, unit=sourceid);
        collapse = bernoulliTrial(p=prob_collapse, unit=[storyid, viewerid]);
        
  - definition: Sample_Default
    assign: !planout |
        ratings_goal = 1;
        something_else = true;


# default experiment is required, refer to an existing definition from above
default_experiment: Sample_Default


# instantiate experiments here with "add" action, possibly multiple times
# each instance gets its own allotment of traffic
# use "remove" action to remove a previously added experiment
        
experiment_sequence:
  - action: add                       # required, one of add, remove
    definition: Sample_Experiment_1   # required for add, refers to an experiment defined in "experiment_definitions"
    name: Instance_of_Sample_Exp_1    # must be unique within experiment_sequence
    segments: 40                      # how many segments to allocate to this experiment
  - action: add
    definition: Sample_Experiment_2
    name: Instance_of_Sample_Exp_2
    segments: 20
  - action: remove
    name: Instance_of_Sample_Exp_1
  - action: add
    definition: Sample_Experiment_2
    name: New_Instance_of_Sample_Exp_2
    segments: 30
```

## Tools

The `tools` module produces "uber-jar" named `planout4j-tools-${version}.jar`. The jar is executable and self-contained (e.g. can be copied anywhere and executed without the need to specify classpath) and represents the consolidated entry point for all the tools.

`java -jar planout4j-tools-${version}.jar --help` will list the tools available as well as general options. `planout4j-tools-${version}.jar tool --help` will provide detailed help for the tool `tool`. The following tools are currently available:

* compile
	- compiles PlanOut DSL (if input is file *not* ending in  `.yaml`,  `.yml`,  or  `.p4j`  as  well  as when input is not a file) or PlanOut4J namespace config YAML with embedded PlanOut DSL (in all other cases) to JSON representation
* ship
   - compiles all namespace config YAML files in the source backend to JSON and stores results in the target backend
* nslist
	- lists all namespaces (name + short summary) in the target (effective) backend
* eval
	- evaluates namespace, experiment, or code snippet

## Sample use

#### PlanOut-style programmatically at experiment level
This is currently not implemented but would be easy to add to the codebase as all the underlying primitives (individual operations) are in place.

__TODO__: Enhance `Interpreter` to recognize when script already represents `PlanOutOp` tree.

#### Programmatically, `core` and `compiler` deps only

```java
import java.util.Collections;
import com.glassdoor.planout4j.*;
import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
nsConf = new NamespaceConfig("my namespace", 100, "userid", null);
nsConf.defineExperiment("default", "itemsToShow = uniformChoice(choices=[5, 10, 20], unit=userid);");
nsConf.setDefaultExperiment("default");
Namespace ns = new Namespace(nsConf, Collections.singletonMap("userid", 123), null);
int itemsToShow = ns.getParam("itemsToShow", 10);
```

#### Using YAML namespace configuration (no sringframework)

Let's assume there's `test-ns.yaml` file with the content as above (top of the document). We can compile it to JSON by executing compiler tool:
`mvn exec:java -Dexec.mainClass=Planout4jCompilerTool -Dtool=compilePlanout4jConfig -Dinput=test-ns.yaml -Doutput=test-ns.json`
This will produce `test-ns.json` which can be consumed by the code below.

The code (specifically, `Planout4jRepositoryImpl`) will use `planout4j-config.conf` file to determine which *backend* to use as well as to set the backend's properties. All the properties can be overridden.

__TODO__: Allow user-provided config file.

```java
// obviously this is crude; in reality one would pass the property override
// using command-line option (-D)
// the configuration mechanism is currently quite raw, will be revisited
System.setProperty("file.
NamespaceFactory nsFact = new SimpleNamespaceFactory();
Namespace ns = nsFact.getNamespace("test-ns", Collections.singletonMap("userid", 123).get();
String buttonText = ns.getParam("button_text", "default");
```

#### Using YAML namespace configuration (with sringframework)

```java
@ContextConfiguration(classes = Planout4jAppContext.class)
public class MyClass {
    @Resource
    private NamespaceFactory nsFact;
    // use nsFact as in the above example
}
```

