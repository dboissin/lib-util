package fr.dboissin.util.atmosphere;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.BroadcasterCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.lambdaj.Lambda;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * The Class RedisBroadcasterCache.
 */
public abstract class RedisBroadcasterCache implements BroadcasterCache<HttpServletRequest, HttpServletResponse> {

    /** The Constant SEPARATOR. */
    private static final String SEPARATOR = "_@_";
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(RedisBroadcasterCache.class);
    
    /** The Constant BACKLOG. */
    protected static final String BACKLOG= ":backlog";
    
    /** The id. */
    private final String id;

    /** The pool. */
    private static JedisPool pool;

    static {
        ServiceLoader<RedisConfig> loader = ServiceLoader.load(RedisConfig.class);
        logger.debug("ServiceLoader : {}", loader.toString());

        if (loader != null && loader.iterator().hasNext()) {
            RedisConfig config = loader.iterator().next();
            logger.debug("RedisConfig name : {}", config.getName());
            pool = new JedisPool(config.getConfig());
        } else {
            logger.debug("RedisConfig not found. Use default config.");
            pool = new JedisPool("localhost");
        }
        pool.init();
    }

    /**
     * Instantiates a new redis broadcaster cache.
     *
     * @param id the id
     */
    public RedisBroadcasterCache(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.atmosphere.cpr.BroadcasterCache#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.atmosphere.cpr.BroadcasterCache#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.atmosphere.cpr.BroadcasterCache#addToCache(org.atmosphere.cpr.AtmosphereResource, java.lang.Object)
     */
    @Override
    public synchronized void addToCache(
            AtmosphereResource<HttpServletRequest, HttpServletResponse> r,
            Object e) {
        logger.trace("addToCache method - ENTER");

        if (!(e instanceof String)) {
            logger.debug("addTocache - the message isn't serialized.");
            return;
        }

        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            logger.debug("addTocache - rpush val : {}", e.toString());
            Integer nb = jedis.rpush(id + BACKLOG, e.toString());
            if (r != null && r.getRequest() != null) {
                cache(r, id + SEPARATOR + (nb - 1));
            }
        } catch (TimeoutException ex) {
            logger.error("addToCache - ex : {}", ex.getMessage());
        } finally {
            if (jedis != null) {
                pool.returnResource(jedis);
            }
            logger.trace("addToCache method - LEAVE");
        }
    }

    /* (non-Javadoc)
     * @see org.atmosphere.cpr.BroadcasterCache#retrieveFromCache(org.atmosphere.cpr.AtmosphereResource)
     */
    @Override
    public synchronized List<Object> retrieveFromCache(
            AtmosphereResource<HttpServletRequest, HttpServletResponse> r) {
        logger.trace("retrieveFromCache method - ENTER");
        List<Object> cachedMessages = null;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String lastIdx = retrieveLastMessageIdx(r);
            Integer idx;
            if (lastIdx == null) {
                logger.debug("retrieveFromCache - lastIdx is null");
                if (r.getRequest().getAttribute(AtmosphereResourceImpl.PRE_SUSPEND) != null) {
                    idx = 0;
                } else {
                    return new ArrayList<Object>();
                }
            } else {
                logger.debug("retrieveFromCache - lastIdx : {}", lastIdx);
                String [] vals = lastIdx.split(SEPARATOR, 2);
                if (vals == null || id == null || !vals[0].equals(id)) {
                    return new ArrayList<Object>();
                }
                idx = Integer.parseInt(vals[1]) + 1;
            }
            Integer cacheSize = jedis.llen(id + BACKLOG);

            if (cacheSize > idx || (idx == 0 && lastIdx == null)) {
                cachedMessages = new ArrayList<Object>(jedis.lrange(id + BACKLOG,
                        idx, cacheSize - 1));
                if (r != null) {
                    cache(r, id + SEPARATOR + (cacheSize - 1));
                }
                logger.debug("retrieveFromCache - lrange res : {}", Lambda.join(cachedMessages, ","));
            } else {
                return new ArrayList<Object>();
            }
        } catch (TimeoutException ex) {
            logger.error("retrieveFromCache - ex : {}", ex.getMessage());
        } finally {
            if (jedis != null) {
                pool.returnResource(jedis);
            }
            logger.trace("retrieveFromCache method - LEAVE");
        }
        return cachedMessages;
    }

    /**
     * Cache the last message broadcasted.
     *
     * @param r  {@link org.atmosphere.cpr.AtmosphereResource}.
     * @param cacheIdx the cache index string
     */
    public abstract void cache(final AtmosphereResource<HttpServletRequest, HttpServletResponse> r, String cacheIdx);

    /**
     * Return the last message broadcasted to the {@link org.atmosphere.cpr.AtmosphereResource}.
     *
     * @param r {@link org.atmosphere.cpr.AtmosphereResource}.
     * @return the last message index string
     */
    public abstract String retrieveLastMessageIdx(final AtmosphereResource<HttpServletRequest, HttpServletResponse> r);

}
