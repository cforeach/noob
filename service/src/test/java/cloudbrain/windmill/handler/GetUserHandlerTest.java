package cloudbrain.windmill.handler;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Test;
import org.junit.runner.RunWith;

import cloudbrain.windmill.utils.AESUtil;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class GetUserHandlerTest extends BaseClassTest {
private int port;
  private final static Long TOKEN_EXPIRED = 1000 * 60 * 60 * 24 * 30L;

  public GetUserHandlerTest() throws IOException {
    super();
  }

  @Test
  public void testHandle(TestContext context) throws Exception {
    ServerSocket server=new ServerSocket(0);
   port = server.getLocalPort();
   server.close();
    JsonObject mysqlConf = new JsonObject();
    mysqlConf.put("host", "localhost").put("port", 3306).put("username", "root")
        .put("password", "123456").put("database", "test")
        .put("connectTimeout", 5).put("charset", "UTF-8");
    SQLClient sqlClient = MySQLClient.createNonShared(vertx, mysqlConf);

    HttpServer httpServer = vertx.createHttpServer();

    router.route("/main/user/*").handler(new GetUserHandler(sqlClient));

    httpServer.requestHandler(router::accept).listen(port);

    httpClient(context,port);

  }

  public static void httpClient(TestContext context,int port) {

    // 异步结果结束标签
    Async async = context.async();

    // 客户端
    WebClient webClient2 = WebClient.create(vertx);

    String unionid = AESUtil.encrypt("eaer");
    // 先插入测试数据
    JsonObject mysqlConf = new JsonObject();
    mysqlConf.put("host", "localhost").put("port", 3306).put("username", "root")
        .put("password", "123456").put("database", "test")
        .put("connectTimeout", 5).put("charset", "UTF-8");
    SQLClient sqlClient = MySQLClient.createNonShared(vertx, mysqlConf);
    sqlClient.updateWithParams(
        "replace into t_user (nickname,headimgurl,unionid) values(?,?,?)",
        new JsonArray().add("test111").add("g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe").add("eaer"), res -> {
        });
    long timeMillis = System.currentTimeMillis() + TOKEN_EXPIRED;
    String timer = String.valueOf(timeMillis);
    //拼装查询用户信息路径
    String path1 = "/main/user/?unionid=";
    //拼装:+ts
    String path2 = unionid + ":" + timer;

    String path = path1 + path2;
    // 发送get请求
    webClient2.get(port, "localhost", path).send(res -> {
      if (res.failed()) {
        // 输出失败的状态码
        res.cause().printStackTrace();
        context.assertFalse(true);
      } else {
        // 输出成功的状态码
        // 删除测试信息
        sqlClient.updateWithParams("delete from t_user where unionid = ?",
            new JsonArray().add("eaer"), res2 -> {
            });
       // context.assertTrue(false);
      }
      async.complete();
    });
  }

}
