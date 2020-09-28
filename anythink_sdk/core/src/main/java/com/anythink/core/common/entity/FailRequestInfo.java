package com.anythink.core.common.entity;

public class FailRequestInfo {
    public String id;
    public int requestType; // 1:POST 2:GET 3:TCP
    public String headerJSONString;
    public String requestUrl;
    public String content;
    public long time;
}
