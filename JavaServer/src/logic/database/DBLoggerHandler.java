package logic.database;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mysql.jdbc.PreparedStatement;

import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import logic.common.LogicManager;
import protocol.GameData.LogRpc;
import protocol.GameData.LogTypeEnm;

public class DBLoggerHandler {
	private Logger log = LoggerHelper.getLogger();
	public static DBLoggerHandler instance = null;
	
	public DBLoggerHandler(){
		instance = this;

		LogicManager.logicThread.setRpc(LogRpc.class, this);
		
	}
	
	public void onLogRpc(DataPackage data){
		try {
			if(NetManager.mysqlStmt == null){
				return;
			}
			LogRpc rpc = LogRpc.parseFrom(data.rpcPo.getAnyPo());
			String sql = "";
			PreparedStatement pstmt = null;
			try {
				if(rpc.getType() == LogTypeEnm.ADD_GOLD_LOG){
					sql = "INSERT INTO gold_log (username, gold, time) VALUES(?,?,?);";
					pstmt.setString(1, rpc.getUsername());
					pstmt.setInt(2, rpc.getGold());
					pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					pstmt.executeUpdate();
				}else if(rpc.getType() == LogTypeEnm.ERROR_LOG){
					sql = "INSERT INTO error_log (username, log, time, cause) VALUES(?,?,?,?);";
					pstmt.setString(1, rpc.getUsername());
					pstmt.setString(2, rpc.getLog());
					pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					pstmt.setInt(4, rpc.getCauseValue());
					pstmt.executeUpdate();
				}else if(rpc.getType() == LogTypeEnm.RECHARGE_LOG){
					sql = "INSERT INTO recharge_log (orderid, username, log, time) VALUES(?,?,?,?);";
					pstmt = (PreparedStatement) NetManager.mysqlConn.prepareStatement(sql);
					pstmt.setString(1, rpc.getOrderid());
					pstmt.setString(2, rpc.getUsername());
					pstmt.setString(3, rpc.getLog());
					pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					pstmt.executeUpdate();
				}else if(rpc.getType() == LogTypeEnm.GMCOMMAND_LOG){
					sql = "INSERT INTO gmcmd_log (username, cmd, args, time, gm) VALUES(?,?,?,?,?);";
					pstmt = (PreparedStatement) NetManager.mysqlConn.prepareStatement(sql);
					pstmt.setString(1, rpc.getUsername());
					pstmt.setString(2, rpc.getCmd());
					pstmt.setString(3, rpc.getArgs());
					pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					pstmt.setString(5, rpc.getGm());
					pstmt.executeUpdate();
				}
			} catch (SQLException e) {
				log.error(e);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
}
