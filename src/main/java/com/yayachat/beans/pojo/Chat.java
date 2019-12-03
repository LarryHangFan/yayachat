package com.yayachat.beans.pojo;


import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Chat {
    private Integer id; //数据库id

    /**
     * 发送者用户ID
     */
    private String userSendId;
    /**
     * 接受者用户Id
     */
    private String userReceiveId;
    /**
     * 内容
     */
    private String content;
    /**
     * 创建时间
     */
    private String createTime;

    /*
     * 0 已读
     * 1 未读
     * 2 消息未发送给接收人
     *
     */
    private int state; //消息状态

    //消息类型(用于区分消息类型)
    private String messageType;  // 0 文字, 1 图片
}

