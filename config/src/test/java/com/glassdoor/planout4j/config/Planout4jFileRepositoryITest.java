package com.glassdoor.planout4j.config;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;

import static org.assertj.core.api.Assertions.assertThat;


public class Planout4jFileRepositoryITest {

   Logger LOG = LoggerFactory.getLogger(getClass());

   Planout4jConfigFileBackend fileBackend;

   @Before
   public void setup() {
      fileBackend = new Planout4jConfigFileBackend();
      fileBackend.configure(ConfigFactory.parseMap(
              ImmutableMap.of("sourceDirHierarchy", "src/test/resources/namespaces",
                              "destDirHierarchy", "src/test/resources/namespaces/output",
                              "inputFileNamesPattern", "*",
                              "outputFileExtension", ".yaml")));
   }
   
   @After
   public void cleanup() throws IOException {
      FileUtils.deleteDirectory(Paths.get(fileBackend.getDestDirHierarchy()).toFile());
   }

   @Test
   public void loadFromRepository() {
      Map<String, String> configs = fileBackend.loadAll();
      LOG.debug("loaded:\n{}", configs.toString());
      assertThat(configs).isNotEmpty();
   }

   @Test
   public void storeInRepository() {
      Map<String, String> e_configs = ImmutableMap.of("filename", "content");
      fileBackend.persist(e_configs);
      
      fileBackend.setSourceDirHierarchy(fileBackend.getDestDirHierarchy());
      Map<String, String> a_configs = fileBackend.loadAll();
      
      assertThat(a_configs).isEqualTo(e_configs);
   }

}
