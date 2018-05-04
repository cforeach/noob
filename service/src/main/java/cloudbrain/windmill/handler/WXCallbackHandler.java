package cloudbrain.windmill.handler;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import cloudbrain.windmill.Server;
import cloudbrain.windmill.dao.UserDAO;
import cloudbrain.windmill.utils.AESUtil;
import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class WXCallbackHandler implements Handler<RoutingContext> {

  private final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
  private JsonObject wxJsonConf;
  private WebClient webClient;
  private SQLClient mysqlClient;
  private String appid;
  private String secret;

  public WXCallbackHandler(JsonObject wxJsonConf, WebClient webClient, SQLClient mysqlClient) {
    this.wxJsonConf = wxJsonConf;
    this.webClient = webClient;
    this.mysqlClient = mysqlClient;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    // 操作数据库对象 userDao
    UserDAO userDao = new UserDAO();
    HttpServerRequest request = routingContext.request();// 第三方的请求作用域
    String code = request.getParam("code");// 获取code

    appid = wxJsonConf.getString("appid");
    secret = wxJsonConf.getString("secret");

    // 发送给微信获取acctoken的url
    String Url_To_Wx = wxJsonConf.getString("Get_AccessToken_Url") + appid + "&secret=" + secret
        + "&grant_type=authorization_code&code=" + code;

    webClient.get(Url_To_Wx).putHeader("content-type", "application/json;charset=utf-8").send(msg -> {
      if (msg.failed()) {
        logger.error(msg.cause());// 网络响应失败
        routingContext.response().setStatusCode(401).end("401");// 响应状态码
        return;
      }
     //判断返回值是否包括指定的Key
      if (msg.result().bodyAsJsonObject().containsKey("openid")) {
        HttpResponse<Buffer> result = msg.result();
        JsonObject jsonObject = result.bodyAsJsonObject();// 发给微信code后微信返回的jsonObject
        String old_access_token = jsonObject.getString("access_token");// 从response获取access_token
        String new_access_token = AESUtil.encrypt(old_access_token);// 加密后的token
        String openid = jsonObject.getString("openid");// 从response获取openid

        // 获取用户信息的微信请求地址(未处理token和oppenid)
        String Get_UserInfo_Url = wxJsonConf.getString("Get_UserInfo_Url");
        // 拼接access_token和oppenid到url
        String new_getUserInfo_Url = Get_UserInfo_Url + "access_token=" + old_access_token + "&openid=" + openid;

        webClient.get(new_getUserInfo_Url).putHeader("content-type", "application/json;charset=utf-8").send(msg2 -> {
          if (msg2.succeeded()) {
            JsonObject userInfoJsonObj = result.bodyAsJsonObject();// 给微信code返回的包含用户信息的jsonObj
            String NICKNAME = userInfoJsonObj.getString("nickname");// 获取NICKNAME
            String HEADIMGURL = userInfoJsonObj.getString("headimgurl");// 获取HEADIMGURL
            String unionID = userInfoJsonObj.getString("unionid");// 返回给用户的token
            // 先在数据库保存用户数据
            userDao.getReplaceSQL(routingContext, userInfoJsonObj);
            String sqlReturn = userDao.getReplaceSQL(routingContext, userInfoJsonObj);
            if ("".equals(sqlReturn)) {
              // sql执行失败
              logger.error("sql执行失败");
            }

            JsonObject responseJson = new JsonObject();// 整个返回的json
            responseJson.put("token", new_access_token);// 加密后的token
            JsonObject userResponseJson = new JsonObject();// 封装用户信息的json
            userResponseJson.put("unionid", unionID);
            userResponseJson.put("nickname", NICKNAME);
            userResponseJson.put("headimgurl", HEADIMGURL);
            responseJson.put("user", userResponseJson);
            routingContext.response().putHeader("content-type", "application/json").end(responseJson.encode());

          } else {
            logger.error(msg.cause());
            routingContext.response().setStatusCode(401).end("401");// 响应状态码
          }
        });
      } 
      else{
        logger.error(msg.cause());// 网络正常，但是返回信息没有包含用户信息
        routingContext.response().setStatusCode(401).end("401");// 响应给手机用户状态码
      }
      
    });
  }
}
