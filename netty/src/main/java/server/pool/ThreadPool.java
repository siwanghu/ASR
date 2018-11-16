package server.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.netty.handler.EchoServerHandler;
import server.protocol.WsProtocolRequest;
import server.task.ThreadDBTask;
import server.task.ThreadFileTask;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private  final Logger LOGGER= LoggerFactory.getLogger(EchoServerHandler.class);

    private  final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(100, 128, 10,
            TimeUnit.SECONDS, new LinkedBlockingQueue());

    public  void addTaskWirteFile(WsProtocolRequest wsProtocol){
        threadPool.execute(new ThreadFileTask(wsProtocol));
    }

    public void addTaskWirteFile(String filename,List<WsProtocolRequest> buffer){
        threadPool.execute(new ThreadFileTask(filename,buffer));
    }

    public void addTaskWriteDB(WsProtocolRequest wsProtocol){
        threadPool.execute(new ThreadDBTask(wsProtocol));
    }

    public void addTaskWriteDB(List<WsProtocolRequest> buffer){
        threadPool.execute(new ThreadDBTask(buffer));
    }

    public void close(){
        threadPool.shutdown();
    }
}
