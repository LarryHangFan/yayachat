package com.yayachat.beans.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * nettyIo 实体类
 */
@Component
@Data
public class ClientInfo {
    private String clientId;
    private boolean isOnline;
    private long mostSignificantBits;
    private long leastSignificantBits;
    private Date lastConnectedTime;
}
