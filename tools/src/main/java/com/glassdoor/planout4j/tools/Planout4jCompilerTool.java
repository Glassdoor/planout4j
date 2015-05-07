package com.glassdoor.planout4j.tools;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.CharStreams;

import com.glassdoor.planout4j.compiler.Planout4jConfigParser;
import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
import com.glassdoor.planout4j.config.ValidationException;

/**
 * Command-line interface to {@link com.glassdoor.planout4j.config.NamespaceConfigBuilder}.
 * Usage: <code>mvn exec:java -Dexec.mainClass=Planout4jCompilerTool -Dtool=tool -Dinput=input_file -Doutput=json_file</code>
 * where <code>tool</code> is one of the static methods of this class and <code>input_file</code> and <code>output_file</code>
 * are if either input or output files are not specified, the code will use stdin and stdout respectively.
 */
public class Planout4jCompilerTool {
   
    private static final Logger LOG = LoggerFactory.getLogger(Planout4jCompilerTool.class);

    public static void compilePlanout4jConfig(final Reader in, final Writer out) throws IOException, ValidationException {
        JSONValue.writeJSONString(Planout4jConfigParser.parseAndValidateYAML(in), out);
    }

    public static void compilePlanoutDSL(final Reader in, final Writer out) throws IOException, ValidationException {
        JSONValue.writeJSONString(PlanoutDSLCompiler.dsl_to_json(CharStreams.toString(in)), out);
    }

    /**
    * The method compiles all supplied namespaces, keeping track of invalid namespace along the way. Throws
    * ValidationException at the end if any invalid namespaces are supplied.
    * 
    * @param namespace2Config namespaces to compile
    * @param compiledNamespace2Config a map to hold compiled versions of valid namespaces
    * @throws ValidationException If any namespace is invalid.
    */
    public static void compilePlanout4jConfig(Map<String, String> namespace2Config, Map<String, String> compiledNamespace2Config) throws ValidationException {
       List<String> invalidNamespaces = new ArrayList<>();
       for (String namespaceKey : namespace2Config.keySet()) {
          StringWriter currentOutput = new StringWriter();
          try {
             compilePlanout4jConfig(new StringReader(Planout4jConfigParser.insertNamespaceNameIntoYaml(namespace2Config.get(namespaceKey), namespaceKey)), currentOutput);
          } catch (Throwable t) {
             invalidNamespaces.add(namespaceKey);
             LOG.error("Cannot compile namespace " + namespaceKey, t);
             continue;
          }
          compiledNamespace2Config.put(namespaceKey, currentOutput.toString());
       }
       if(!invalidNamespaces.isEmpty()){
          throw new ValidationException("Failed to compile following namespaces " + invalidNamespaces);
       }
    }


    public static void main(final String[] args) throws Throwable {
        ToolsSupport.initLogging("compiler tool");
        final String tool = System.getProperty("tool");
        if (StringUtils.isBlank(tool)) {
            final List<String> tools = new ArrayList<>();
            for (Method m : Planout4jCompilerTool.class.getMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && !m.getName().equals("main")) {
                    tools.add(m.getName());
                }
            }
            System.err.printf("Usage: java -Dtool=[%s] %s [-Dinput=file] [-Doutput=file]\n",
                    StringUtils.join(tools, '|'), Planout4jCompilerTool.class);
            System.exit(1);
        }

        final String inFile = System.getProperty("input");
        final Reader in = StringUtils.isBlank(inFile) ? new InputStreamReader(System.in) : new FileReader(inFile);
        final String outFile = System.getProperty("output");
        final Writer out = StringUtils.isBlank(outFile) ? new OutputStreamWriter(System.out) : new FileWriter(outFile);
        try {
            Planout4jCompilerTool.class.getMethod(tool, Reader.class, Writer.class).invoke(null, in, out);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        if (StringUtils.isBlank(outFile)) {
            out.write('\n');
            out.flush();
        } else {
            out.close();
        }
    }
    
}
