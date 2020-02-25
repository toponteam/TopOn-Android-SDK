package com.anythink.core.common.entity;

import org.json.JSONObject;

public class AgentInfoBean extends LoggerInfoInterface {

    public String key;
    public String requestId;
    public String unitId;
    public String psid;
    public String sessionId;
    public String groupId;
    public String unitgroupId;
    public String timestamp;
    public String asid;
    public String refresh;
    public String msg;
    public String msg1;
    public String msg2;
    public String msg3;
    public String msg4;
    public String msg5;
    public String msg6;
    public String msg7;
    public String msg8;
    public String msg9;
    public String msg10;
    public String msg11;


    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", key);
            jsonObject.put("requestid", requestId);
            jsonObject.put("unitid", unitId);
            jsonObject.put("psid", psid);
            jsonObject.put("sessionid", sessionId);
            jsonObject.put("groupid", groupId);
            jsonObject.put("unitgroupid", unitgroupId);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("asid", asid);
            jsonObject.put("refresh", refresh);
            jsonObject.put("msg", msg);
            jsonObject.put("msg1", msg1);
            jsonObject.put("msg2", msg2);
            jsonObject.put("msg3", msg3);
            jsonObject.put("msg4", msg4);
            jsonObject.put("msg5", msg5);
            jsonObject.put("msg6", msg6);
            jsonObject.put("msg7", msg7);
            jsonObject.put("msg8", msg8);
            jsonObject.put("msg9", msg9);
            jsonObject.put("msg10", msg10);
            jsonObject.put("msg11", msg11);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
