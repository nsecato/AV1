package com.example.av1;

import org.json.JSONObject;

public class JSONManager {
    public JSONObject createJson(String id, Double time) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("time", time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
