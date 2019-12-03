package com.yayachat.beans.pojo;

import lombok.Data;

@Data
public class ChatUserMsg {
    private Integer userId;
    private String avatarSrc;
    private String userName;
}
