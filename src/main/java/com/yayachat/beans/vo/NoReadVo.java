package com.yayachat.beans.vo;

import com.yayachat.beans.pojo.ChatNotice;
import lombok.Data;

import java.util.List;

@Data
public class NoReadVo {
    private List<ChatNotice> chatNoticeList;
    private Integer waitAgreeContactsNum;
}
