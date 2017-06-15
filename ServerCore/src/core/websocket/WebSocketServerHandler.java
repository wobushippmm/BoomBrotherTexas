package core.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import core.config.Constant;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketTerminal;
import core.net.SocketThread;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private Logger log = LoggerHelper.getLogger();
	public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	public static HashMap<Channel, SocketTerminal> channel2Terminal = new HashMap<Channel, SocketTerminal>();
	
	private WebSocketServerHandshaker handshaker;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		channelGroup.add(ctx.channel());
		if(channel2Terminal.containsKey(ctx.channel())){
			channel2Terminal.remove(ctx.channel()).quit();
		}
		channel2Terminal.put(ctx.channel(), new SocketTerminal(ctx));
		log.info("WebSocket connect");
		
	}
	
	@Override  
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {  
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {  
            IdleStateEvent event = (IdleStateEvent) evt;  
            if (event.state() == IdleState.READER_IDLE) {  
            	SocketTerminal terminal = channel2Terminal.get(ctx.channel());
            	if(terminal != null){
            		log.warn("Websocket, Long time no see !!!");
            		terminal.quit();
            	}
            }    
            else if (event.state() == IdleState.WRITER_IDLE)  
                ;  
            else if (event.state() == IdleState.ALL_IDLE)  
                ;  
        }  
    }  

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 自带快速心跳
		channelGroup.remove(ctx.channel());
		if(channel2Terminal.containsKey(ctx.channel())){
			channel2Terminal.remove(ctx.channel()).quit();
		}
		log.info("WebSocket disconnect");
	}

	

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 传统的HTTP接入
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
        // WebSocket接入
        else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("exceptionCaught", cause);
		if(channel2Terminal.containsKey(ctx.channel())){
			channel2Terminal.remove(ctx.channel()).quit();
		}
		channelGroup.remove(ctx.channel());
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		log.info("websocket shaker hand");
		// 如果HTTP解码失败，返回HHTP异常
		if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}
//		log.info("ws://" + SocketThread.getServerIp()+ ":"+ NetManager.websocketPort+"/websocket");
		// 构造握手响应返回，本机测试
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				"ws://" + SocketThread.getServerIp()+ ":"+ NetManager.websocketPort, null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}
	   
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		// 判断是否是关闭链路的指令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		// 本例程仅支持文本消息，不支持二进制消息
		if (!(frame instanceof BinaryWebSocketFrame)) {
			throw new UnsupportedOperationException(
					String.format("%s frame types not supported", frame.getClass().getName()));
		}

		try {
			dataRead(ctx, (BinaryWebSocketFrame) frame);
		} catch (Exception e) {
			log.error(e);
		}
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// 返回应答给客户端
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	public void dataRead(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
		SocketTerminal terminal = channel2Terminal.get(ctx.channel());
		if(terminal == null){
			return;
		}
		ByteBuf b = msg.content();
		terminal.buf.writeBytes(b);
		
		int c = 0;
		while(c++ < 1000){ // 防止死循环
			if(terminal.readHead){
				if(terminal.buf.readableBytes() >= Constant.LENGTH_OF_NET_PACKAGE_HEADER){ // 读取数据头
					byte[] header = new byte[Constant.LENGTH_OF_NET_PACKAGE_HEADER];
					terminal.buf.readBytes(header, 0, Constant.LENGTH_OF_NET_PACKAGE_HEADER);
					terminal.dataSize = ((header[0] & 0x000000ff) << 24) + // java只有有符号数，通过&0xff去掉符号
							((header[1] & 0x000000ff) << 16) + // 这里byte在&时转成了int类型
							((header[2] & 0x000000ff) << 8) +
							((header[3] & 0x000000ff) << 0); // 数据大小，包含数据头
					if(terminal.dataSize > Constant.MAX_SIZE_OF_NET_MESSAGE){
						// 数据尺寸过大，可能是受到攻击，粘包
						log.warn("Receive Data out of size ! " + terminal.dataSize);
					}else if(terminal.dataSize <= 4){
						log.error("Receive Data length " + terminal.dataSize + " <= 4 ! ");
						terminal.quit(); // 数据异常，关闭socket
						return;
					}
					// log.info(header[0]+"　"+header[1]+" "+header[2]+" "+header[3]);
					terminal.recData = new byte[terminal.dataSize];
					terminal.offset = Constant.LENGTH_OF_NET_PACKAGE_HEADER;
					System.arraycopy(header, 0, terminal.recData, 0, Constant.LENGTH_OF_NET_PACKAGE_HEADER); // 拼回去
					terminal.readHead = false;
				}else{
					return;
				}
			}else{
				// 读取数据体
				int available = terminal.buf.readableBytes();
				if(available >= terminal.dataSize - terminal.offset){ // 这边读完之后可能socket缓存中还有很多数据，应该立马再读取
					terminal.buf.readBytes(terminal.recData, terminal.offset, terminal.dataSize - terminal.offset);
					terminal.readHead = true;
					// 添加到数据列表
					DataPackage data = new DataPackage(terminal.recData, terminal);
					if(!data.rpcPo.getRpc().equals("HeartRpc") && !data.rpcPo.getRpc().equals("HeartReq")){
						log.info("receive " + terminal.recData.length + " bytes " + data.rpcPo.toString());
					}
					if(data.rpcPo.getRpc().equals("HeartRpc") ||
							data.rpcPo.getRpc().equals("HeartReq")){
						terminal.heartTime = System.currentTimeMillis();
					}else{
						synchronized (NetManager.dataList) {
							NetManager.dataList.add(data);
						}
					}
					terminal.buf.discardReadBytes(); // 回收无用内存，不好调用频繁，消耗性能
				}else if(available > 0){ // 有就读，防止粘包
					terminal.buf.readBytes(terminal.recData, terminal.offset, available);
					terminal.offset += available;
				}else{
					return;
				}
			}
		}
		log.error("websocket 死循环");
		terminal.quit();
	}
}
