package me.georgeyang.test.entity;

import android.content.Context;

import me.georgeyang.test.R;

/**
 * Created by george.yang on 2015/12/3.
 */
public class PlayGroundPoint {
    public int itemid;
    public int x;
    public int y;
    public int category;
    public String name;
    public String icon;
    public String image;
    public String description;
    public int getDrawableRes (Context context) {
        int i =  context.getResources().getIdentifier(icon.replace(".png",""), "drawable", context.getPackageName());
        if (i>0) {
            return i;
        }
        return R.drawable.icon_aid;
    }
}
