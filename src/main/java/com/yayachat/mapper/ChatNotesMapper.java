package com.yayachat.mapper;

import com.yayachat.beans.pojo.Chat;
import com.yayachat.beans.pojo.ChatNotice;


import java.util.List;

//@Mapper
public interface ChatNotesMapper {
    void insertChatNotes(Chat chat);
    void batchInsertChatNotes(List<Chat> list);  //批量插入
    void upadteChatNotes(Chat chat);
    List<Chat> findChatnotes(Chat chat);  //查询未发送的消息
    List<Chat> findChatnotes2(Chat chat);  //查询未发送和未读的消息
    List<ChatNotice> findOnePeopleploChatNotesNumber(Integer userReceiveId); //查询某人未读的消息条数
    int findChatNotesNumber(Integer userReceiveId); //查询所有未读的消息条数
}
