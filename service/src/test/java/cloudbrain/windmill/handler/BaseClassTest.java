package cloudbrain.windmill.handler;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

public class BaseClassTest {
	protected static JsonObject serverConf;
	protected static JsonObject wxJsonConf;
	protected static WebClient webClient;
	protected static Router router;
	protected static Vertx vertx;
	protected static ServerSocket serverSocket;
	protected static ServerSocket serverSocket2;
	protected static int localPort;
	protected static int localPort2;

	public BaseClassTest() throws IOException {
		vertx = Vertx.vertx();
		router = Router.router(vertx);
		JsonObject serverConf = ConfReadUtils.getServerConfByJson("conf.json");
		wxJsonConf = serverConf.getJsonObject("wx");
		webClient = WebClient.create(vertx);// 构造用的参数

		serverSocket = new ServerSocket(0);
		serverSocket2 = new ServerSocket(0);
		localPort = serverSocket.getLocalPort();
		localPort2 = serverSocket2.getLocalPort();
		System.out.println("baseclas中port2=="+localPort2);
		serverSocket.close();
		serverSocket2.close();
	}

	public JsonObject getSQLConf() {
		JsonObject mysqlConf = new JsonObject();
		mysqlConf.put("host", "localhost").put("port", 3306).put("username", "root")
				.put("password", "root").put("database", "circle_test")
				.put("connectTimeout", 5).put("charset", "UTF-8");
		return mysqlConf;

	}

}
