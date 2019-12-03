package com.yayachat.service.impl;

import com.yayachat.beans.pojo.Chat;
import com.yayachat.beans.pojo.ChatNotice;
import com.yayachat.beans.pojo.ChatUserMsg;
import com.yayachat.mapper.ChatNotesMapper;
import com.yayachat.mapper2.UserMapper;
import com.yayachat.service.ChatNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatNotesServiceImpl implements ChatNotesService {

    @Autowired
    private ChatNotesMapper chatNotesMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Integer findWaitAgreeContactsNum(Integer userId) {
        return userMapper.findWaitAgreeContactsNum(userId);
    }

    @Override
    public  List<ChatNotice> findOnePeopleploChatNotesNumber(Integer userReceiveId) {
        List<ChatNotice> list = chatNotesMapper.findOnePeopleploChatNotesNumber(userReceiveId);
        //存储id
        List<Integer> list2 = new ArrayList<>();
        if(list!=null){
            for(int i = 0;i<list.size();i++){
                list2.add(Integer.parseInt(list.get(i).getTargetClientId()));
            }
        }
        List<ChatUserMsg> msgs = new ArrayList<>();
        //查询用户信息
       if(list2!=null&&list2.size()>0){
           msgs = userMapper.findChatUserMsgs(list2);
       }
        for(int i = 0;i<list.size();i++){
            Integer id = Integer.parseInt(list.get(i).getTargetClientId());
            for(int j =0;j<msgs.size();j++){
                if(msgs.get(j)!=null&&msgs.get(j).getUserId()==id){
                    list.get(i).setAvatarSrc(msgs.get(j).getAvatarSrc());
                    list.get(i).setUserName(msgs.get(j).getUserName());
                }
            }
        }
      // System.out.println(list);
        return list;
    }

    @Override
    public int findChatNotesNumber(Integer userReceiveId) {
        return chatNotesMapper.findChatNotesNumber(userReceiveId);
    }

    @Override
    public List<Chat> findChatnotes2(Chat chat) {
        return  chatNotesMapper.findChatnotes2(chat);
    }

    @Override
    public Boolean insertChatNotes(Chat chat) {
       // try{
            chatNotesMapper.insertChatNotes(chat);
            return true;
//        }catch (Exception e){
//            return false;
//        }
    }

    @Override
    public Boolean upadteChatNotes(Chat chat) {
       // try{
            chatNotesMapper.upadteChatNotes(chat);
            return true;
       // }catch (Exception e){
        //    return false;
        //}
    }

    @Override
    public List<Chat> findChatnotes(Chat chat) {
       // try{
            return  chatNotesMapper.findChatnotes(chat);

      //  }catch (Exception e){
        //    return null;
      //  }
    }

    @Override
    public Boolean insertChatNotes(List<Chat> list) {
        chatNotesMapper.batchInsertChatNotes(list);
        return true;
    }

}
