package cloudbrain.windmill.web;

import java.io.IOException;

import org.junit.runner.RunWith;

import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/** 
* @author cforeach 
* @version 创建时间：2018年5月7日 下午2:43:10 
* 类说明 这个类用于测试sqlConn连接是否成功，和SQLClient.update方法，需要现在其他启动类部署这个类，不然获取不到conn。
//jsonArray的put方法直接add与？对应的值即可。参考PG(PostgreSQL)在一捅到底架构中的使用 - 圆胖肿的文章 - 知乎
https://zhuanlan.zhihu.com/p/28109253
*/

public class ConnectionTest extends AbstractVerticle{
  private static final long TOKEN_TIMEOUT = 1800;
  static SQLClient mysqlclient;
  public static int vertx_port;
  private static JsonObject serverConf;
  static Vertx vert = Vertx.vertx();
  
  public static void main(String[] args) throws Exception {
    serverConf = ConfReadUtils.getServerConfByJson("conf.json");
    JsonObject mysqlConf = serverConf.getJsonObject("mysql");
    mysqlclient = MySQLClient.createNonShared(vert, mysqlConf);
    String NICKNAME = "fromJAVAVAAVA222";// 获取NICKNAME
    String HEADIMGURL = "headimgurl";// 获取HEADIMGURL
    String unionID = "12345789";// 返回给用户的token
    JsonArray params = new JsonArray(); // jsonArray装jsonObject
    params.add(NICKNAME).add(HEADIMGURL).add(unionID);//jsonArray的put方法直接add与？对应的值即可。
https://zhuanlan.zhihu.com/p/28109253
    String sql = "replace into t_user (nickname,headimgurl,unionid) values (?,?,?)";
    mysqlclient.updateWithParams(sql, params, res -> {
      if (res.succeeded()) {
        System.out.println("success");
      } else {
        System.out.println(res.cause());
      }
    });
  }

}
