package com.ws.pool;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisPool {
    private static Logger log = Logger.getLogger(RedisPool.class.getClass());
    private static String ADDR = "127.0.0.1";
    private static Integer PORT = 6379;
    private static String PASS="root";

    private static Integer MAX_TOTAL = 200;
    private static Integer MAX_IDLE = 100;
    private static Integer MAX_WAIT_MILLIS = 10000;
    private static Integer TIMEOUT = 10000;
    private static Boolean TEST_ON_BORROW = true;
    private static JedisPool jedisPool = null;

    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_TOTAL);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT_MILLIS);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config,ADDR,PORT,TIMEOUT,PASS);
        } catch (Exception e) {
            log.error("Redis连接池初始化失败!");
            throw new RuntimeException("Redis连接池初始化失败!");
        }
    }

    public synchronized static Jedis getJedis(){
        try {
            if(jedisPool != null){
                Jedis jedis = jedisPool.getResource();
                return jedis;
            }else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void returnResource(final Jedis jedis){
        if(jedis!=null){
            jedisPool.returnResource(jedis);
        }
    }
}
