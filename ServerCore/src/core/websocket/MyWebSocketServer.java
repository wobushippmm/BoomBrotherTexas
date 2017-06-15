package core.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ClientThread;
import core.net.NetManager;
import core.net.ServerThread;
import core.net.SocketTerminal;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SystemPropertyUtil;

public class MyWebSocketServer extends Thread{
	private Logger log = LoggerHelper.getLogger();
	public int port = 0;
	public ServerBootstrap bootstrap = null;
	public ChannelFuture future = null;
	
	public MyWebSocketServer(int port) {
		this.port = port;
	}

	
	public void quit(){
		if(future != null){
			future.channel().close();
		}
	}
	
//	static{
//		System.setProperty( "io.netty.selectorAutoRebuildThreshold", "10");
//	}
	
	public void run(){
		EventLoopGroup boosGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(boosGroup, workGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new IdleStateHandler(30, 0, 0));
						ch.pipeline().addLast(new HttpServerCodec());
						ch.pipeline().addLast(new HttpObjectAggregator(65536));
						ch.pipeline().addLast(new WebSocketServerHandler());
						
					}
				});

			future = bootstrap.bind(port).sync();
			log.info("WebSocket start, wait for client ......");
			future.channel().closeFuture().sync();
			log.info("WebSocket------------------>close");
		} catch (InterruptedException e) {
			log.error("WebSocketServer", e);
		}finally {
			boosGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
}
