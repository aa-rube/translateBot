package app.translater.bundle.redis;

import app.translater.bundle.model.Bundle;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class BundleRedisDao {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    public static final String KEY = "bundle:";

    private final JedisPool jedisPool;

    public BundleRedisDao() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
    }

    public void saveBundle(Bundle bundle) {
        try (Jedis jedis = jedisPool.getResource()) {
            String bundleKey = KEY + bundle.getFrom();
            Map<String, String> bundleMap = Map.of(
                    "nameTo", bundle.getNameTo(),
                    "nameFrom", bundle.getNameFrom(),

                    "key", bundle.getKey(),
                    "lang", bundle.getLang(),
                    "flag", bundle.getFlag(),

                    "to", String.valueOf(bundle.getTo()),
                    "from", String.valueOf(bundle.getFrom())
            );
            jedis.hmset(bundleKey, bundleMap);
        }
    }

    public HashMap<String, Bundle> getAllBundles() {
        HashMap<String, Bundle> allBundles = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(KEY + "*");
            for (String key : keys) {
                Map<String, String> bundleMap = jedis.hgetAll(key);
                Bundle bundle = new Bundle();
                bundle.setNameTo(bundleMap.get("nameTo"));
                bundle.setNameFrom(bundleMap.get("nameFrom"));

                bundle.setKey(bundleMap.get("key"));
                bundle.setLang(bundleMap.get("lang"));

                bundle.setTo(Long.parseLong(bundleMap.get("to")));
                bundle.setFrom(Long.parseLong(bundleMap.get("from")));

                bundle.setFlag(bundleMap.get("flag"));

                allBundles.put(bundle.getKey(), bundle);
            }
        }
        return allBundles;
    }

    public Optional<Bundle> getBundle(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> bundleMap = jedis.hgetAll(key);
            if (bundleMap.isEmpty()) {
                return Optional.empty();
            }

            Bundle bundle = new Bundle();
            bundle.setNameTo(bundleMap.get("nameTo"));
            bundle.setNameFrom(bundleMap.get("nameFrom"));
            bundle.setKey(bundleMap.get("key"));
            bundle.setLang(bundleMap.get("lang"));
            bundle.setFlag(bundleMap.get("flag"));
            bundle.setTo(Long.parseLong(bundleMap.get("to")));
            bundle.setFrom(Long.parseLong(bundleMap.get("from")));

            return Optional.of(bundle);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void deleteBundle(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public void close() {
        jedisPool.close();
    }
}