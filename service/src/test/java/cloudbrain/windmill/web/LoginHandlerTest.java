package cloudbrain.windmill.web;


import cloudbrain.windmill.constant.LoginConstant;
import cloudbrain.windmill.utils.AESUtil;

import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
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
  static SQLClient mysqlclient;
  public static int vertx_port;
  private static JsonObject serverConf;

  @Before
  public void init(TestContext context) throws Exception {
    Vertx vert = Vertx.vertx();


    serverConf = ConfReadUtils.getServerConfByJson("conf.json");
    JsonObject mysqlConf =serverConf.getJsonObject("mysql");


    mysqlclient = MySQLClient.createNonShared(vert, mysqlConf);



  }

  @Test
  public void wxLoginFirstGetUrl(TestContext context) {

  }

  @Test
  public void wxLoginCallBack(TestContext context) {
    Async async = context.async();

    JsonObject result=new JsonObject();

    JsonObject userJsonFromWx = new JsonObject();
    userJsonFromWx.put("openid", "1234").put("nickname", "张三").put("sex", "1").put("unionid", "888766").put("country","CN").put("province","浙江").put("city","杭州").put("headimgurl","http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0");

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
        String beforeToken = userJsonFromWx.getString("unionid") + ":" + String.valueOf(System.currentTimeMillis() + LoginConstant.TOKEN_TIMEOUT) + ":" + (int) (Math.random() * 1000);
       System.out.println("加密前的token:"+beforeToken);
        String token = AESUtil.encrypt(beforeToken);
        result.put("success","true").put("token", token).put("message","").put("user",userJsonFromWx);
        System.out.println("结果"+result);//uuLG22vjBwqA33JZgAOtunxvJVhv0fs7Ie5oZ5RLLDc=
        async.complete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }


  @Test
  public void getUserByToken() {

  }
}