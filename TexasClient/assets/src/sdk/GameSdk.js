var GameSdk = {};

// 用户系统
/*
var info = {
    server_id   : "2",
    server_url  : "http://xxx.xxx.xxx",
    key1        : "value1",
    key2        : "value2"
};
*/
GameSdk.login = function(info){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.login){
        return;
    }
    user_plugin.login(info);
};

GameSdk.isLogined = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.isLogined){
        return;
    }
    return user_plugin.isLogined();
};

GameSdk.getUserID = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.getUserPlugin){
        return;
    }
    return user_plugin.getUserID();
};

GameSdk.logout = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.logout){
        return;
    }
    user_plugin.logout();
};

GameSdk.enterPlatform = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.enterPlatform){
        return;
    }
    user_plugin.enterPlatform();
};

/*
ToolBarPlace.kToolBarTopLeft	    value=1；左上角
ToolBarPlace.kToolBarTopRight	    value=2；右上角
ToolBarPlace.kToolBarMidLeft	    value=3；左边中间
ToolBarPlace.kToolBarMidRight	    value=4；右边中间
ToolBarPlace.kToolBarBottomLeft	    value=5；右下角
ToolBarPlace.kToolBarBottomRight	value=6；右下角
*/
GameSdk.showToolBar = function(place){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.showToolBar){
        return;
    }
    user_plugin.showToolBar(place);
};

GameSdk.hideToolBar =function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.hideToolBar){
        return;
    }
    user_plugin.hideToolBar();
};

GameSdk.accountSwitch = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.accountSwitch){
        return;
    }
    user_plugin.accountSwitch();
};

GameSdk.exit = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.exit){
        return;
    }
    user_plugin.exit();
};

GameSdk.pause = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.pause){
        return;
    }
    user_plugin.pause();
};

GameSdk.realNameRegister = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.realNameRegister){
        return;
    }
    user_plugin.realNameRegister();
};

GameSdk.antiAddictionQuery = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.antiAddictionQuery){
        return;
    }
    user_plugin.antiAddictionQuery();
};

/*
dataType	    Y	数据类型，1 为进入游戏，2 为创建角色，3 为角色升级，4 为退出
roleId	        Y	角色 ID
roleName	    Y	角色名称
roleLevel	    Y	角色等级
zoneId	        Y	服务器 ID
zoneName	    Y	服务器名称
balance	        Y	用户余额（RMB 购买的游戏币）
partyName	    Y	帮派、公会等
vipLevel	    Y	VIP 等级
roleCTime	    Y	角色创建时间（单位：秒）（历史角色没记录时间的传 -1，新创建的角色必须要）
roleLevelMTime	Y	角色等级变化时间（单位：秒）（创建角色和进入游戏时传 -1）
*/
/*
var data = {
    dataType:"1",
    roleId:"123456",
    roleName:"test",
    roleLevel:"1",
    zoneId:"1",
    zoneName:"test",
    balance:"1",
    partyName:"test",
    vipLevel:"1",
    roleCTime:"1480318110",
    roleLevelMTime:"-1"
}
*/
GameSdk.submitLoginGameRole = function(data){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.submitLoginGameRole){
        return;
    }
    user_plugin.submitLoginGameRole(data);
};

GameSdk.setUserListener = function(){
    var agent = anysdk.agentManager;
    var user_plugin = agent.getUserPlugin();
    if(!user_plugin || !user_plugin.setListener){
        return;
    }
    user_plugin.setListener(GameSdk.onUserResult, GameSdk);
};

GameSdk.onUserResult = function(code, msg){
    cc.log('on user result action');
    cc.log('msg:'+msg);
    cc.log('code:'+code);
    switch(code){
        case anysdk.UserActionResultCode.kInitSuccess:

            break;
        case anysdk.UserActionResultCode.kInitFail:

            break;
        case anysdk.UserActionResultCode.kLoginSuccess:

            break;
        case anysdk.UserActionResultCode.kLoginNetworkError:

            break;
        case anysdk.UserActionResultCode.kLoginCancel:

            break;
        case anysdk.UserActionResultCode.kLoginFail:
        
            break;
        case anysdk.UserActionResultCode.kLogoutSuccess:

            break;
        case anysdk.UserActionResultCode.kLogoutFail:

            break;
        case anysdk.UserActionResultCode.kPlatformEnter:

            break;
        case anysdk.UserActionResultCode.kPlatformBack:

            break;
        case anysdk.UserActionResultCode.kAccountSwitchSuccess:

            break;
        case anysdk.UserActionResultCode.kAccountSwitchFail:
        
            break;
        case anysdk.UserActionResultCode.kAccountSwitchCancel:

            break;
        case anysdk.UserActionResultCode.kExitPage:

            break;
        case anysdk.UserActionResultCode.kRealNameRegister:

            break;
        case anysdk.UserActionResultCode.kAntiAddictionQuery:

            break;
    };
}

