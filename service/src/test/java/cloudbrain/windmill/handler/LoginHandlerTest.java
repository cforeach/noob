package cloudbrain.windmill.handler;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class LoginHandlerTest extends BaseClassTest {

	public LoginHandlerTest() throws IOException {
		super();
	}

	@Test
	public void testHandler(TestContext context) {
		HttpServer httpServer = vertx.createHttpServer();

		router.route("/public/login/wxLogin").handler(new LoginHandler());

		httpServer.requestHandler(router::accept).listen(localPort);

		httpClient(context);
	}

	/**
	 * 模拟发送请求HttpClient
	 */

	private void httpClient(TestContext context) {
		Async async = context.async();

		WebClient webClient = WebClient.create(vertx);
		webClient.get(localPort, "localhost", "/public/login/wxLogin").send(res -> {
			if (res.failed()) {
				// 输出失败的状态码
				System.out.println(res.result().statusCode());
				res.cause().printStackTrace();
				context.assertFalse(true);
			} else {
				// 输出成功的状态码
				System.out.println("二维码为====" + res.result().bodyAsString());

				context.assertTrue(
						res.result().bodyAsJsonObject().containsKey("qrcode_url"));
			}

			// 出口
			async.complete();
		});

	}
}
