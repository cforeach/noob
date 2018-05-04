package cloudbrain.windmill.web;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import cloudbrain.windmill.dao.UserDAO;
import cloudbrain.windmill.handler.WXCallbackHandler;
import cloudbrain.windmill.utils.ConfReadUtils;
import cloudbrain.windmill.utils.Md5Util;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;

/** 
* @author cforeach 
* @version 创建时间：2018年5月4日 上午9:36:47 
* 类说明 
*/

@RunWith(VertxUnitRunner.class)
public class MyTest {
  
  @Test
  public void sqlTest() throws Exception{
    
   JsonObject serverConf = ConfReadUtils.getServerConfByJson("conf.json");
    JsonObject mysqlConf = serverConf.getJsonObject("mysql");
    Vertx vertx=Vertx.vertx();
    UserDAO userDao=new UserDAO();
    JsonObject userJsonFromWx = new JsonObject();
    userJsonFromWx.put("openid", "2234").put("nickname", "张三12").put("sex", "1").put("unionid", "988766")
        .put("country", "CN").put("province", "河南").put("city", "平顶山").put("headimgurl",
            "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0");
    String sql = userDao.getReplaceSql(userJsonFromWx);
   SQLClient sqlClient = MySQLClient.createNonShared(vertx, mysqlConf);
   sqlClient.update(sql, res->{
     if(res.succeeded()){
       System.out.println("sql success");
     }else{System.out.println("sql failed");}
   });
  }
  
private void cacu(int i, int j, Object object) {
  // TODO Auto-generated method stub
  /** 
  * @author cforeach 
  * @version 创建时间：2018年5月4日 上午10:49:15 
  * 类说明 
  */ 
}

@Test
public void Server() throws Exception{
      //启动一个服务，监听对应的地址
  Vertx vertx=Vertx.vertx();
  Router router = Router.router(vertx);
  JsonObject serverConf;
  int port= 8097;
  String mycode=Md5Util.MD5("aabbcc");
  System.out.println("mycode==="+mycode);
  serverConf=ConfReadUtils.getServerConfByJson("conf.json");
 // router.get("/test/state=STATE&code="+mycode).handler(new WXCallbackHandler2(vertx, serverConf)::handle);
 // router.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));
  //router.route().handler(BodyHandler.create());
  HttpServer httpServer = vertx.createHttpServer();
// httpServer.requestHandler(router::accept).listen(port);
  httpServer.requestHandler(res->{
    res.response().end("MyTest方法进来了");
  }).listen(port);
  //httpServer.listen();
}

@Test
public void listenTest(){
  Vertx vertx=Vertx.vertx();
vertx.createHttpServer().requestHandler(
    res->{
      res.response().end("listenTest");
    }
    ).listen(8888);
}


}
