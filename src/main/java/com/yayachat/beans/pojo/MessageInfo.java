package com.yayachat.beans.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Data
public class MessageInfo {
    //源客户端id
    private String sourceClientId;
    //目标客户端id
    private String targetClientId;
    //消息内容
    private String msg;

    //消息时间
    private String dateTime;
    //消息类型(用于区分消息类型)
    private String messageType;
}

