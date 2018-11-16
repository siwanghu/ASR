package server.netty.handler;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import server.pool.RedisPool;
import server.pool.ThreadPool;
import server.protocol.ConstantValue;
import server.protocol.WsProtocolRequest;
import server.protocol.WsProtocolResponse;
import server.until.ServerUntil;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class EchoServerHandler extends ChannelHandlerAdapter {
	private MongoClient mongoClient;
	private ServerUntil serverUntil;
	private ThreadPool threadPool;
	private Jedis jedis;
	private long sequence;
	private List<WsProtocolRequest> buffer;

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		mongoClient = new MongoClient( ConstantValue.MONGODB_URL, 27017 );
		serverUntil=new ServerUntil();
		threadPool=new ThreadPool();
		buffer=new LinkedList<WsProtocolRequest>();
		jedis= RedisPool.getJedis();
		sequence=0;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		WsProtocolRequest wsProtocol=(WsProtocolRequest)msg;
		ReferenceCountUtil.release(msg);
		if(wsProtocol.getTypeId()==0) {
			String deviceID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(wsProtocol.getDeviceIdHigh()), serverUntil.longToBytes(wsProtocol.getDeviceIdLow()));
			String sessionID = serverUntil.bytesToStringUUID(serverUntil.longToBytes(wsProtocol.getSessionIdHigh()), serverUntil.longToBytes(wsProtocol.getSessionIdLow()));
			String sequenceID = String.valueOf(wsProtocol.getSequenceId());
			MongoDatabase mongoDatabase = mongoClient.getDatabase(deviceID);
			MongoCollection<Document> collection = mongoDatabase.getCollection(sessionID);
			Document document=new Document();
			document.append("_id",sequenceID);
			document.append("device",deviceID);
			document.append("session",sessionID);
			document.append("content", Base64.getEncoder().encodeToString(wsProtocol.getContent()));
			collection.insertOne(document);
			if (!jedis.sismember("deviceID", deviceID)) {
				jedis.sadd("deviceID", deviceID);
			}
			if (!jedis.sismember(deviceID, sessionID)) {
				jedis.sadd(deviceID, sessionID);
			}
			if(buffer.size()<ConstantValue.BUFFER_SIZE){
				buffer.add(wsProtocol);
			}else{
				threadPool.addTaskWirteFile(String.valueOf(sequence),new LinkedList<WsProtocolRequest>(buffer));
				buffer.clear();
				buffer.add(wsProtocol);
				sequence++;
			}
		}else if(wsProtocol.getTypeId()==1){
			ctx.channel().writeAndFlush(new WsProtocolResponse(ConstantValue.PONG));
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if(buffer.size()>0){
			threadPool.addTaskWirteFile(String.valueOf(sequence),new LinkedList<WsProtocolRequest>(buffer));
		}
		closeResourse(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
		closeResourse(ctx);
	}

	private void closeResourse(ChannelHandlerContext ctx){
		sequence=0;
		serverUntil=null;
		if(mongoClient!=null) {
			mongoClient.close();
		}
		if(buffer!=null){
			buffer.clear();
		}
		if(threadPool!=null){
			threadPool.close();
		}
		if(jedis!=null){
			RedisPool.returnResource(jedis);
		}
		System.out.println(ctx.channel().remoteAddress()+":执行关闭连接");
		System.gc();
	}
}