// 支付系统
/*
Product_Id	    Y	商品 ID（联想、七匣子、酷派等商品 ID 要与在渠道后台配置的商品 ID 一致）
参数类型：字符串
Product_Name	Y	商品名
参数类型：字符串
Product_Price	Y	商品价格（元），可能有的 SDK 只支持整数
参数类型：字符串
Product_Count	Y	商品份数（除非游戏需要支持一次购买多份商品，否则传 1 即可）
参数类型：字符串
Product_Desc	N	商品描述（不传则使用 Product_Name）
参数类型：字符串
Coin_Name	    Y	虚拟币名称（如金币、元宝）
参数类型：字符串
Coin_Rate	    Y	虚拟币兑换比例（例如 100，表示 1 元购买 100 虚拟币）
参数类型：字符串
Role_Id	        Y	游戏角色 ID 
参数类型：字符串
Role_Name	    Y	游戏角色名
参数类型：字符串
Role_Grade	    Y	游戏角色等级
参数类型：字符串
Role_Balance	Y	用户游戏内虚拟币余额，如元宝，金币，符石
参数类型：字符串
Vip_Level	    Y	VIP 等级 
参数类型：字符串
Party_Name	    Y	帮派、公会等
参数类型：字符串
Server_Id	    Y	服务器 ID，若无填 “1”
参数类型：字符串
Server_Name	    Y	服务器名
参数类型：字符串
EXT	            N	扩展字段
参数类型：字符串，可以使用 JSON 型字符串。
*/
GameSdk.payForProduct = function(productInfo){
    var agent = anysdk.agentManager;
    var iap_plugin = agent.getIAPPlugin();
    if(!iap_plugin || !iap_plugin.payForProduct){
        return;
    }
    iap_plugin.payForProduct(productInfo);
};

GameSdk.getOrderId = function(){
    var agent = anysdk.agentManager;
    var iap_plugin = agent.getIAPPlugin();
    if(!iap_plugin || !iap_plugin.getOrderId){
        return;
    }
    return iap_plugin.getOrderId();
};

GameSdk.resetPayState = function(){
    if(!anysdk.ProtocolIAP || !anysdk.ProtocolIAP.resetPayState){
        return;
    }
    anysdk.ProtocolIAP.resetPayState();
};

GameSdk.setIAPListener = function(){
    var agent = anysdk.agentManager;
    var iap_plugin = agent.getIAPPlugin();
    if(!iap_plugin || !iap_plugin.setListener){
        return;
    }
    iap_plugin.setListener(GameSdk.onPayResult, GameSdk);
};

GameSdk.onPayResult = function(code, msg, info){
    cc.log('pay result, resultcode: '+code+', msg: '+msg);
    switch(code){
        case anysdk.PayResultCode.kPaySuccess:

            break;
        case anysdk.PayResultCode.kPayCancel:

            break;
        case anysdk.PayResultCode.kPayFail:

            break;
        case anysdk.PayResultCode.kPayNetworkError:

            break;
        case anysdk.PayResultCode.kPayProductionInforIncomplete:

            break;
    }
};

// 统计系统
GameSdk.startSession = function(){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.startSession){
        return;
    }
    analytics_plugin.startSession();
};

GameSdk.stopSession = function(){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.stopSession){
        return;
    }
    analytics_plugin.stopSession();
};

/*
使用自定义事件功能请先该统计 SDK 的管理后台添加相应的事件后，服务器才会对相应的事件请求进行处理。
自定义事件的代码需要放在 Activity 里面的 onResume 方法后面。
analytics_plugin.logEvent("1", {test:"123"})
*/
GameSdk.logEvent = function(name, data){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.logEvent){
        return;
    }
    analytics_plugin.logEvent(name, data);
};

GameSdk.setSessionContinueMillis = function(millis){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.setSessionContinueMillis){
        return;
    }
    analytics_plugin.setSessionContinueMillis(millis);
};

GameSdk.setCaptureUncaughtException = function(capture){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.setCaptureUncaughtException){
        return;
    }
    analytics_plugin.setCaptureUncaughtException(capture);
};

