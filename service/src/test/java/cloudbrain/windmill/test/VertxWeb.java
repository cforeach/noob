package cloudbrain.windmill.test;

import cloudbrain.windmill.handler.WXCallbackHandler;

import java.io.IOException;
import java.net.ServerSocket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
 
public class VertxWeb extends AbstractVerticle {
public static int port=0;
  public static Vertx vertx;
  
  public String testURL1="/public/login/wxLoginCallBack?code=thisistestcode1234&state=thisisteststate1234";
  
  public static void main(String[] args) throws Exception {
    
    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
   vertx = Vertx.vertx();
  // 部署发布rest服务
   vertx.deployVerticle(new VertxWeb());
  }
  @Override
  public void start() throws Exception {
    HttpServer  httpServer = vertx.createHttpServer();
      final Router router = Router.router(vertx);
     
      
      router.get(testURL1)
      .handler(new WXCallbackHandler( null, null, null)::handle);
      router.post("/searchResult").handler(this::searchResult);
//      httpServer.requestHandler(router::accept).listen(8098);
      httpServer.requestHandler(req -> {
        req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello World alibaba!");
}).listen(port);
      
      
      HttpClient httpClient = vertx.createHttpClient();
   //   httpClient.get(testURL1).headers().set(HttpHeaders.CONTENT_TYPE, "application/json")
      
  }//start()
  
  private void searchResult(RoutingContext context) {
      context.response().end("ok");
      System.out.println(context.getBodyAsString());
  }
}