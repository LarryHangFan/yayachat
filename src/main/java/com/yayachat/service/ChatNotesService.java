package com.yayachat.service;

import com.yayachat.beans.pojo.Chat;
import com.yayachat.beans.pojo.ChatNotice;

import java.util.List;

public interface ChatNotesService {
    Boolean insertChatNotes(Chat chat);
    Boolean insertChatNotes(List<Chat> list);  //批量插入
    Boolean upadteChatNotes(Chat chat);
    List<Chat> findChatnotes(Chat chat);
    List<Chat> findChatnotes2(Chat chat);  //查询未发送和未读的消息
    List<ChatNotice> findOnePeopleploChatNotesNumber(Integer userReceiveId); //查询某人未读的消息条数
    int findChatNotesNumber(Integer userReceiveId); //查询所有未读的消息条数
    Integer findWaitAgreeContactsNum(Integer userId);
}
