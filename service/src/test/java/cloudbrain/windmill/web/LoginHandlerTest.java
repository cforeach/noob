package cloudbrain.windmill.web;


import cloudbrain.windmill.utils.Md5Util;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.RedisClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(VertxUnitRunner.class)
public class LoginHandlerTest extends LoginHandler {
  private static final long TOKEN_TIMEOUT = 1800;
  static RedisClient redisClient;
  static SQLClient mysqlclient;
  volatile static boolean flag = true;

  @Before
  public void init(TestContext context) throws Exception {
    Vertx vert = Vertx.vertx();
    redisClient = RedisClient.create(vert);
    JsonObject mysqlConf = new JsonObject();
    mysqlConf.put("host", "127.0.0.1").put("port", 3306).put("username", "root").put("password", "1").put("database", "windmill").put("charset", "UTF-8").put("maxPoolSize", 500);
    mysqlclient = MySQLClient.createNonShared(vert, mysqlConf);

  }

  @Test
  public void wxLoginFirstGetUrl(TestContext context) {

  }

  @Test
  public void wxLoginCallBack(TestContext context) {

    JsonObject userJsonFromWx = new JsonObject();
    userJsonFromWx.put("openid", "1234").put("nickname", "nickname").put("sex", "1").put("unionid", "555");

    String unionid = userJsonFromWx.getString("unionid");
    System.out.println(unionid);

    //查询数据库是否有此用户
    mysqlclient.query("SELECT * FROM T_USER T WHERE T.`unionid`='" + unionid + "' ", mySqlRes -> {
      List<JsonObject> userFromDB = mySqlRes.result().getRows();
      System.out.println(userFromDB);
      if (userFromDB.size() == 0) {//新增
        System.out.println("insert");
        String insertSql = userDAO.getInsertSql(userJsonFromWx);
        System.out.println(insertSql);
        mysqlclient.update(insertSql, insertRes -> {
        });
      } else { //更新
        System.out.println("update");
        String updateSql = userDAO.getUpdateSql(userJsonFromWx);
        System.out.println(updateSql);
        mysqlclient.update(updateSql, insertRes -> {
        });

      }

      //生成token 并保存至redis中
      try {
        String token = Md5Util.MD5(userJsonFromWx.getString("unionid") + String.valueOf(System.currentTimeMillis()));
        redisClient.setex(token, TOKEN_TIMEOUT, userJsonFromWx.encodePrettily(), redisRes -> {
          userJsonFromWx.put("token", token);
          flag = false;
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    while (flag) {
    }
  }


}