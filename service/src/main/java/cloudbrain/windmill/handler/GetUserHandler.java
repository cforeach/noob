package cloudbrain.windmill.handler;

import java.io.IOException;

import org.apache.log4j.net.SyslogAppender;

import cloudbrain.windmill.utils.AESUtil;
import cloudbrain.windmill.utils.ConfReadUtils;
import cloudbrain.windmill.utils.TokenUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;

public class GetUserHandler implements Handler<RoutingContext> {

	private SQLClient mysqlClient;

	public GetUserHandler(SQLClient mysqlClient) {
		this.mysqlClient = mysqlClient;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handle(RoutingContext context) {

		// TODO Auto-generated method stub
		HttpServerRequest request = context.request();
		String param = request.getParam("unionid");

		boolean flag = TokenUtil.checkTime(param);

		if (flag) {
			// 使用工具类，获得解密后的uinionID
			String uinionID = TokenUtil.getUinionID(param);
			// 查询sql语句，只显示nickname,unionid,imgurl三列
			String sql = "select nickname,unionid,headimgurl from t_user where unionid = ?";
			// 拼装updateParams参数
			JsonArray jsonArray = new JsonArray();
			jsonArray.add(uinionID);

			// 异步IO
			mysqlClient.queryWithParams(sql, jsonArray, res -> {
				if (res.failed()) {
					context.response().setStatusCode(500).end("数据库操作失败");
					res.cause().printStackTrace();
				} else {
					JsonObject jsonObject = res.result().getRows().get(0);
					context.response().putHeader("content-type", "application/json").end(jsonObject.encode());
				}
			});

		} else {
			// unionid为空
			context.response().setStatusCode(401).end("token超时");
		}

	}

}
