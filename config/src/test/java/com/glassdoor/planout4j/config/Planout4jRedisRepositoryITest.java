package com.glassdoor.planout4j.config;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import redis.embedded.RedisServer;

import static org.assertj.core.api.Assertions.assertThat;


public class Planout4jRedisRepositoryITest {

   private static final int TEST_PORT = 46379;

   private Planout4jConfigRedisBackend redisBackend;
   private RedisServer redisServer;

   @Before
   public void setup() throws IOException {
      redisServer = new RedisServer(TEST_PORT);
      redisServer.start();
      redisBackend = new Planout4jConfigRedisBackend();
      redisBackend.configure(ConfigFactory.parseMap(
                  ImmutableMap.of("host", "localhost", "port", TEST_PORT, "key", "planout4j")));
   }
   
   @After
   public void cleanup() throws IOException {
      redisServer.stop();
   }

   @Test
   public void testStoreAndLoad() {
      assertThat(redisBackend.loadAll()).isEmpty();
      persistAndCheck(ImmutableMap.of("namespace1", "data1"));
      persistAndCheck(ImmutableMap.of("namespace2", "data2"));
      persistAndCheck(ImmutableMap.of("namespace1", "data1", "namespace2", "data2"));
      persistAndCheck(ImmutableMap.<String, String>of());
   }

   private void persistAndCheck(Map<String, String> e_configs) {
      redisBackend.persist(e_configs);
      assertThat(redisBackend.loadAll()).isEqualTo(e_configs);
   }

}
