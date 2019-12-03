package com.yayachat.beans.pojo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChatImgs {
    //源客户端id
    private String sourceClientId;
    //目标客户端id
    private String targetClientId;
    //聊天发送的图片
    //private Base64Phone[] phoneFiles;
    private String[] msg;
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
