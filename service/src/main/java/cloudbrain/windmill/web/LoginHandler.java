package cloudbrain.windmill.web;

import cloudbrain.windmill.StartServer;
import cloudbrain.windmill.dao.UserDAO;
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
    obj.put("QRCODE_URL", QRCODE_URL);
    toResponse(context, obj);
  }


  /**
   * 扫二维码回调方法(未完成)
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
    Future<Void> saveRedisFuture=Future.future();

    //redis处理的回调函数
    saveRedisFuture.setHandler(res->{

    });

    //openid 换取微信User信息
    getUserByWxFuture.setHandler(res -> {
      JsonObject userJsonFromWx = res.result().bodyAsJsonObject();
      String unionid = userJsonFromWx.getString("unionid");
      toResponse(context, result);

      //查询数据库是否有此用户
      StartServer.mysqlclient.query("SELECT * FROM T_USER T WHERE T.`unionid`='" + unionid + "' ", mySqlRes -> {
        List<JsonObject> userFromDB = mySqlRes.result().getRows();
        if (userFromDB.size() == 0) {//新增
          String insertSql=userDAO.getInsertSql(userFromDB.get(0));
          StartServer.mysqlclient.update(insertSql,insertRes->{
          });
        } else { //更新

        }
        //生成token 并生成信息
      });

    });

    getAccessTokenFuture.setHandler(res -> {
      JsonObject respnseBody = res.result().bodyAsJsonObject();
      if (respnseBody.containsKey("openid")) {
        result.put("success", true).put("openid", respnseBody.getValue("openid"));
        String openid = respnseBody.getString("openid");
        String accessToken = respnseBody.getString("access_token");

        //获取微信user信息
        HttpRequest<Buffer> getRequest = StartServer.webClient.get(String.valueOf(StartServer.vertx_port), GET_USER_BY_ACCESS_TOKEN_URL);
        getRequest.putHeader("content-type", "application/json").putHeader("openid", openid).putHeader("access_token", accessToken).send(getUserByWxFuture.completer());
      } else {
        result.put("success", false);
      }

    });
    HttpRequest<Buffer> getRequest = StartServer.webClient.get(String.valueOf(StartServer.vertx_port), url);
    getRequest.putHeader("content-type", "application/json").send(getAccessTokenFuture.completer());
  }

  private String getAccessTokenUrl(String code) {
    return this.GET_ACCESS_TOKEN_URL.replace("?1", code);
  }

  /**
   * 返回一个json格式的相应
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
