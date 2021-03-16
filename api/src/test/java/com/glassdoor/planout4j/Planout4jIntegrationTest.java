package com.glassdoor.planout4j;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.glassdoor.planout4j.config.Planout4jTestConfigHelper;
import com.glassdoor.planout4j.spring.Planout4jAppContext;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Planout4jAppContext.class)
public class Planout4jIntegrationTest {

    @BeforeClass
    public static void before() {
        Planout4jTestConfigHelper.setSystemProperties(false);
    }

    public static final String TEST_NAMESPACE_NAME = "ns_test1";
    public static final String BOGUS_NAMESPACE_NAME = "_bogus_";

    @Resource
    private NamespaceFactory namespaceFactory;

    @Test
    public void testInternalNamespaceCanBeAccessed() {
        Optional<Namespace> internal = namespaceFactory.getNamespace(TEST_NAMESPACE_NAME,
                                                                     Map.of("user_guid", 12345));
        assertTrue(TEST_NAMESPACE_NAME + " is not present in the factory " + namespaceFactory,
                   internal.isPresent());
        Optional<Namespace> bogus = namespaceFactory.getNamespace(BOGUS_NAMESPACE_NAME, Map.of("foo", ""));
        assertFalse(BOGUS_NAMESPACE_NAME + " is present in the factory " + namespaceFactory, bogus.isPresent());
    }

}
