# 规范

## 底部Tabs对应的字母一览
a -> 我的设备
b -> 视频广场
c -> 扩展功能
d -> 配置
e -> 我的(原来更多功能)
f -> ...
g -> ...
.....其它的以后有待扩展

## 中间扩展功能面板中功能对应的字母一览
a -> 小维社区
b -> 小维知道
c -> 小维商城
d -> 小维工程
e -> 局域网搜索设备
.....其它的以后有待扩展

## 实时视频播放界面中间功能区域对应的字母一览
a -> 云台控制
b -> 远程回放
c -> 设备设置
d -> 语音对讲
e -> 分享链接 (目前猫眼分享和视频分享到广场都是这个标志)
.....其它的以后有待扩展




## 底部Tab[我的]中列表机能实现 
-> 具体的实现方式与原有更多的ListView实现一致
-> 把繁锁的设置属性操作放到了json中实现
-> 对于json文件中的itemFlag的命名,如果Consts.java中存在,则直接使用.没有的情况,创建新的变量
-> 如果需要在代码中动态修改属性,参考JVProfileFunctionActivity.java中的checkDatas()方法

json文件:assets/profile.json
adapter:FragmentAdapter.java
bean:MoreFragmentBean.java

## 实时视频播放界面中的设备设置一级列表(索引界面)
-> 实现及注意事项同上

json文件:assets/device_setting.json
adapter:FragmentAdapter.java
bean:DeviceSettingAdapter.java
