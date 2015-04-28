package com.glassdoor.planout4j.compiler;

import com.glassdoor.planout4j.config.ValidationException;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import static java.lang.String.format;

/**
 * Customizes SnakeYAML parser to recognize <code>!planout</code> tag and replace any string annotated with the tag
 * with the product of DLS compilation.
 * @author ernest.mishkin
 */
class PlanoutDSLConstructor extends Constructor {

    private static final String PLANOUT_TAG = "!planout";

    public PlanoutDSLConstructor() {
        yamlConstructors.put(new Tag(PLANOUT_TAG), new PlanoutDSLConstruct());
    }


    private class PlanoutDSLConstruct extends AbstractConstruct {

        @Override
        public Object construct(final Node node) {
            if (!(node instanceof ScalarNode)) {
                throw new YAMLException(format("Only strings must be tagged as %s, this is %s",
                        PLANOUT_TAG, node.getClass().getSimpleName()));
            }
            final Object value = constructScalar((ScalarNode)node);
            if (value == null) {
                return null;
            }
            if (!(value instanceof String)) {
                throw new YAMLException(format("Only strings must be tagged as %s, this is %s",
                        PLANOUT_TAG, value.getClass()));
            }
            try {
                return PlanoutDSLCompiler.dsl_to_json((String)value);
            } catch (ValidationException e) {
                throw new YAMLException(format("Failed to compile PlanOut DSL (annotated with %s)", PLANOUT_TAG), e);
            }
        }

    }

}
