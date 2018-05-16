package cloudbrain.windmill.handler;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

@RunWith(VertxUnitRunner.class)
public class WXCallBackHanderTest {
  private WebClient webClient2;
  private ServerSocket serverSocket1;// 本地服务器端口
  private ServerSocket serverSocket2;// 微信服务器端口
  private int localPort1;
  private int localPort2;

  @Test
  public void testHandler() throws Exception {
    serverSocket1 = new ServerSocket(0);
    serverSocket2 = new ServerSocket(0);
    localPort1 = serverSocket1.getLocalPort();
    localPort2 = serverSocket2.getLocalPort();
    System.out.println("localPort1" + localPort1);
    System.out.println("localPort2" + localPort2);
    serverSocket1.close();
    serverSocket2.close();
    
    //启动微信服务器
    createWXServer(localPort2);
    //启动wind服务器
    createWindSerser(localPort1,localPort2);
  }
  
  
  //微信服务端
  public void createWXServer(int localPort2) throws Exception {
    // 测试语句
    System.out.println("wxServer方法进来了");
    // 创建本地端口号
    

    // vertx
    Vertx vertx = Vertx.vertx();
    // 测试下2个方法的vertx是否是同一个。。应该不是
    System.out.println("wx服务器的vertx为" + vertx);

    Router router = Router.router(vertx);
    router.route("/test/*").handler(res -> {
      System.out.println("requestPath" + res.request().path());
      System.out.println("requestUri" + res.request().uri());
      System.out.println("wxServer路由进来了");
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("openid", "THIS_IS_TEST_OPENID")
          .put("access_token", "THIS_IS_TEST_ACCESS_TOKEN")
          .put("userinfo_url", "THIS_IS_TEST_USERINFO_URL");
      // 返回用于测试access_token的json
      // res.response().end("123");
      res.response().end(jsonObject.toString());
    });
    HttpServer httpServer = vertx.createHttpServer();
    httpServer.requestHandler(router::accept).listen(localPort2);
    System.out.println("wxServer started");

    }
  
    // 自己的服务器
  public void createWindSerser(int localPort1, int localPort2) {
    // 测试语句System.out.println("testHanle come in");
    Vertx vertx2 = Vertx.vertx();
    System.out.println("自己服务器的vertx为" + vertx2);
                        
    // wxServer(context);
    Router router2 = Router.router(vertx2);

    // 创建sqlClient
    // 配置
    JsonObject mysqlConf = new JsonObject();
    mysqlConf.put("host", "localhost").put("port", 3306).put("username", "root")
        .put("password", "root").put("database", "circle_test")
        .put("connectTimeout", 5).put("charset", "UTF-8");
    // 传参
    SQLClient mysqlClient = MySQLClient.createNonShared(vertx2, mysqlConf);

    HttpServer httpServer2 = vertx2.createHttpServer();
    // 配置自己的jsonObject,拼接url
    JsonObject wxJsonConf2 = new JsonObject().put("appid", "13dwK0")
        .put("secret", "w247BO").put("accesstoken_url",
            "localhost:" + localPort2 + "/test/access_token?appid=");// 配置请求wx
                                                                     // url
    JsonObject jj = new JsonObject();
    jj.put("defaultHost", "localhost").put("defaultPort", localPort1);
    WebClientOptions options = new WebClientOptions(jj);

    System.out.println("server:"+options.getDefaultHost());
    System.out.println("server:"+options.getDefaultPort());
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
    System.out.println("client come in");
    WebClient webClient = WebClient.create(vertx3);
    webClient.get(localPort1, "localhost",
        "/public/login/wxLoginCallBack?appid=APPID&secret=SECRET&code=jagdpather&grant_type=authorization_code")
        .send(res -> {
          if (res.failed()) {
            // 输出失败的状态码
            System.out.println("输出失败的状态码" + res.result().statusCode());
            res.cause().printStackTrace();
            context.assertFalse(true);
          } else {
            // 输出成功的状态码
            System.out.println("输出成功的状态码" + res.result().statusCode());
            System.out.println(res.result().body().toString());
            context.assertTrue(false);
          }
          async.complete();
        });
  }

}
