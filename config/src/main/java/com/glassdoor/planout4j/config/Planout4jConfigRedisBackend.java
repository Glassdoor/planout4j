package com.glassdoor.planout4j.config;

import java.util.Map;

import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;


/**
 * Manages reading and writing of Planout4j configuration from / to <a href="http://redis.io">Redis</a> NoSQL store.
 */
public class Planout4jConfigRedisBackend implements Planout4jConfigBackend {

    private Jedis jedis;
    private String redisKey;

    public Planout4jConfigRedisBackend() {}

    public Planout4jConfigRedisBackend(final Jedis jedis, final String redisKey) {
        this.jedis = requireNonNull(jedis);
        this.redisKey = defaultIfEmpty(redisKey, "planout4j");
    }

    @Override
    public void configure(final Config config) {
        jedis = new Jedis(config.getString("host"), config.getInt("port"));
        redisKey = config.getString("key");
    }

    @Override
    public Map<String, String> loadAll() {
        return jedis.hgetAll(redisKey);
    }

    @Override
    public void persist(final Map<String, String> configData) {
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
        //noinspection resource
        return String.format("%s @ %s:%s, key = %s", persistenceLayer(),
                jedis.getClient().getHost(), jedis.getClient().getPort(), redisKey);
    }

}
