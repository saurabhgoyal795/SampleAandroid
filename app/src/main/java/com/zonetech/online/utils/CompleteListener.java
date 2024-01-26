package com.zonetech.online.utils;

import org.json.JSONObject;

public interface CompleteListener{
    public void success(JSONObject response);
    public void error(String error);
}