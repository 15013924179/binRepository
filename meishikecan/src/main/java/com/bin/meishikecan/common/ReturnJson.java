package com.bin.meishikecan.common;

public class ReturnJson {
    public static String error(Constants appConstants)
    {
        return  appConstants.toJson();
    }

    public static String error(Constants appConstants, Object object)
    {
        return  appConstants.toJson(object);
    }

    public static String toErrorJson(Constants appConstants, String message)
    {
        return  appConstants.toErrorJson(message);
    }

    public static String success()
    {
        Constants appConstants = Constants.OK;
        return appConstants.toJson();
    }

    public static String success(Object data)
    {
        Constants appConstants = Constants.OK;
        if(data==null){
            return appConstants.toJson(appConstants);
        }
        return  appConstants.toJson(data);
    }

    public static String successWithMessage(Object data,Constants appConstants)
    {

        if(data==null){
            return appConstants.toJson(appConstants);
        }
        return  appConstants.toJson(data);
    }
}
