package com.yayachat.socket;


import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.yayachat.beans.pojo.*;
import com.yayachat.beans.vo.NoReadVo;
import com.yayachat.service.ChatNotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
  获取未读消息数量
 */
@Component("SocketIO_2")
@Slf4j
public class SocketIO_2 implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private ChatNotesService chatNotesService;

	SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	String time = simpleDateFormat.format(new Date());

	//会话集合
	private static final ConcurrentSkipListMap<String, ClientInfo> webSocketMap = new ConcurrentSkipListMap<>();
	//静态变量，用来记录当前在线连接数。（原子类、线程安全）
	private static AtomicInteger onlineCount = new AtomicInteger(0);

	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		System.out.println("socket2类开启");
		//if (arg0.getApplicationContext().getParent() != null) {// root application context 有parent，他就是儿子.
			// 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
			System.out.println("socket2类线程开始");
			new Thread(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					socketStart();
				}
			}).start();
		//}

	}

	private void socketStart() {
		System.out.println("in socketio");

		Configuration config = new Configuration();
	//	config.setHostname("0.0.0.0");
		config.setPort(7778);

		SocketConfig sockConfig = new SocketConfig();
		//地址服用，这时候再启动不报错
		sockConfig.setReuseAddress(true);
		
		//设置使用的协议和轮询方式
		config.setTransports( Transport.WEBSOCKET,Transport.POLLING);
		//设置允许源
		//config.setOrigin(":*:");

		config.setSocketConfig(sockConfig);
		//允许最大帧长度
		config.setMaxFramePayloadLength(1024 * 1024);
		//允许下最大内容
		config.setMaxHttpContentLength(1024 * 1024);
		SocketIOServer server = new SocketIOServer(config);

		//添加链接事件监听
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				String time = simpleDateFormat.format(new Date());

				String clientId = client.getHandshakeData().getSingleUrlParam("clientid");
			//	String targetClientId = client.getHandshakeData().getSingleUrlParam("targetclientid");
			//	log.info("web socket连接:"+clientId+" 被链接："+targetClientId);
				UUID session = client.getSessionId();
				ClientInfo si = webSocketMap.get(clientId);
				// 如果没有连接信息、则新建会话信息
				if (si == null) {
					si = new ClientInfo();
					si.setOnline(true);
					//在线数加1
					log.info("socket 建立新连接、sessionId:"+session+"、clientId:"+clientId+"、当前连接数："+onlineCount.incrementAndGet());
				}
				// 更新设置客户端连接信息
                si.setClientId(clientId);
				si.setLeastSignificantBits(session.getLeastSignificantBits());
				si.setMostSignificantBits(session.getMostSignificantBits());
				si.setLastConnectedTime(new Date());
				//将会话信息更新保存至集合中
				webSocketMap.put(clientId, si);


			}
		});
		//添加销毁链接事件监听
		server.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				String clientId = client.getHandshakeData().getSingleUrlParam("clientid");
				webSocketMap.remove(clientId);
				//在线数减1
				log.info("socket 断开连接、sessionId:"+client.getSessionId()+"、clientId:"+clientId+"、当前连接数："+ onlineCount.decrementAndGet());

			}
		});


		//用户离开聊天界面
		server.addEventListener("leave_chat", MessageInfo.class,  new DataListener<MessageInfo>(){
			@Override
			public void onData(SocketIOClient client, MessageInfo data, AckRequest ackSender) throws Exception {
				webSocketMap.remove(data.getSourceClientId());
			}

		});


		//得到某人未读的消息
		server.addEventListener("get_noReadNum", ChatNotice.class,  new DataListener<ChatNotice>(){
			@Override
			public void onData(SocketIOClient client, ChatNotice data, AckRequest ackSender) throws Exception {
				//System.out.println(data);
				if(data.getSourceClientId()==null){
					return ;
				}
				ClientInfo clientInfo = webSocketMap.get(data.getSourceClientId());
				if(clientInfo==null){
					return ;
				}
				UUID target = new UUID(clientInfo.getMostSignificantBits(), clientInfo.getLeastSignificantBits());
                   //查询未读数量
				List<ChatNotice> nums = chatNotesService.findOnePeopleploChatNotesNumber(Integer.parseInt(data.getSourceClientId()));
				//data.setNum(num);
				//发送
				//System.out.println(nums);
				//查询带同意的联系人数量
				Integer waitAgreeContactsNum = chatNotesService.findWaitAgreeContactsNum(Integer.parseInt(data.getSourceClientId()));
				NoReadVo noReadVo  = new NoReadVo();
				noReadVo.setChatNoticeList(nums);
				noReadVo.setWaitAgreeContactsNum(waitAgreeContactsNum);
				server.getClient(target).sendEvent("get_noReadNum", noReadVo);
			}
		});

		//所有未读的消息
		server.addEventListener("get_noReadCount", ChatNotice.class,  new DataListener<ChatNotice>(){
			@Override
			public void onData(SocketIOClient client, ChatNotice data, AckRequest ackSender) throws Exception {
				ClientInfo clientInfo = webSocketMap.get(data.getSourceClientId());
				UUID target = new UUID(clientInfo.getMostSignificantBits(), clientInfo.getLeastSignificantBits());
				//查询未读数量
				int count = chatNotesService.findChatNotesNumber(Integer.parseInt(data.getSourceClientId()));
				data.setNum(count);
				//发送
				server.getClient(target).sendEvent("get_noReadCount", data);
			}
		});

		server.start();
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
	}
}

