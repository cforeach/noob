package cloudbrain.windmill.dao;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserDAO {

  public String getReplaceSQL(RoutingContext routingContext, JsonObject userInfoJsonObj)  {
    
    String createTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String nickname=userInfoJsonObj.getString("nickname");
    String headimgurl=userInfoJsonObj.getString("headimgurl");
    String unionid=userInfoJsonObj.getString("unionid");
    
    Connection conn = routingContext.get("mysqlconn");
    String sql="replace into t_user (nickname,headimgurl,unionid) values (?,?,?)";
    int i=0;
    try {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, nickname);
      ps.setString(2, headimgurl);
      ps.setString(3, unionid);
      
        i = ps.executeUpdate(sql);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return String.valueOf(i);
    /** 
    * @author cforeach 
    * @version 创建时间：2018年5月4日 下午5:23:42 
    * 类说明 
    */ 
  }
  
}