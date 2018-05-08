package cloudbrain.windmill.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/** 
* @author cforeach 
* @version 创建时间：2018年5月4日 下午1:04:25 
* 类说明 
*/
public class DeployClass extends AbstractVerticle{

  public static void main(String[] args) {
    Vertx vertx=Vertx.vertx();
    vertx.deployVerticle(ConnectionTest.class.getName());
  }
  
}
