package server.task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import server.asr.Asr;
import server.netty.handler.EchoServerHandler;
import server.pool.RedisPool;
import server.protocol.ConstantValue;
import server.protocol.WsProtocolRequest;
import server.until.ServerUntil;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;

public class ThreadFileTask implements Runnable, Serializable {
    private static final Logger LOGGER= LoggerFactory.getLogger(EchoServerHandler.class);
    private WsProtocolRequest wsProtocol;
    private List<WsProtocolRequest> buffer;
    private ServerUntil serverUntil;
    private Asr asr;
    private String filename;
    private Jedis jedis;

    public ThreadFileTask(WsProtocolRequest wsProtocol){
        this.wsProtocol=wsProtocol;
        this.buffer=null;
        serverUntil=new ServerUntil();
        asr=new Asr();
        jedis=RedisPool.getJedis();
    }

    public ThreadFileTask(String filename,List<WsProtocolRequest> buffer){
        this.buffer=buffer;
        this.filename=filename;
        this.wsProtocol=null;
        serverUntil=new ServerUntil();
        asr=new Asr();
        jedis=RedisPool.getJedis();
    }

    @Override
    public void run() {
        if(wsProtocol!=null) {
            String deviceID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(wsProtocol.getDeviceIdHigh()), serverUntil.longToBytes(wsProtocol.getDeviceIdLow()));
            String sessionID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(wsProtocol.getSessionIdHigh()), serverUntil.longToBytes(wsProtocol.getSessionIdLow()));
            String sequenceID = String.valueOf(wsProtocol.getSequenceId());
            Thread.currentThread().setName(deviceID + ":" + sessionID + ":" + sequenceID);
            String deviceDir = ConstantValue.PCM_DIR + File.separatorChar + deviceID;
            String sessionDir = deviceDir + File.separatorChar + sessionID;
            String splitDir=sessionDir+File.separatorChar+ConstantValue.SPLIT_DIR;
            String file = sessionDir + File.separatorChar + sequenceID;
            serverUntil.hasDirectoryWithCreate(deviceDir);
            serverUntil.hasDirectoryWithCreate(sessionDir);
            serverUntil.hasDirectoryWithCreate(splitDir);
            serverUntil.hasFileWithCreate(file);
            try {
                RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                long fileLength = randomFile.length();
                randomFile.seek(fileLength);
                randomFile.write(wsProtocol.getContent());
                wsProtocol.setContent(null);
                randomFile.close();
                LOGGER.debug("线程：" + Thread.currentThread().getName() + "执行成功!");
            } catch (Exception e) {
                LOGGER.error("线程：" + Thread.currentThread().getName() + "文件写入出错!");
                throw new RuntimeException(e);
            }
        }else {
            try {
                String deviceID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(buffer.get(0).getDeviceIdHigh()), serverUntil.longToBytes(buffer.get(0).getDeviceIdLow()));
                String sessionID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(buffer.get(0).getSessionIdHigh()), serverUntil.longToBytes(buffer.get(0).getSessionIdLow()));
                String deviceDir = ConstantValue.PCM_DIR+ File.separatorChar + deviceID;
                String sessionDir = deviceDir + File.separatorChar + sessionID;
                String splitDir=sessionDir+File.separatorChar+ConstantValue.SPLIT_DIR;
                String file = sessionDir + File.separatorChar + filename+ConstantValue.SUFFX_NAME;
                serverUntil.hasDirectoryWithCreate(deviceDir);
                serverUntil.hasDirectoryWithCreate(sessionDir);
                serverUntil.hasDirectoryWithCreate(splitDir);
                serverUntil.hasFileWithCreate(file);
                RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                for(int i=0;i<buffer.size();i++){
                    long fileLength = randomFile.length();
                    randomFile.seek(fileLength);
                    randomFile.write(buffer.get(i).getContent());
                    buffer.get(i).setContent(null);
                }
                randomFile.close();
                this.buffer.clear();
                this.buffer=null;
                String result=asr.asr(file);
                JSONObject jsonObject=new JSONObject(result);
                if(jsonObject.has("result")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    if (jsonArray.get(0) != null &&jsonArray.get(0).toString().length()>3) {
                        jedis.lpush(deviceID + ":" + sessionID, filename + ":" + jsonArray.get(0).toString());
                    }
                    LOGGER.debug("后台线程：" + Thread.currentThread().getName() + "执行结束!");
                }
                RedisPool.returnResource(jedis);
            } catch (Exception e) {
                LOGGER.error("后台线程：" + Thread.currentThread().getName() + "文件写入出错!");
                if(jedis!=null){
                    RedisPool.returnResource(jedis);
                }
                throw new RuntimeException(e);
            }
        }
    }
}
