package com.ws.controller;

import com.ws.entity.Select;
import com.ws.pool.RedisPool;
import com.ws.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AsrController {

    @Autowired
    RedisService redisService;

    @RequestMapping(value="/asr/deviceId",method ={RequestMethod.GET,RequestMethod.POST})
    public List<Select> getDeviceIds() {
        return redisService.scanDeviceIds();
    }

    @RequestMapping(value="/asr/sessionId",method ={RequestMethod.GET,RequestMethod.POST})
    public List<Select> getSessionIds(String deviceId) {
        return redisService.scanSessionIds(deviceId);
    }

    @RequestMapping(value="/asr/asrText",method ={RequestMethod.GET,RequestMethod.POST})
    public List<String> getAsr(String deviceIdAndSessionId) {
        return redisService.scanASRResult(deviceIdAndSessionId);
    }
}
