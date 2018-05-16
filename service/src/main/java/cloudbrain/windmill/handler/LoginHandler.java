package cloudbrain.windmill.handler;

import java.util.UUID;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class LoginHandler implements Handler<RoutingContext> {
	// 微信二维码地址
	private static final String QRCODE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=wxbdc5610cc59c1631&redirect_uri=wxLoginCallback&response_type=code&scope=snsapi_login&state=xxxxx#wechat_redirect";

	/**
	 * 点击微信登录
	 */
	@Override
	public void handle(RoutingContext context) {
		// appid和redirecturi都不知道，暂时不拼装参数。测试返回
		JsonObject jsonObj = new JsonObject();
		jsonObj.put("qrcode_url", QRCODE_URL);
		context.response().putHeader("content-type", "application/json;charset=utf-8").end(jsonObj.encode());

	}

}
