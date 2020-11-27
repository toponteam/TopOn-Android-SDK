/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

public class FailRequestInfo {
    public String id;
    public int requestType; // 1:POST 2:GET 3:TCP
    public String headerJSONString;
    public String requestUrl;
    public String content;
    public long time;
}
