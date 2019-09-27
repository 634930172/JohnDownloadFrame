package com.john.johndownloadframe.network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/24 16:40
 * <p/>
 * Description:
 */
public class MainConfig implements Parcelable {

    String name;
    String age;

    public MainConfig(){
    }

    protected MainConfig(Parcel in) {
        name = in.readString();
        age = in.readString();
    }

    public static final Creator<MainConfig> CREATOR = new Creator<MainConfig>() {
        @Override
        public MainConfig createFromParcel(Parcel in) {
            return new MainConfig(in);
        }

        @Override
        public MainConfig[] newArray(int size) {
            return new MainConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(age);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
