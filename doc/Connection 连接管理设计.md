--------------------------String addr------------------------------
String addr = "127.0.0.1:8888?_CONNECTIONNUM=10&_CONNECTIONWARMUP=true&_CONNECTTIMEOUT=3000";
String res = (String) client.invokeSync(addr, req, 3000);

--------------------------Url url------------------------------
Url url = new Url(ip, port);
url.setProtocol(RpcProtocolV2.PROTOCOL_VERSION_2);
url.setVersion(RpcProtocolV2.PROTOCOL_VERSION_2);
url.setConnNum(10); // 期望连接数
url.setConnWarmup(true); // 是否预热
url.setConnectTimeout(3000); // 连接超时，3000 ms
String res = (String) client.invokeSync(url, req, 3000);