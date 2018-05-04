package cloudbrain.windmill.web;

import java.io.IOException;

import cloudbrain.windmill.handler.WXCallbackHandler;
import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

/** 
* @author cforeach 
* @version 创建时间：2018年5月4日 下午1:01:42 
* 类说明 
*/
public class VisualServer extends AbstractVerticle{


  public void start() throws Exception{
    JsonObject wxJsonConf=ConfReadUtils.getServerConfByJson("conf.json").getJsonObject("wx");
    JsonObject mysqlJsonConf=ConfReadUtils.getServerConfByJson("conf.json").getJsonObject("mysql");
    Router router = Router.router(vertx);
    String path=" ";
    WebClient webClient=WebClient.create(vertx);
    SQLClient mysqlClient=MySQLClient.createNonShared(vertx, mysqlJsonConf);
    router.route(path).handler(new WXCallbackHandler(wxJsonConf, webClient, mysqlClient, path, path));
    vertx.createHttpServer().requestHandler(
        res->{
          res.response().setStatusCode(401).end("this is class VisualServer");
        }
        ).listen(8901);
  }
  
}
