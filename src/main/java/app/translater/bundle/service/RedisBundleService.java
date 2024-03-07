package app.translater.bundle.service;

import app.translater.bundle.model.Bundle;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;


@Service
public class RedisBundleService {
    private final Gson gson = new Gson();
    private final String key = "bundles";
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private final Jedis jedis;

    public RedisBundleService() {
        this.jedis = new Jedis(REDIS_HOST, REDIS_PORT);
    }

    public void saveBundle(Bundle bundle) {
        String bundleJson = gson.toJson(bundle);
        jedis.hset(getKey(), String.valueOf(bundle.getFrom()), bundleJson);
    }

    public Bundle getBundle(Long from) {
        String bundleJson = jedis.hget(key, String.valueOf(from));
        return gson.fromJson(bundleJson, Bundle.class);
    }

    public List<Bundle> findAll() {
        List<Bundle> bundles = new ArrayList<>();
        List<String> allBundlesJson = jedis.hvals(key);
        for (String json : allBundlesJson) {
            Bundle bundle = gson.fromJson(json, Bundle.class);
            bundles.add(bundle);
        }
        return bundles;
    }

    public void deleteBundle(Long from) {
        jedis.hdel(getKey(), String.valueOf(from));
    }

    public void close() {
        jedis.close();
    }

    public String getKey() {
        return key;
    }
}
