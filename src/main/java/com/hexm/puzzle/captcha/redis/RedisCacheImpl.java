package com.hexm.puzzle.captcha.redis;

import com.hexm.puzzle.captcha.util.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis的cache实现
 *
 * @author hexm
 */
@Component
@Slf4j
public class RedisCacheImpl<T> implements Cache<T> {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置超时时间
     * @param key
     * @param timeout 秒
     */
    @Override
    public void setExp(String key,long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 是否存在key
     *
     * @param key
     * @return
     */
    @Override
    public boolean hasKey(String key) {
        Boolean b = redisTemplate.hasKey(key);
        return b != null && b;
    }

    /**
     * 从缓存中获取项目
     *
     * @param key 要查询的key
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error(ThrowableUtil.stackTraceToString(e));
            return null;
        }
    }

    /**
     * 查询集合
     *
     * @param keys 要查询的key集合
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> getList(Collection<String> keys) {
        return (List<T>) redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量set
     *
     * @param map
     */
    @Override
    public void putAll(Map<String, T> map) {
        Map<String, T> temp = new HashMap<>(map.size() * 2);
        map.forEach(temp::put);
        redisTemplate.opsForValue().multiSet(temp);
    }

    /**
     * 批量删除
     *
     * @param keys 要删除的key集合
     */
    @Override
    public void removeAll(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 往缓存中写入内容
     *
     * @param key
     * @param value
     */
    @Override
    public void put(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 往缓存中写入内容
     *
     * @param key
     * @param value
     * @param exp   超时时间，单位为秒
     */
    @Override
    public void put(String key, T value, long exp) {
        redisTemplate.opsForValue().set(key, value, exp, TimeUnit.SECONDS);
    }

    /**
     * 删除
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 前缀模糊删除
     *
     * @param key
     */
    @Override
    public void prefixRemove(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清理所有缓存
     */
    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    //----------map-----------

    @Override
    public void putMap(String key, Object hashKey, Object hashValue) {
        redisTemplate.opsForHash().put(key, hashKey, hashValue);
    }

    @Override
    public void putAllMap(String key, Map<String, T> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getMapValue(String key, String hashKey) {
        try {
            return (T) redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error(ThrowableUtil.stackTraceToString(e));
            return null;
        }
    }

    /**
     * 读取多个缓存值
     *
     * @param key
     * @param hashKey
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> getMapValue(String key, List<String> hashKey) {
        try {
            return (List<T>) redisTemplate.opsForHash().multiGet(key, (List) hashKey);
        } catch (Exception e) {
            log.error(ThrowableUtil.stackTraceToString(e));
            return null;
        }
    }

    @Override
    public Map<String, T> getMap(String key) {
        try {
            HashOperations<String, String, T> hashOperations = this.redisTemplate.opsForHash();
            return hashOperations.entries(key);
        } catch (Exception e) {
            log.error(ThrowableUtil.stackTraceToString(e));
            return null;
        }
    }

    /**
     * 是否存在map key
     *
     * @param key
     * @param hashKey
     * @return
     */
    @Override
    public boolean hasMapKey(String key, String hashKey) {
        return this.redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 移除map中的key
     *
     * @param key
     * @param hashKey
     */
    @Override
    public Long removeMapKey(String key, String... hashKey) {
        return this.redisTemplate.opsForHash().delete(key, hashKey);
    }
}
