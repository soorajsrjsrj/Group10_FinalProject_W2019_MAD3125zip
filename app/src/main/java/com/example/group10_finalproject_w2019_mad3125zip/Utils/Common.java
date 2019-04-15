package com.example.group10_finalproject_w2019_mad3125zip.Utils;

import com.example.group10_finalproject_w2019_mad3125zip.Model.User;
import com.example.group10_finalproject_w2019_mad3125zip.Retrofit.IShopAppApi;
import com.example.group10_finalproject_w2019_mad3125zip.Retrofit.RetrofitClient;

public class Common {
    //in emulator, localhost = 10.0.2.2

    private static final String BASE_URL = "http://10.0.2.2:8080/ShopApp/";


    public static User currentUser = null;

    public static IShopAppApi getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(IShopAppApi.class);
    }

}
