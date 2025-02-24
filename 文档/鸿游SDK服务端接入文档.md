## 前言
本文档由鸿游SDK后端接入人员查看。

鸿游不会向游戏方提供域名，游戏方需自备域名，并提供**唯一**支付回调地址（下文详述）给鸿游相关运营人员。

## 协议说明
采用HTTP协议、GET方式请求数据，所有接口发起方为鸿游SDK服务器，接收方为游戏服务器。请求接收方首先需要将接收到的参数进行UrlDecode，才能获得正确的参数值。请求中会拼接sign参数，用于验证请求来源。

## 接口说明
鸿游没有登陆验证接口，游戏方仅需要对接支付回调接口。

### 签名规则
例如，请求参数为price=6，appid=666666，userid=1000。

将所有参数（不包括sign参数）以及对应的参数值按照key1=value1&key2=value2...的形式拼接为字符串，并且按照key的ascii码升序排序:
> appid=666666&price=6&userid=1000

最后拼接支付私钥。如果支付私钥为fjgc6c4g0my90，则：
> appid=666666&price=6&userid=1000&fjgc6c4g0my90

将上述字符串整体进行MD5加密，加密结果为32位小写字符串，作为sign的值。这个例子的完整请求为：

> http://域名/xxxxxx?appid=666666&price=6&userid=1000&sign=55bd8bcda09c5d137960f2108e7c9581

### 支付回调接口
用户支付，并且SDK服务器收到第三方支付平台的支付回调后，将会回调游戏服务器。

**请求参数**

参数名 | 说明 |---
---|---|----
appid | 游戏在鸿游平台的id|
userid | 游戏客户端发起支付时传入的userid值|
orderid | 游戏方订单号|
price | 价格，|单位元，保留两位小数
payresult | 支付结果| 0为成功，-1为失败，-2为用户取消
ext | 透传参数，即游戏客户端支付时传入的ext参数原样返回游戏服务器|此处不能用'&'或者'\|'
sign | 用于游戏方验证请求来源的安全参数|

**返回**

游戏服务器收到请求后，需要着重校验price和sign是否正确。如果校验成功，请返回"success"（小写），返回其他算作失败。

