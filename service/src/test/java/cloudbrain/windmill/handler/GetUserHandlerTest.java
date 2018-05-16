package cloudbrain.windmill.handler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import cloudbrain.windmill.utils.AESUtil;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class GetUserHandlerTest extends BaseClassTest {

	private final static Long TOKEN_EXPIRED = 1000 * 60 * 60 * 24 * 30L;

	public GetUserHandlerTest() throws IOException {
		super();
	}

	@Test
	public void testHandle(TestContext context) {
		SQLClient mysqlClient = MySQLClient.createNonShared(vertx,
				super.getSQLConf());

		HttpServer httpServer = vertx.createHttpServer();

		router.route("/main/user/*").handler(new GetUserHandler(mysqlClient));

		httpServer.requestHandler(router::accept).listen(8887);

		httpClient(context);

	}

	public static void httpClient(TestContext context) {

		// 异步结果结束标签
		Async async = context.async();

		// 客户端
		WebClient webClient2 = WebClient.create(vertx);
		String path1 = "/main/user/?unionid=";

		String unionid = AESUtil.encrypt("eaer");
		long timeMillis = System.currentTimeMillis() + TOKEN_EXPIRED;
		String timer = String.valueOf(timeMillis);
		System.out.println("constant:" + TOKEN_EXPIRED);
		System.out.println("systemtime:" + System.currentTimeMillis());
		System.out.println("time:" + timer);
		String path2 = unionid + ":" + timer;

		String path = path1 + path2;
		// 发送get请求
		webClient2.get(8887, "localhost", path).send(res -> {
			if (res.failed()) {
				// 输出失败的状态码
				System.out.println(res.result().statusCode());

				res.cause().printStackTrace();
				context.assertFalse(true);
			} else {
				// 输出成功的状态码
				System.out.println(res.result().statusCode());
				System.out.println("用户信息：" + res.result().bodyAsString());
				context.assertTrue(false);
			}
			async.complete();
		});
	}

}
