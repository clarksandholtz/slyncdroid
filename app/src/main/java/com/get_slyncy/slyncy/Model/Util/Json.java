package com.get_slyncy.slyncy.Model.Util;

import com.google.gson.Gson;

public class Json
{
    public static String toJson(Object o)
    {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> classOfT)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, classOfT);
    }
}
