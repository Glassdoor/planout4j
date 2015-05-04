package com.glassdoor.planout4j.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;

/**
 * Manages reading and writing of Planout4j configuration from / to <a href="http://redis.io">Redis</a> NoSQL store.
 */
public class Planout4jConfigRedisBackend implements Planout4jConfigBackend {

   private static final Logger LOG = LoggerFactory.getLogger(Planout4jConfigRedisBackend.class);

   private Jedis jedis;
   private String redisKey;

   public void configure(final Config config) {
      jedis = new Jedis(config.getString("host"), config.getInt("port"));
      redisKey = config.getString("key");
   }

   @Override
   public Map<String, String> loadAll() {
      return jedis.hgetAll(redisKey);
   }

   @Override
   public void persist(Map<String, String> configData) {
      jedis.del(redisKey);
      if (configData != null && !configData.isEmpty()) {
         jedis.hmset(redisKey, configData);
      }
   }

   @Override
   public String persistenceLayer() {
      return "REDIS";
   }

   @Override
   public String persistenceDestination() {
      return String.format("%s @ %s:%s, key = %s", persistenceLayer(),
              jedis.getClient().getHost(), jedis.getClient().getPort(), redisKey);
   }

}
