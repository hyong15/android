
package com.jovision.bean;

import com.jovision.utils.ConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//登录用户
public class User {
    private final static String TAG = "UserBean";
    private long primaryID = 0;
    private int userType = 0;// 用户类型 1： 邮箱 2：手机
    private String userName = "";
    private String userPwd = "";
    private String userEmail = "";
    private String userPhone = "";

    private int lastLogin = 0;// 最后一次登录，0：不是 1：是
    private int cacheJudgeFlag = 0;// 默认没有缓存

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public void setCacheJudgeFlag(int cacheJudgeFlag) {
        this.cacheJudgeFlag = cacheJudgeFlag;
    }

    public static String getTag() {
        return TAG;
    }

    public int getCacheJudgeFlag() {
        return cacheJudgeFlag;
    }

    public void Phone(int userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public int getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(int lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long getPrimaryID() {
        return primaryID;
    }

    public void setPrimaryID(long primaryID) {
        this.primaryID = primaryID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setJudgeFlag(int flag) {
        this.cacheJudgeFlag = flag;
    }

    public int getJudgeFlag() {
        return cacheJudgeFlag;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("primaryID", primaryID);
            object.put("userType", userType);
            object.put("userName", userName);
            object.put("userPwd", userPwd);
            object.put("userEmail", userEmail);

            object.put("userPhone", userPhone);

            object.put("lastLogin", lastLogin);
            object.put("cacheJudgeFlag", cacheJudgeFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONArray toJsonArray(ArrayList<User> userList) {
        JSONArray userArray = new JSONArray();
        try {
            if (null != userList && 0 != userList.size()) {
                int size = userList.size();
                for (int i = 0; i < size; i++) {
                    userArray.put(i, userList.get(i).toJson());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userArray;
    }

    public String listToString(ArrayList<User> userList) {
        return toJsonArray(userList).toString();
    }

    /**
     * JSON转成User 对象
     * 
     * @param string
     * @return
     */
    public static User fromJson(String string) {
        User user = new User();
        try {
            JSONObject object = new JSONObject(string);
            user.setPrimaryID(object.getLong("primaryID"));
            user.setUserName(ConfigUtil.getString(object, "userName"));
            user.setUserPwd(ConfigUtil.getString(object, "userPwd"));
            user.setUserEmail(ConfigUtil.getString(object, "userEmail"));
            user.setLastLogin(ConfigUtil.getInt(object, "lastLogin"));
            user.setJudgeFlag(object.optInt("cacheJudgeFlag", 0));
            user.setUserType(ConfigUtil.getInt(object, "userType"));
            user.setUserPhone(ConfigUtil.getString(object, "userPhone"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    public static ArrayList<User> fromJsonArray(String string) {
        ArrayList<User> userList = new ArrayList<User>();
        if (null == string || "".equalsIgnoreCase(string)) {
            return userList;
        }
        JSONArray userArray;
        try {
            userArray = new JSONArray(string);
            if (null != userArray && 0 != userArray.length()) {
                int length = userArray.length();
                for (int i = 0; i < length; i++) {
                    User user = fromJson(userArray.get(i).toString());
                    if (null != user) {
                        userList.add(user);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userList;
    }
}
