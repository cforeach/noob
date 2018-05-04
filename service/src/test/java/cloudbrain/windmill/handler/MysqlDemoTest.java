package cloudbrain.windmill.handler;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * test /syncdata/match_intern
 */
 
 
 
 

@RunWith(VertxUnitRunner.class)
public class MysqlDemoTest {
  private static Vertx vertx;
  private static SQLClient mysqlclient;


  @BeforeClass
  public static void beforeClass() {
    vertx = Vertx.vertx();
    //mysql client
    JsonObject conf = new JsonObject().
      put("host", "localhost").
      put("port", 3306).
      put("username", "root").
      put("password", "root").
      put("database", "circle_test").
      put("connectTimeout", 5).
      put("charset", "UTF-8");
    mysqlclient = MySQLClient.createNonShared(vertx, conf);
  }


  @AfterClass
  public static void afterClass() {
    vertx.close();
  }


  @Test
  public void testDemo(TestContext context) throws Exception {
    Async async = context.async();
    //get connection
    Future.<SQLConnection>future(fut -> mysqlclient.getConnection(fut)).
      compose(conn -> {
        return Future.<ResultSet>future(fut ->
          conn.query("select 'abc' c1", fut));
        }).
      setHandler(ar -> {
          if(ar.failed()) {
            ar.cause().printStackTrace();
            context.assertFalse(1==1);
          } else {
            context.assertTrue("abc".
              equals(ar.result().getRows().get(0).getString("c1")));
          }
          async.complete();
        });
  }
}
