package com.yayachat.mapper2;

import com.yayachat.beans.pojo.ChatUserMsg;

import java.util.List;

//@Mapper
public interface UserMapper {
    List<ChatUserMsg> findChatUserMsgs(List<Integer> list);
    Integer findWaitAgreeContactsNum(Integer userId);
}
