package cloudbrain.windmill;

import cloudbrain.windmill.constant.UrlConstant;
import cloudbrain.windmill.handler.LoginHandler;
import cloudbrain.windmill.handler.GetUserHandler;
import cloudbrain.windmill.handler.WXCallbackHandler;
import cloudbrain.windmill.utils.ConfReadUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Server extends AbstractVerticle {
	private int vertx_port;
	private JsonObject serverConf;
	private SQLClient mysqlClient;
	private JsonObject wxJsonConf;
	private WebClient webClient;
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(Server.class, new DeploymentOptions());

	}

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));// 安装日志处理器
		// 从配置文件获取webClientOptions的信息
		serverConf = ConfReadUtils.getServerConfByJson("conf.json");
		JsonObject webClientOptions = serverConf.getJsonObject("wx").getJsonObject("webClientOptions");

		WebClientOptions options = new WebClientOptions(webClientOptions);

		webClient = WebClient.create(vertx, options);

		JsonObject mysqlConf = serverConf.getJsonObject("mysql");
		mysqlClient = MySQLClient.createNonShared(vertx, mysqlConf);
		// 定义获取连接的handler
		Handler<RoutingContext> mysqlHandler = routingContext -> mysqlClient.getConnection(res -> {
			if (res.failed()) {
				routingContext.fail(res.cause());
			} else {
				SQLConnection conn = res.result();
				routingContext.put("mysqlconn", conn);
				routingContext.addHeadersEndHandler(done -> conn.close(v -> {
				}));
				routingContext.next();
			}
		});
		// 定义关闭连接的handler
		Handler<RoutingContext> mysqlfailHandler = routingContext -> {
			SQLConnection conn = routingContext.get("mysqlconn");
			if (conn != null) {
				conn.close(v -> {
				});
			}
		};

		/**
		 * 路由注册sqlHandler和failureHandler，获取连接和关闭连接
		 */
		router.route("/*").handler(mysqlHandler).handler(TimeoutHandler.create(5 * 1000)).failureHandler(mysqlfailHandler);

		// 初始化映射
		// 返回二维码地址
		router.get(UrlConstant.WX_LOGIN_URL).handler(new LoginHandler());
		// 授权凭证回调
		//router.get(UrlConstant.WX_LOGIN_CALL_BACK).handler(new WXCallbackHandler(wxJsonConf, webClient, mysqlClient));
		// 使用access_token查询用户信息
		router.get(UrlConstant.GET_USER_BY_TOKEN).handler(new GetUserHandler(mysqlClient));

		// 创建server服务，监听vertx端口
		HttpServer httpServer = vertx.createHttpServer();

		vertx_port = ConfReadUtils.getServerConfByJson("conf.json").getJsonObject("vertx").getInteger("port");

		httpServer.requestHandler(router::accept).listen(vertx_port);// 监听vertx端口

	}

}
