package com.sonydafa.phoneUsage;
import java.util.List;

public class App implements Comparable {
    private String packageName;
    private String realName;
    private int frontTime;
    //总使用时常
    public App(String packageName, String realName, int frontTime) {
        this.packageName = packageName;
        this.realName = realName;
        this.frontTime = frontTime;
    }

    @Override
    public String toString() {
        return "App{" +
                "packageName='" + packageName + '\'' +
                ", realName='" + realName + '\'' +
                ", frontTime=" + frontTime +
                '}';
    }
    public String getPackageName() {
        return packageName;
    }

    public String getRealName() {
        return realName;
    }

    public int getFrontTime() {
        return frontTime;
    }

    @Override
    public int compareTo(Object o) {
        App other=(App) o;
        return Integer.valueOf(other.frontTime).compareTo(frontTime);
    }
}
