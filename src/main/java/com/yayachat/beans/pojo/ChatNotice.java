package com.yayachat.beans.pojo;

import lombok.Data;

@Data
public class ChatNotice {
    //源客户端id
    private String sourceClientId;
    //目标客户端id
    private String targetClientId;
    //未读数量
    private Integer num;
    /**
     * 最新内容
     */
    private String msg;
    /**
     * 创建时间
     */
    private String dateTime;
    //消息类型(用于区分消息类型)
    private String messageType;
    //用户名
    private String userName;
    //头像
    private String avatarSrc;
}
