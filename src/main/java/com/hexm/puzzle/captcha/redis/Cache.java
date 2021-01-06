package com.hexm.puzzle.captcha.redis;


import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存接口
 * key值统一使用string类型，多级key使用‘:’分隔，例如mall:user:1
 * value 可以指定任意类型
 *
 * @author hexm
 */
public interface Cache<T> {

    /**
     * 设置超时时间
     *
     * @param key
     * @param timeout 秒
     */
    void setExp(String key, long timeout);

    /**
     * 是否存在key
     *
     * @param key
     * @return
     */
    boolean hasKey(String key);

    /**
     * 从缓存中获取项目
     *
     * @param key
     * @return the cached object or <tt>null</tt>
     */
    <K> K get(String key);

    /**
     * 查询集合
     *
     * @param keys 要查询的key集合
     * @return
     */
    List<T> getList(Collection<String> keys);

    /**
     * 批量set
     *
     * @param map
     */
    void putAll(Map<String, T> map);


    /**
     * 批量删除
     *
     * @param keys 要删除的key集合
     */
    void removeAll(Collection<String> keys);

    /**
     * 往缓存中写入内容
     *
     * @param key
     * @param value
     */
    void put(String key, T value);

    /**
     * 往缓存中写入内容
     *
     * @param key
     * @param value
     * @param exp   超时时间，单位为秒
     */
    void put(String key, T value, long exp);

    /**
     * 删除
     *
     * @param key
     */
    void remove(String key);

    /**
     * 前缀模糊删除
     *
     * @param key
     */
    void prefixRemove(String key);

    /**
     * 清理所有缓存
     */
    void clear();

    /**
     * 往缓存中写入内容,key为键，hashKey为map的key，hashValue为map的value
     *
     * @param key       缓存key
     * @param hashKey   缓存中hashKey
     * @param hashValue hash值
     */
    void putMap(String key, Object hashKey, Object hashValue);

    /**
     * 往缓存中写入内容
     *
     * @param key
     * @param map
     */
    void putAllMap(String key, Map<String, T> map);

    /**
     * 读取缓存值
     *
     * @param key
     * @param hashKey
     * @return
     */
    T getMapValue(String key, String hashKey);

    /**
     * 读取多个缓存值
     *
     * @param key
     * @param hashKey
     * @return
     */
    List<T> getMapValue(String key, List<String> hashKey);

    /**
     * 是否存在map key
     *
     * @param key
     * @param hashKey
     * @return
     */
    boolean hasMapKey(String key, String hashKey);

    /**
     * 移除map中的key
     *
     * @param key
     * @param hashKey
     * @return
     */
    Long removeMapKey(String key, String... hashKey);

    /**
     * 读取缓存值
     *
     * @param key
     * @return
     */
    Map<String, T> getMap(String key);
}