/*
Account_Id	    Y	游戏中玩家 ID
Account_Name	Y	游戏中玩家昵称
Account_Type	Y	传入帐户的类型（ANONYMOUS 匿名，REGISTED 自有账号，SINA_WEIBO 新浪微博，TENCENT_WEIBO 腾讯微博，QQ，ND91）
Account_Level	Y	游戏中玩家等级
Account_Age	    Y	玩家年龄
Account_Operate	Y	账户操作（LOGIN 登陆，LOGOUT 登出，REGISTER 注册）
Account_Gender	Y	游戏角色性别（UNKNOWN 未知，FEMALE 女性，MALE 男性）
Server_Id	    Y	服务器 ID
*/
GameSdk.setAccount = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.setAccount){
        return;
    }
    analytics_plugin.setAccount(paramMap);
};

/*
Order_Id	            Y	订单唯一 ID
Product_Name	        Y	商品名
Currency_Amount	        Y	现金数额（元）
Currency_Type	        Y	请使用国际标准组织 ISO 4217 中规范的 3 位字母￼代码标记货币类型。点击查看参考例：人民币 CNY；美元 USD；欧元 EUR
Payment_Type	        Y	支付的途径，最多 16 个字符。例如：“支付宝”“苹果官方”“XX 支付 SDK
Virtual_Currency_Amount	Y	虚拟币数值
*/
GameSdk.onChargeRequest = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onChargeRequest){
        return;
    }
    analytics_plugin.onChargeRequest(paramMap);
};

GameSdk.onChargeSuccess = function(orderId){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onChargeSuccess){
        return;
    }
    analytics_plugin.onChargeSuccess(orderId);
};

/*
Order_Id	Y	订单唯一 ID
Fail_Reason	Y	失败原因
*/
GameSdk.onChargeFail = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onChargeFail){
        return;
    }
    analytics_plugin.onChargeFail(paramMap);
};

/*
Order_Id	            Y	订单唯一 ID
Product_Name	        Y	商品名
Currency_Amount	        Y	现金数额（元）
Currency_Type	        Y	请使用国际标准组织 ISO 4217 中规范的 3 位字母￼代码标记货币类型。点击查看参考例：人民币 CNY；美元 USD；欧元 EUR
Payment_Type	        Y	支付的途径，最多 16 个字符。例如：“支付宝”“苹果官方”“XX 支付 SDK
Virtual_Currency_Amount	Y	虚拟币数值
*/
GameSdk.onChargeOnlySuccess = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onChargeOnlySuccess){
        return;
    }
    analytics_plugin.onChargeOnlySuccess(paramMap);
};

/*
Item_Id	            Y	物品 ID
Item_Type	        Y	物品类型
Item_Count	        Y	物品数量
Virtual_Currency	Y	虚拟金额
Currency_Type	    Y	虚拟币类型
*/
GameSdk.onPurchase = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onPurchase){
        return;
    }
    analytics_plugin.onPurchase(paramMap);
};

/*
Item_Id	    Y	物品 ID
Item_Type	Y	物品类型
Item_Count	Y	物品数量
Use_Reason	Y	用途说明
*/
GameSdk.onUse = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onUse){
        return;
    }
    analytics_plugin.onUse(paramMap);
};

/*
Item_Id	    Y	物品 ID
Item_Type	Y	物品类型
Item_Count	Y	物品数量
Item_Price	Y	物品价格
Use_Reason	Y	用途说明
*/
GameSdk.onReward = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.onReward){
        return;
    }
    analytics_plugin.onReward(paramMap);
};

/*
Level_Id	Y	关卡 ID
Seq_Num	    Y	关卡顺序
*/
GameSdk.startLevel = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.startLevel){
        return;
    }
    analytics_plugin.startLevel(paramMap);
};

GameSdk.finishLevel = function(levelID){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.finishLevel){
        return;
    }
    analytics_plugin.finishLevel(levelID);
};

/*
Level_Id	Y	关卡 ID
Fail_Reason	Y	失败原因
*/
GameSdk.failLevel = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.failLevel){
        return;
    }
    analytics_plugin.failLevel(paramMap);
};

/*
Task_Id	    Y	任务 ID
Task_Type	Y	任务类型（GUIDE_LINE 新手引导、MAIN_LINE 主线、 BRANCH_LINE 分支、 DAILY 日常、 ACTIVITY 活动、 OTHER 其他）
*/
GameSdk.startTask = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.startTask){
        return;
    }
    analytics_plugin.startTask(paramMap);
};

GameSdk.finishTask = function(taskID){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.finishTask){
        return;
    }
    analytics_plugin.finishTask(taskID);
};

/*
Task_Id	    Y	任务 ID
Fail_Reason	Y	失败原因
*/
GameSdk.failTask = function(paramMap){
    var agent = anysdk.agentManager;
    var analytics_plugin = agent.getAnalyticsPlugin();
    if(!analytics_plugin || !analytics_plugin.failTask){
        return;
    }
    analytics_plugin.failTask(paramMap);
};

module.exports = GameSdk;