package cloudbrain.windmill.web;

import cloudbrain.windmill.Server;
import cloudbrain.windmill.constant.LoginConstant;
import cloudbrain.windmill.dao.UserDAO;
import cloudbrain.windmill.utils.AESUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;

import java.util.List;

public class LoginHandler {
  public UserDAO userDAO=new UserDAO();
  //微信二维码地址
  private static final String QRCODE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=wxbdc5610cc59c1631&redirect_uri=https%3A%2F%2Fpassport." +
          "yhd.com%2Fwechat%2Fcallback.do&response_type=code&scope=snsapi_login&state=3d6be0a4035d839573b04816624a415e#wechat_redirect";

  private static final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=?1&grant_type=authorization_code";
  //?access_token=ACCESS_TOKEN&openid=OPENID 参数
  private static final String GET_USER_BY_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/userinfo";



  /**
   * 点击微信登陆按钮
   *
   * @param
   * @return 二维码url
   * @author jiwei
   * @time 2018/4/23   10:28
   */
  public void wxLoginFirstGetUrl(RoutingContext context) {
    JsonObject obj = new JsonObject();
    obj.put("qr_code", QRCODE_URL);
    toResponse(context, obj);
  }


  /**
   * 扫二维码回调方法(微信交互部分待测试)
   *
   * @param
   * @return
   * @author jiwei
   * @time 2018/4/23   10:31
   */
  public void wxLoginCallBack(RoutingContext context) {
    //拿到微信返回的参数code
    JsonObject result = new JsonObject();

    HttpServerRequest request = context.request();
    String code = request.getParam("code");
    String state = request.getParam("state");

    //获取url
    String url = getAccessTokenUrl(code);

    //给微信发送请求获取accessToken
    Future<HttpResponse<Buffer>> getAccessTokenFuture = Future.future();
    Future<HttpResponse<Buffer>> getUserByWxFuture = Future.future();

    //openid 换取微信User信息
    getUserByWxFuture.setHandler(res ->
            getUserByWx(context, result, res)
    );

    getAccessTokenFuture.setHandler(res ->
            getAccessToken(context, result, getUserByWxFuture, res)
    );

    HttpRequest<Buffer> getRequest = Server.webClient.get(String.valueOf(Server.vertx_port), url);
    getRequest.putHeader("content-type", "application/json;charset=utf-8").send(getAccessTokenFuture.completer());
  }


  /**根据tonken获取User信息
   * @author jiwei
   * @time 2018/4/26   11:39
   * @param
   * @return user 信息
   */
  public void getUserByToken(RoutingContext context){
    JsonObject response=new JsonObject().put("success", "false").put("message", "token已过期请重新登录");
    try {
      //初始化返回值
      JsonObject tokenJson = context.getBodyAsJson();
      //解密token
      String token = AESUtil.decrypt(tokenJson.getString("token"));
      //拆分token
      String[] tokenArray = token.split(":");

      if (System.currentTimeMillis() <= Long.valueOf(tokenArray[1])) {//查看是否超时
        Server.mysqlclient.query("SELECT T.`HEADIMGURL` headimgurl,T.`SEX` sex,T.`UNIONID` unionid,T.`PROVINCE` provice,T.`COUNTRY` country,T.`CITY` city,T.`NICKNAME` nickname FROM `t_user` t WHERE t.`unionid`='" + tokenArray[0] + "'", res -> {
          if (res.result().getRows().size() > 0) {
            response.put("success","true").put("message","").put("user", res.result().getRows().get(0));
          } else {
            response.put("message", "token值有误");
          }
          toResponse(context, response);
        });
      }else {
        toResponse(context, response);
      }

    }catch (Exception e){
      response.put("success", "false").put("message", "参数不正确或token无效");
      toResponse(context, response);
    }
  }


  private void getAccessToken(RoutingContext context, JsonObject result, Future<HttpResponse<Buffer>> getUserByWxFuture, AsyncResult<HttpResponse<Buffer>> res) {
    JsonObject respnseBody = res.result().bodyAsJsonObject();
    if (respnseBody.containsKey("openid")) {
      String openid = respnseBody.getString("openid");
      String accessToken = respnseBody.getString("access_token");

      //获取微信user信息
      HttpRequest<Buffer> getRequest = Server.webClient.get(String.valueOf(Server.vertx_port), GET_USER_BY_ACCESS_TOKEN_URL);
      getRequest.putHeader("content-type", "application/json").putHeader("openid", openid).putHeader("access_token", accessToken).send(getUserByWxFuture.completer());
    } else { //扫码失败
      result.put("success", false).put("message", "用户扫码失败");
      toResponse(context, result);
      return;
    }
  }

  private void getUserByWx(RoutingContext context, JsonObject result, AsyncResult<HttpResponse<Buffer>> res) {
    JsonObject userJsonFromWx = res.result().bodyAsJsonObject();
    String unionid = userJsonFromWx.getString("unionid");

    //查询数据库是否有此用户
    Server.mysqlclient.query("SELECT * FROM T_USER T WHERE T.`unionid`='" + unionid + "' ", mySqlRes -> {
      List<JsonObject> userFromDB = mySqlRes.result().getRows();
      if (userFromDB.size() == 0) {//新增
        String insertSql = userDAO.getInsertSql(userJsonFromWx);
        Server.mysqlclient.update(insertSql, insertRes -> {
        });
      } else { //更新
        String updateSql = userDAO.getUpdateSql(userJsonFromWx);
        Server.mysqlclient.update(updateSql, insertRes -> {
        });
      }

      //生成token 并保存至redis中
      try {
        //加密
        String beforeToken = userJsonFromWx.getString("unionid") + ":" + String.valueOf(System.currentTimeMillis() + LoginConstant.TOKEN_TIMEOUT) + ":" + (int) (Math.random() * 1000);
        String token = AESUtil.encrypt(beforeToken);

        //发送token
        result.put("success", true).put("token", token).put("user", userJsonFromWx);
        toResponse(context, result);


      } catch (Exception e) {
        e.printStackTrace();
        result.put("success", false).put("message", e.getMessage());
        toResponse(context, result);
      }
    });
  }


  private String getAccessTokenUrl(String code) {
    return this.GET_ACCESS_TOKEN_URL.replace("?1", code);
  }

  /**
   * 返回一个json格式的响应
   *
   * @param
   * @return
   * @author jiwei
   * @time 2018/4/23   10:28
   */
  private void toResponse(RoutingContext context, JsonObject result) {
    context.response().putHeader("content-type", "application/json")
            .end(result.encodePrettily());
  }
}
