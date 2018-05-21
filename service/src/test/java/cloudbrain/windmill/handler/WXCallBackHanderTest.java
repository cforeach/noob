package cloudbrain.windmill.handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.net.SyslogAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

@RunWith(VertxUnitRunner.class)
public class WXCallBackHanderTest {
  private WebClient webClient2;
  private ServerSocket serverSocket1;// 本地服务器端口
  private ServerSocket serverSocket2;// 微信服务器端口
  private int localPort1;
  private int localPort2;
  private Vertx vertx;

  @Test
  public void testHandler(TestContext context) throws Exception {
    serverSocket1 = new ServerSocket(0);
    serverSocket2 = new ServerSocket(0);
    localPort1 = serverSocket1.getLocalPort();
    localPort2 = serverSocket2.getLocalPort();
    serverSocket1.close();
    serverSocket2.close();
    vertx=Vertx.vertx();
    //启动微信access_token服务器
    createWXServer(localPort2,context);
    
    //启动wind服务器
    createWindSerser(localPort1,localPort2);
    //appClient
    createAppClient(localPort1, context);
  }
  //微信服务端
  public void createWXServer(int localPort2, TestContext context) throws Exception {
    // 测试语句
    // vertx
    Vertx vertx = Vertx.vertx();
    // 测试下2个方法的vertx是否是同一个。。应该不是
    Router router = Router.router(vertx);
    //accesstoken的路由注册
    router.route("/test/access_token*").handler(res -> {
      if(res.failed()) {
      }
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("openid", "THIS_IS_TEST_OPENID")
          .put("access_token", "THIS_IS_TEST_ACCESS_TOKEN")
          .put("userinfo_url", "THIS_IS_TEST_USERINFO_URL");
      res.response().setStatusCode(200).end(jsonObject.encode());
    });
    router.route("/test/userinfo*").handler(res->{
      if(res.failed()) {
      }
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("openid", "OPENID")
          .put("nickname", "NICKNAME")
          .put("headimgurl", "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0")
          .put("unionid"," o6_bmasdasdsad6_2sgVt7hMZOPfL");
      res.response().end(jsonObject.encode());
    });
    
    HttpServer httpServer = vertx.createHttpServer();
    httpServer.requestHandler(router::accept).listen(localPort2);
    
    }
  
    // 自己的服务器
  public void createWindSerser(int localPort1, int localPort2) {
    Vertx vertx2 = Vertx.vertx();
                        
    // wxServer(context);
    Router router2 = Router.router(vertx2);

    // 创建sqlClient
    // 配置
    JsonObject mysqlConf = new JsonObject();
    mysqlConf.put("host", "localhost").put("port", 3306).put("username", "root")
        .put("password", "123456").put("database", "test")
        .put("connectTimeout", 5).put("charset", "utf-8");
    // 传参
    SQLClient mysqlClient = MySQLClient.createNonShared(vertx2, mysqlConf);

    HttpServer httpServer2 = vertx2.createHttpServer();
    // 配置自己的jsonObject,拼接url
    JsonObject wxJsonConf2 = new JsonObject()
        .put("appid", "13dwK0")
        .put("secret", "w247BO")
        .put("host","localhost")
        .put("port", localPort2)
        .put("accesstoken_url","/test/access_token?appid=")// 配置请求wx
        .put("userinfo_url","/test/userinfo?access_token=");// 配置请求wx
                                                                     // url
    JsonObject jj = new JsonObject();
    jj.put("defaultHost", "localhost").put("defaultPort", localPort2);
    WebClientOptions options = new WebClientOptions(jj);

    // 创建主机为localhost端口号和微信服务器一直的webClient
    webClient2 = WebClient.create(vertx2, options);

    // 处理code
    router2.route("/public/login/wxLoginCallBack/*")
        .handler(new WXCallbackHandler(wxJsonConf2, webClient2, mysqlClient));

    httpServer2.requestHandler(router2::accept).listen(localPort1);


  }

  /**
   * 接口2的客户端
   * 
   * @param localPort1
   * 
   * @param context
   */
  public void createAppClient(int localPort1, TestContext context) {
    Vertx vertx3 = Vertx.vertx();
    Async async = context.async();
    WebClient webClient = WebClient.create(vertx3);
    webClient.get(localPort1, "localhost",
        "/public/login/wxLoginCallBack?appid=APPID&secret=SECRET&code=jagdpather&grant_type=authorization_code")
        .send(res -> {
          if (res.failed()) {
            res.cause().printStackTrace();
          //  context.assertFalse(true);
          } else {
            // 输出成功的状态码
          }
          async.complete();
        });
  }

}
