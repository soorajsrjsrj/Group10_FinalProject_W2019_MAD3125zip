package com.example.group10_finalproject_w2019_mad3125zip.Utils;

import com.example.group10_finalproject_w2019_mad3125zip.Retrofit.IShopAppApi;
import com.example.group10_finalproject_w2019_mad3125zip.Retrofit.RetrofitClient;

public class Common {
    //in emulator, localhost = 10.0.2.2

    private static final String BASE_URL = "http://192.168.64.2/ShopApp/";

    public static IShopAppApi getAPI(){
        return RetrofitClient.getClient(BASE_URL).create(IShopAppApi.class);
    }

}
