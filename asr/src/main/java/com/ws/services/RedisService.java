package com.ws.services;

import com.ws.entity.Select;
import com.ws.pool.RedisPool;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class RedisService {

    public List<Select> scanDeviceIds(){
        Jedis jedis= RedisPool.getJedis();
        Set<String> deviceIds=jedis.smembers("deviceID");
        List<Select> list=new ArrayList<Select>();
        for(String device :deviceIds){
            list.add(new Select(device,device));
        }
        RedisPool.returnResource(jedis);
        return list;
    }

    public List<Select> scanSessionIds(String deviceId){
        Jedis jedis= RedisPool.getJedis();
        Set<String> sessionIds=jedis.smembers(deviceId);
        List<Select> list=new ArrayList<Select>();
        for(String session :sessionIds){
            list.add(new Select(session,session));
        }
        RedisPool.returnResource(jedis);
        return list;
    }

    public List<String> scanASRResult(String deviceIdAndSessionId){
        Jedis jedis= RedisPool.getJedis();
        List<Integer> keys=new ArrayList<Integer>();
        List<String> list = jedis.lrange(deviceIdAndSessionId, 0, -1 );
        Map<Integer,String> map=new HashMap<Integer,String>();
        for(String str:list){
            String[] split=str.split(":");
            map.put(Integer.parseInt(split[0]),split[1]);
            keys.add(Integer.parseInt(split[0]));
         }
        Collections.sort(keys);
        list.clear();
        for(Integer integer:keys){
            list.add("\n"+map.get(integer));
        }
        RedisPool.returnResource(jedis);
        Collections.reverse(list);
        return list;
    }
}
