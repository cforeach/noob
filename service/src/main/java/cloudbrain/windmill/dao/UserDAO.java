package cloudbrain.windmill.dao;

import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.Map;

public class UserDAO {

  public String getInsertSql(JsonObject jsonObject) {
    StringBuffer condition = new StringBuffer();//`id`, `headimgurl`, `Create_time`, `nickname`, `sex`, `unionid`, `province`, `country`, `city`
    StringBuffer values = new StringBuffer();//


    Iterator<Map.Entry<String, Object>> iterable = jsonObject.iterator();
    while (iterable.hasNext()) {
      Map.Entry<String, Object> v = iterable.next();
      condition.append("," + v.getKey());
      values.append(",'" + v.getValue()+"'");
    }

    String sql = "insert into `t` (" + condition + ") values(" + values + ");";
    return sql.replaceAll("\\(,","(");
  }
}
