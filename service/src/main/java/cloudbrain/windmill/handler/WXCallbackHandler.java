package cloudbrain.windmill.handler;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import cloudbrain.windmill.Server;
import cloudbrain.windmill.utils.AESUtil;
import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WXCallbackHandler implements Handler<RoutingContext> {

	private final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
	private JsonObject wxJsonConf;
	private WebClient webClient;
	private SQLClient mysqlClient;
	private final Long TOKEN_EXPIRED=1000*60*60*24*30L;
  private int localPort2;
  private WebClient webClient2;

	public WXCallbackHandler(JsonObject wxJsonConf, WebClient webClient, SQLClient mysqlClient) {
		this.wxJsonConf = wxJsonConf;
		this.webClient = webClient;
		this.mysqlClient = mysqlClient;
	}

	@Override
	public void handle(RoutingContext routingContext) {
		// 获取code
		String code = routingContext.request().getParam("code");

		String appid = wxJsonConf.getString("appid");
		String secret = wxJsonConf.getString("secret");

		// 发送给微信获取acctoken的url
		String Url_To_Wx = wxJsonConf.getString("accesstoken_url") + appid + "&secret=" + secret
				+ "&grant_type=authorization_code&code=" + code;

		System.out.println(Url_To_Wx);
		
		webClient.get(Url_To_Wx).putHeader("content-type", "application/json;charset=utf-8").send(msg -> {
System.out.println("判断消息是否发送成功"+msg.succeeded());
			// 请求发送失败
			if (msg.failed()) {
				logger.error("token请求发送失败", msg.cause());
				// 响应状态码
				routingContext.response().setStatusCode(401).end("token请求发送失败");
				return;
			}
			// 判断返回值是否包括指定的Key
			System.out.println("msg.result()statusCode=="+msg.result().statusCode());

			System.out.println("token发送成功，返回数据开始判断");
			if (msg.result().bodyAsJsonObject().containsKey("openid")) {
				// 发给微信code后微信返回的jsonObject
				JsonObject jsonObject = msg.result().bodyAsJsonObject();
				// 从response获取openid
				String openid = jsonObject.getString("openid");
				// 从response获取access_token
				String token = jsonObject.getString("access_token");
				
				System.out.println("微信返回的access_token=="+token);
				
				// 获取用户信息的微信请求地址(未处理token和oppenid)
				String Get_UserInfo_Url = wxJsonConf.getString("userinfo_url");
				// 拼接access_token和oppenid到url
				String new_getUserInfo_Url = Get_UserInfo_Url + "access_token=" + token + "&openid=" + openid;

				/**
				 * 根据accesstoken，获取userInfo
				 */
				webClient.get(new_getUserInfo_Url).putHeader("content-type", "application/json;charset=utf-8").send(msg2 -> {
					if (msg2.succeeded()) {
						JsonObject userInfoJsonObj = msg2.result().bodyAsJsonObject();
						// 获取NICKNAME
						String NICKNAME = userInfoJsonObj.getString("nickname");
						// 获取HEADIMGURL
						String HEADIMGURL = userInfoJsonObj.getString("headimgurl");
						// 返回给用户的token
						String unionID = userInfoJsonObj.getString("unionid");
						// 获取refresh_token
						String refresh_token = userInfoJsonObj.getString("refresh_token");
						// 先在数据库保存用户数据
						JsonArray params = new JsonArray();
						params.add(NICKNAME).add(HEADIMGURL).add(unionID);
						mysqlClient.updateWithParams("replace into t_user (nickname,headimgurl,unionid) values (?,?,?)", params,
								res -> {
									if (res.failed()) {
										// 打印错误信息
										logger.error("微信返回用户信息数据库操作失败", res.cause());
										routingContext.response().setStatusCode(401).end("微信返回用户信息数据库操作失败");
									}
								});
						// token添加时间戳，用于判断是否超时了；默认30天
						Long time = System.currentTimeMillis() +TOKEN_EXPIRED;
						// 用:分割token和timer
						String timer = ":" + String.valueOf(time);

						// 整个返回的json
						JsonObject responseJson = new JsonObject();
						// 加密后的token
						String encrypt_token = AESUtil.encrypt(token);
						responseJson.put("token", encrypt_token);
						// 封装用户信息的json
						JsonObject userResponseJson = new JsonObject();
						userResponseJson.put("unionid", AESUtil.encrypt(encrypt_token) + timer);
						userResponseJson.put("nickname", NICKNAME);
						userResponseJson.put("headimgurl", HEADIMGURL);
						responseJson.put("user", userResponseJson);
						routingContext.response().putHeader("content-type", "application/json").end(responseJson.encode());

					} else {
						logger.error("请求用户信息发送失败", msg.cause());
						// 响应状态码
						routingContext.response().setStatusCode(401).end("请求用户信息发送失败");
					}
				});
			} else {
				// 网络正常，但是返回信息没有包含用户信息
				logger.error("微信响应内容异常",msg.result().bodyAsString());
				// 响应给手机用户状态码
				routingContext.response().setStatusCode(401).end("微信返回信息没有包含用户信息");
			}
		});
	}
}
