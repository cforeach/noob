package cloudbrain.loc.dss.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import cloudbrain.loc.dss.Settings;
import cloudbrain.loc.dss.pojo.App;
import cloudbrain.loc.dss.pojo.AppBrowser;
import cloudbrain.loc.dss.pojo.AppChannel;
import cloudbrain.loc.dss.pojo.Category;
import cloudbrain.loc.dss.pojo.Version;
import cloudbrain.loc.dss.util.HttpUtil;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import net.svr.mon.orm.OrmBase;
import net.svr.mon.stack.comm.DaemonThreadFactory;
import net.svr.mon.trace.ITracing;
import net.svr.mon.trace.TracingManager;

public class BrowserAppTask {

  private static final ITracing sTracing = TracingManager
      .getTracing(BrowserAppTask.class);
  private static final ScheduledExecutorService sExecutor = Executors
      .newScheduledThreadPool(1, new DaemonThreadFactory("browser-app-task"));

  public static void init() {
    sExecutor.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        try {
          sTracing.debug("begin to run browser app sync task ...");
          checkTasksAndExecute();
          sTracing.debug("end to run browser app sync task");
        } catch (Throwable t) {
          sTracing.error(t, "run browser app task error.");
        }
      }
    }, 0, Settings.getBrowserAppTaskPeriod() * 60 * 1000,
        TimeUnit.MILLISECONDS);
  }

  private static void checkTasksAndExecute() throws Exception {
    
    Version lastVersion = new Version();
    //数据库版本号
    lastVersion.conditionLoad("name=?", "browser_app_version");
    //最大的数据更新时间，局部变量
    Version currentVersion = new Version();
    List<App> content = new ArrayList<>();
    checkAddAndDelete(content, lastVersion, currentVersion);
    checkUpdate(content, lastVersion,currentVersion);

    if (content.size() <= 0) {
      // 没有要更新的数据，退出当前任务
      return;
    }

    // 封装请求参数
    Gson gson = new Gson();
    String json = gson.toJson(content);

    // 把发送的内容放在输出日志中
    sTracing.info("本次推送条数: {}", content.size());
    sTracing.info("数据库查询结果: {}", json);
  
    // WebClient
    Vertx vertx = Vertx.vertx();
    WebClient webClient = WebClient.create(vertx);
    
    MultiMap form=MultiMap.caseInsensitiveMultiMap();
    form.set("uuid", Settings.getAppID())
    .set("apikey", Settings.getAppKey())
    .set("count", String.valueOf(content.size()))
    .set("count", String.valueOf(content.size()))
    .set("content", json);
    
    webClient.post(80, Settings.getBrowserHost(), Settings.getBrowserPath()).sendForm(form, res->{
      if (res.succeeded()) {
        sTracing.info("reponse body: {}", res.result().body());
         sTracing.info("返回状态为:{}", res.result().statusCode());
         // 判断返回结果是否真确
         // 200
         if (res.result().statusCode() == 200) { 
           Version locVersion = new Version();
           // 当返回值正确时，执行更新本地最大版本号操作
           locVersion.updateField("version=?", "name=?", currentVersion.version,
               "browser_app_version");
         }
         // 500
         if (res.result().statusCode() == 500) {
           sTracing.info("500:{}", Settings.get500Report());
         } if(res.result().statusCode()!= 500&&res.result().statusCode()!=200) {
           // 其他状态码
           sTracing.info("其他状态码返回状态为:{}", res.result().statusCode());
         }
       }
       // 发送失败了
       else {
         sTracing.error(res.cause(), "send reuqest to {} failed.",
             Settings.getBrowserHost());
       }
    });
webClient.close();
  }

  /**
   * 方法含义：定义一个用于检查新增和删除元素的方法；
   * 参数：
   * 返回值：
   * 实现：b.ts>c.version
   */
  public static void checkAddAndDelete(List<App> content ,Version lastVersion,Version currentVersion) {
    //1.通用sql
    String commonSql="select * from loc_app_channel where ts >? and channel=4";
    //2.创建Orm对象
    //AppBrowser appBrowser=new AppBrowser();
    AppChannel appChannel=new AppChannel();
    //3.查询出b表中所有更新了的元素集合
    List<AppChannel> list = appChannel.loadArray(AppChannel.class, commonSql, lastVersion.version);
    //4.根据集合中元素的状态，判断是新增还是下架
      //4.1判断集合是否为空
      if(list==null||list.size()<1) {
        //如果为空，return 
        return ;
      }
      //4.2不为空，证明有更新操作
      if(list!=null&&list.size()>=1) {
        //4.2.1遍历集合 每个元素为appBrowser2
        for (AppChannel appBrowser2 : list) {
          int status = appBrowser2.status;
          //4.2.1.1判断status。0下架 1新增
            if(status==0) {
              //4.2.1.1.1执行删除操作 调用方法， 得到对应的app
              App app = checkNullAndSetUpdateTime(appBrowser2);
              //4.2.1.1.2setAction
              app.action="delete";
              //4.2.1.1.3添加到局部变量content集合
              content.add(app);
              //更新版本号
              if(appBrowser2.ts.getTime()>lastVersion.version.getTime()) {
                currentVersion.version=appBrowser2.ts;
              }
            }
          //4.2.1.2 新增
            if(status==1) {
              //4.2.1.2.1调用方法，得到对应的app
              App app = checkNullAndSetUpdateTime(appBrowser2);
              //4.2.1.2.2赋值action
              app.action="add";
              //4.2.1.2.3添加到content
              content.add(app);
              //更新版本号
              if(appBrowser2.ts.getTime()>lastVersion.version.getTime()) {
                currentVersion.version=appBrowser2.ts;
              }
            }
        }
      }
      return;
  }
  
  
  
  
  
  /**方法内容：
   *  1.检查空值:如果对象的字段为null，赋予""，避免对方收不到这个字段，因为对方设置了过滤null功能；
   *  2.setUpdateTime
   *  3.set2、3级分类名称：
   * @param appBrowser2
   */
  public static App checkNullAndSetUpdateTime(AppChannel appBrowser2) {
    App loc_app;
    //得到对应app的id
    int id = appBrowser2.app_id;
    //根据id，取到对应的app实体
    String getAppSql="select * from loc_app where id=?";
    App app=new App();
    List<App> list = App.loadArray(App.class, getAppSql, id);
    //只有一个id，所以集合只有1个元素，但是谨慎起见，先判断集合是否为null,理论上不可能为null，因为渠道应用的添加是从app列表中取出来的
    if(list==null||list.size()<1) {
      //说明browser表中有多余的id，未在app表中出现
      sTracing.error("表中id同步出错：{}", "browser表中有多余的id，未在app表中出现");
      return null;
    }
    if(list!=null||list.size()==1) {
      //查出来了具体的app，取出唯一的元素app :( 孤独寂寞冷单身狗
       loc_app = list.get(0);
        //1.setUpdateTime
          // 取出数据库中的对应的date
          Date date = loc_app.fake_update_time;
          // 拿到long值
          long time = date.getTime();
          // 文档要求截取前10位，用自定义方法截取
          long time2 = getTime(time);
          loc_app.update_time = String.valueOf(time2);
        //2.checkNull
          //2.1 判断icon
          if (loc_app.icon == null) {
            loc_app.icon = "";
          }
          //2.2 link
          if (loc_app.link == null) {
            loc_app.link = "";
          }
          //2.3 判断vcode
          if (loc_app.vcode == null) {
            loc_app.vcode = "";
          }
          //2.4 判断is_official 默认是0
          if (loc_app.is_official == null) {
            loc_app.is_official = 0;
          }
          //2.5 判断description
          if (loc_app.subtitle == null) {
            loc_app.subtitle = "";
          }
          //2.6 查询sha1
          if (loc_app.sha1 == null) {
            loc_app.sha1 = "";
          }
          //2.7 查询一级分类信息
          int pcateId = loc_app.fake_pcate;
        //查询分类的通用sql
          String queryCateSql = "select  * from loc_category where id =?";
          List<Category> pcateList = Category.loadArray(Category.class,
                  queryCateSql, pcateId);
              // 拿到用id查询出来的对应的pojo
              Category pcate = pcateList.get(0);
              if(pcate!=null) {
            	 if("游戏".equals(pcate.name)) 
            	 {loc_app.pcate=2;}
            	 else {
            		 loc_app.pcate=1;
            	 }
              }
          
        //3.set2、3级分类属性
          
          //二级分类为空：赋值""，三级分类也就为空了
          if (loc_app.fake_fir_cate == null) {
            loc_app.fir_cate = "";
            //三级
            loc_app.sec_cate = "";
          }
          //不为空且不等于"" 正常操作
          if(!loc_app.fake_fir_cate.equals("")) {
            
            int firCateId = loc_app.fake_fir_cate;
            // 根据id查询SQL
            // 封装SQL
            List<Category> loadArray2 = Category.loadArray(Category.class,
                queryCateSql, firCateId);
            // 拿到用id查询出来的对应的pojo
            Category loc_category2 = loadArray2.get(0);
            // 取出name属性
            String firName = loc_category2.name;
            // 二级赋值
            loc_app.fir_cate = firName;
            //判断三级
            if (loc_app.fake_sec_cate!= null) {
              List<Category> loadArray3 = Category.loadArray(Category.class,
                  queryCateSql, pcateId);
              // 取出pojo
              Category loc_category3 = loadArray3.get(0);
              // 拿到Name
              String SecName = loc_category3.name;
              // 封装属性
              loc_app.sec_cate = SecName;
            } // else
          }//if
      
          //在list!=null内返回loc_app
          return loc_app;
      }
    //理论不可能走到这一步
    return null;
   
  }
  
  
 
  /**
   * 检查更新总记录
   * 
   * @return
   */
  private static void checkUpdate(List<App> content, Version lastVersion, Version currentVersion ) {
    // 1.取出loc_app表中的数据ts大于loc_app_channel的数据 更新
    String sql = "SELECT distinct * FROM loc_app a,loc_app_channel b WHERE a.id = b.appid AND a.ts>? AND b.status=1 and b.channel=4";
    App loc_app = new App();

    List<App> list = App.loadArray(App.class, sql, lastVersion.version);

    // 判断集合
    if (list == null || list.size() < 1) {
      return;
    }

    for (App pojo : list) {
      pojo.action = "update";

      Date date = pojo.fake_update_time;
      // 拿到long值
      long time = date.getTime();
      // 文档要求截取前10位，用自定义方法截取
      long time2 = getTime(time);
      loc_app.update_time = String.valueOf(time2);

      // 查询一级分类信息
      int pcateId = pojo.fake_pcate;
    //查询分类的通用sql
      String queryCateSql = "select  * from loc_category where id =?";
      List<Category> pcateList = Category.loadArray(Category.class,
              queryCateSql, pcateId);
          // 拿到用id查询出来的对应的pojo
          Category pcate = pcateList.get(0);
          if(pcate!=null) {
        	 if("游戏".equals(pcate.name)) 
        	 {pojo.pcate=2;}
        	 else {
        		 pojo.pcate=1;
        	 }
          }
      // 查询二级分类
      if (pojo.fake_fir_cate == null) {
    	  pojo.fir_cate = "";
      }

      if (pojo.fake_fir_cate != null)  {
        int firCateId = pojo.fake_fir_cate;
        // 根据id查询SQL
        Category.loadArray(Category.class, queryCateSql, pcateId);
        // 封装SQL
        List<Category> loadArray2 = Category.loadArray(Category.class,
            queryCateSql, firCateId);
        // 拿到用id查询出来的对应的pojo
        Category loc_category2 = loadArray2.get(0);
        // 取出name属性
        String firName = loc_category2.name;
        // 赋值
        pojo.fir_cate = firName;
      }

      // 判断三级分类
      if (pojo.fake_sec_cate == null) {
    	  pojo.sec_cate = "";
      }

      if (pojo.fake_sec_cate != null) {
        // 根据id查询sql
        List<Category> loadArray3 = Category.loadArray(Category.class,
            queryCateSql, pcateId);

        Category loc_category3 = loadArray3.get(0);
        // 拿到Name
        String SecName = loc_category3.name;
        // 封装属性
        pojo.sec_cate = SecName;

      }

      // 查询sha1
      if (pojo.sha1 == null) {
        pojo.sha1 = "";
      }
      
      
      content.add(pojo);
      //更新currentVersion
      if(pojo.fake_update_time.getTime()>lastVersion.version.getTime()) {
        currentVersion.version=pojo.fake_update_time;
      }
    }
  }

  /**
   * 截取时间戳前10位
   * 
   */
  private static long getTime(long time) {
    String string = String.valueOf(time).substring(0, 10);
    Long long1 = Long.valueOf(string);

    return long1;
  }

  /**
   * 判断loc_app空值
   */
  public static App checkNull(App loc_app) {

    if (loc_app.icon == null) {
      loc_app.icon = "";
    }
    // link
    if (loc_app.link == null) {
      loc_app.link = "";
    }
    // 判断vcode
    if (loc_app.vcode == null) {
      loc_app.vcode = "";
    }

    // 判断is_official 默认是0
    if (loc_app.is_official == null) {
      loc_app.is_official = 0;
    }
    // 判断description
    if (loc_app.subtitle == null) {
      loc_app.subtitle = "";
    }

    // 查询sha1
    if (loc_app.sha1 == null) {
      loc_app.sha1 = "";
    }
    
    return loc_app;
  }
}
