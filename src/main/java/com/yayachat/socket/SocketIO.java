package com.yayachat.socket;


import com.yayachat.beans.pojo.Chat;
import com.yayachat.beans.pojo.ChatImgs;
import com.yayachat.beans.pojo.ClientInfo;
import com.yayachat.beans.pojo.MessageInfo;
import com.yayachat.service.ChatNotesService;
import com.yayachat.utils.QiuNiuYunUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component("SocketIO")
@Slf4j
public class SocketIO implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private ChatNotesService chatNotesService;

	SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	String time = simpleDateFormat.format(new Date());

	//会话集合
	private static final ConcurrentSkipListMap<String, ClientInfo> webSocketMap = new ConcurrentSkipListMap<>();
	//静态变量，用来记录当前在线连接数。（原子类、线程安全）
	private static AtomicInteger onlineCount = new AtomicInteger(0);

	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		System.out.println("socket类开启");
		//if (arg0.getApplicationContext().getParent() != null) {// root application context 有parent，他就是儿子.
			// 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
			System.out.println("socket类线程开始");
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
	//	config.setHostname("localhost");
		//config.setHostname("0.0.0.0");
		config.setPort(7777);

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
				String targetClientId = client.getHandshakeData().getSingleUrlParam("targetclientid");
				log.info("web socket连接:"+clientId+" 被链接："+targetClientId);
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
				//查询上次连接，未发送和未读的的消息(并发送)
				Chat chat = new Chat();
				chat.setUserSendId(targetClientId);
				chat.setUserReceiveId(clientId);
				//chat.setState(2);
			    List<Chat> chats = chatNotesService.findChatnotes2(chat);

				//发送未发送的消息
                if(chats!=null&&chats.size()>0){

					List<MessageInfo> list = new ArrayList<>();
					for(int i =0;i<chats.size();i++){
						MessageInfo sendData = new MessageInfo();
						sendData.setSourceClientId(targetClientId);
						sendData.setTargetClientId(clientId);
						sendData.setMsg(chats.get(i).getContent());
						sendData.setMessageType(chats.get(i).getMessageType());
						sendData.setDateTime(chats.get(i).getCreateTime()!=null?chats.get(i).getCreateTime():time);
						list.add(sendData);
					}

					// 向目标会话发送信息
					ClientInfo clientInfo = webSocketMap.get(clientId);
					UUID client_self = new UUID(clientInfo.getMostSignificantBits(), clientInfo.getLeastSignificantBits());
					server.getClient(client_self).sendEvent("message_event", list);
					//信息发送成功，修改数据未发信息的状态，改为已发(这这里是已读)
//					chat.setUserSendId(clientId);
//					chat.setUserReceiveId(targetClientId);
					chat.setState(0);
					chatNotesService.upadteChatNotes(chat);
				}
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
		//添加发送消息事件监听
		server.addEventListener("message_event", MessageInfo.class,  new DataListener<MessageInfo>(){
			@Override
			public void onData(SocketIOClient client, MessageInfo data, AckRequest ackSender){


				Chat chat  = new Chat();
				chat.setUserSendId(data.getSourceClientId());
				chat.setUserReceiveId(data.getTargetClientId());
				chat.setContent(data.getMsg());
				chat.setCreateTime(data.getDateTime()!=null?data.getDateTime():time);
				chat.setMessageType(data.getMessageType());
				System.out.println("发送信息："+chat);
				//这里保存聊天记录
				String targetClientId = data.getTargetClientId();
				ClientInfo clientInfo = webSocketMap.get(targetClientId);
				System.out.println("clientInfo："+clientInfo);

				if (clientInfo != null && clientInfo.isOnline()){
					UUID target = new UUID(clientInfo.getMostSignificantBits(), clientInfo.getLeastSignificantBits());
					log.info("目标会话UUID:"+target);
					MessageInfo sendData = new MessageInfo();
					sendData.setSourceClientId(data.getSourceClientId());
					sendData.setTargetClientId(data.getTargetClientId());
					sendData.setMsg(data.getMsg());
					sendData.setDateTime(data.getDateTime());
					sendData.setMessageType(data.getMessageType());
					// 向当前会话发送信息
					//client.sendEvent("message_event", sendData);
					// 向目标会话发送信息
					try{
						server.getClient(target).sendEvent("message_event", sendData);
						//消息发送成功，存入数据库
						chat.setState(0);
						try{
							chatNotesService.insertChatNotes(chat);
						}catch (Exception e1) {

						}
					}catch (Exception e){
						//连接错误,消息未发送存入数据库
						chat.setState(2);
						try{
							chatNotesService.insertChatNotes(chat);
						}catch (Exception e1){

						}

					}

				}else{
					//客服端不在线,消息未发送存入数据库
					chat.setState(2);
					try{
						chatNotesService.insertChatNotes(chat);
					}catch (Exception e1){

					}
				}
			}

		});

		//用户离开聊天界面
		server.addEventListener("leave_chat", MessageInfo.class,  new DataListener<MessageInfo>(){
			@Override
			public void onData(SocketIOClient client, MessageInfo data, AckRequest ackSender) throws Exception {
				webSocketMap.remove(data.getSourceClientId());
			}

		});


		//用户发送图片
		server.addEventListener("send_img", ChatImgs.class,  new DataListener<ChatImgs>(){
			@Override
			public void onData(SocketIOClient client, ChatImgs data, AckRequest ackSender) throws Exception {
				if(data.getMsg()!=null&&data.getMsg().length>0){
					ClientInfo clientInfo = webSocketMap.get(data.getTargetClientId());
					List<Chat> chats = new ArrayList<>();
					if (clientInfo != null && clientInfo.isOnline()){
						UUID target = new UUID(clientInfo.getMostSignificantBits(), clientInfo.getLeastSignificantBits());
						log.info("目标会话UUID:"+target);
						//发送至客户端
						List<MessageInfo> list = new ArrayList<>();
						for(int i =0;i<data.getMsg().length;i++){

							MessageInfo sendData = new MessageInfo();
							sendData.setSourceClientId(data.getSourceClientId());
							sendData.setTargetClientId(data.getTargetClientId());
							sendData.setMsg(data.getMsg()[i]);
							sendData.setMessageType(data.getMessageType());
							sendData.setDateTime(data.getCreateTime()!=null?data.getCreateTime():time);
							list.add(sendData);
						}
						// 向当前会话发送信息
						//client.sendEvent("message_event", sendData);
						// 向目标会话发送信息
						try{
							server.getClient(target).sendEvent("message_event", list);
							//消息发送成功，存入数据库
							for(int i = 0;i<data.getMsg().length;i++){
								Chat chat = new Chat();
								//String chatimg	= QiuNiuYunUtils.uploadImg(data.getPhoneFiles()[i].getPhoneFile(),data.getPhoneFiles()[i].getPhoneFileLength(),"yaya-chatimg");
								chat.setMessageType(data.getMessageType());
								chat.setState(1);
								chat.setUserReceiveId(data.getTargetClientId());
								chat.setUserSendId(data.getSourceClientId());
								chat.setContent(data.getMsg()[i]);
								chat.setCreateTime(data.getCreateTime()!=null?data.getCreateTime():time);
								chats.add(chat);
							}

							try{
								//存入数据库
								chatNotesService.insertChatNotes(chats);
							}catch (Exception e1){

							}
						}catch (Exception e){
							//连接错误,消息未发送存入数据库
							for(int i = 0;i<data.getMsg().length;i++){
								Chat chat = new Chat();
								//String chatimg	= QiuNiuYunUtils.uploadImg(data.getPhoneFiles()[i].getPhoneFile(),data.getPhoneFiles()[i].getPhoneFileLength(),"yaya-chatimg");
								chat.setMessageType(data.getMessageType());
								chat.setState(2);
								chat.setUserReceiveId(data.getTargetClientId());
								chat.setUserSendId(data.getSourceClientId());
								chat.setContent(data.getMsg()[i]);
								chat.setCreateTime(data.getCreateTime()!=null?data.getCreateTime():time);
								chats.add(chat);
							}

							try{
								//存入数据库
								chatNotesService.insertChatNotes(chats);
							}catch (Exception e1){

							}

						}
					}else{
						//客服端不在线,消息未发送存入数据库
						for(int i = 0;i<data.getMsg().length;i++){
							Chat chat = new Chat();
							//String chatimg	= QiuNiuYunUtils.uploadImg(data.getPhoneFiles()[i].getPhoneFile(),data.getPhoneFiles()[i].getPhoneFileLength(),"yaya-chatimg");
							chat.setMessageType(data.getMessageType());
							chat.setState(2);
							chat.setUserReceiveId(data.getTargetClientId());
							chat.setUserSendId(data.getSourceClientId());
							chat.setContent(data.getMsg()[i]);
							chat.setCreateTime(data.getCreateTime()!=null?data.getCreateTime():time);
							chats.add(chat);
						}
						try{
							//存入数据库
							chatNotesService.insertChatNotes(chats);
						}catch (Exception e1){

						}
					}

				}
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

