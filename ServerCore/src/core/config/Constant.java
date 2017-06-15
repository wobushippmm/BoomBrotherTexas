package core.config;

public class Constant {
	public static final int MAX_SIZE_OF_NET_MESSAGE = 65536; // 网络通信数据包最大尺寸
	public static final int LENGTH_OF_NET_PACKAGE_HEADER = 4; // 协议包头
	public static final int MAX_SIZE_OF_SINGLE_PACKAGE = 1024; // 单个包的长度
	public static final int TIME_OUT_OF_SAY_HELLO = 10000; // 服务器等待亮明身份超时时间
	public static final int DELAY_OF_RECONNECT = 1000; // 重新连接间隔
	public static final int TIME_OUT_OF_HEART = 10000; // 心跳超时
	public static final int DELAY_OF_HEART = 5000; // 心跳间隔时间
	public static final int MAX_LINK_OF_SERVER = 30; // 最大服务器连接数
	public static final int MAX_LINK_OF_CLIENT = 800; // 客户端连接最大数
	public static final int TIME_OUT_OF_SERVER_SOCKET_WAIT = 1000; // 接收连接等待超时
	public static final int TIME_OUT_OF_UDP_RECEIVE = 5; // udp接收数据超时
	public static final int SIZE_OF_UDP_CACHE = 8192; // udp包缓冲大小 8k
	
	public static final String TYPE_CLIENT = "client";
	public static final String TYPE_SOS = "sos";
	public static final String TYPE_LOGIN = "login";
	public static final String TYPE_GATEWAY = "gateway";
	public static final String TYPE_SCENE = "scene";
	public static final String TYPE_WORLD = "world";
	public static final String TYPE_BATTLE = "battle";
	public static final String TYPE_DATABASE = "database";
	public static final String TYPE_CHAT = "chat";
}
