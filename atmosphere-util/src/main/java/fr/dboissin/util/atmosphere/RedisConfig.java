package fr.dboissin.util.atmosphere;

import redis.clients.jedis.JedisShardInfo;

/**
 * The Interface RedisConfig.
 */
public interface RedisConfig {

    /**
     * Get implementation name.
     *
     * @return name
     */
    String getName();

    /**
     * Get config object to redis connection.
     *
     * @return info object to jedis
     */
    JedisShardInfo getConfig();

}
