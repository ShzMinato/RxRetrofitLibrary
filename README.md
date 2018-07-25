# RxRetrofitLibrary
### Library介绍
①：基于RxJava2+Retrofit的网络请求和文件下载上传的网络框架<br>
②：支持数据的缓存和log的输出<br>
③：支持联网失败之后的重试<br>
### 使用方法
第一步：创建具有获取网络数据的接口=====Retrofit的接口<br>
第二部：创建XXXAPI继承自BaseApi，复写apply和getObserable方法。<br>
&emsp;&emsp;&emsp;&emsp;apply方法：负责将ResponseBody转为需要的类型<br>
&emsp;&emsp;&emsp;&emsp;getObserable方法：负责获取第一步接口中的定义的相对应的Obserable对象<br>
第三步：通过HttpManager的doHttpDeal方法。
