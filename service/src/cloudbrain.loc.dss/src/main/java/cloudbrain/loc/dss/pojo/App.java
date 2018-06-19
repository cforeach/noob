package cloudbrain.loc.dss.pojo;

import java.util.Date;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName = "LOC_DB", tableName = "loc_app")
public class App extends OrmBase {

	public String action;
	// 应用ID
	@OrmField(fieldName = "Id")
	public int id;
	// 应用名称
	@OrmField(fieldName = "name")
	public String title;
	// 应用简介
	@OrmField(fieldName = "description")
	public String subtitle;
	// 图标
	@OrmField(fieldName = "icon_url")
	public String icon;
	// 下载地址
	@OrmField(fieldName = "url")
	public String link;
	// 应用包名
	@OrmField(fieldName = "package")
	public String package_name;
	// 版本号
	@OrmField(fieldName = "version_code")
	public String vcode;
	// 版本号名称
	@OrmField(fieldName = "version_name")
	public String vname;
	// 应用大小
	@OrmField(fieldName = "size")
	public long size;
	// 下载数
	@OrmField(fieldName = "download_count")
	public int down_num;
	// 分类 1:应用 2:游戏
	/**
	 * 特别注意： 数据库的一级分类对应这个PCATE； 数据库的二级分类，对应POJO的一级分类； 数据库的三级分类，对应POJO的二级分类；
	 */
	@OrmField(fieldName = "category")
	public int fake_pcate;

	public int pcate;

	// 一级分类
	@OrmField(fieldName = "sub_category")
	public Integer fake_fir_cate;

	public String fir_cate;

	// 二级分类
	@OrmField(fieldName = "thd_category")
	public Integer fake_sec_cate;

	public String sec_cate;
	// ====》 软件标签 数据库没有，就不映射试试
	// @OrmField(fieldName = "")
	public String tag;
	// 软件评分
	@OrmField(fieldName = "score")
	public double star;
	// 是否官方
	@OrmField(fieldName = "is_official")
	public Integer is_official;
	// 更新时间 时间戳
	@OrmField(fieldName = "ts")
	public Date fake_update_time;

	public String update_time;
	// 是否免费 0免费
	@OrmField(fieldName = "free")
	public int free;
	// report
	// @OrmField(fieldName = "report")
	// public String report;
	// source 无用数据，表中多余字段
	// @OrmField(fieldName = "source")
	// public String source;

	//sha1值
	@OrmField(fieldName="sha1")
	public String sha1;
	
	private String code;

	@Override
  public String toString() {
    return "App [action=" + action + ", id=" + id + ", title=" + title
        + ", subtitle=" + subtitle + ", icon=" + icon + ", link=" + link
        + ", package_name=" + package_name + ", vcode=" + vcode + ", vname="
        + vname + ", size=" + size + ", down_num=" + down_num + ", fake_pcate="
        + fake_pcate + ", pcate=" + pcate + ", fake_fir_cate=" + fake_fir_cate
        + ", fir_cate=" + fir_cate + ", fake_sec_cate=" + fake_sec_cate
        + ", sec_cate=" + sec_cate + ", tag=" + tag + ", star=" + star
        + ", is_official=" + is_official + ", fake_update_time="
        + fake_update_time + ", update_time=" + update_time + ", free=" + free
        + ", code=" + code + "]";
  }

}
