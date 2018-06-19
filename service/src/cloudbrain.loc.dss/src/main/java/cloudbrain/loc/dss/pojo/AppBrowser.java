package cloudbrain.loc.dss.pojo;

import java.util.Date;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName = "LOC_DB", tableName = "loc_app_browser")
public class AppBrowser extends OrmBase {

	@Override
  public String toString() {
    return "AppBrowser [app_id=" + app_id + ", status=" + status + ", ts=" + ts
        + "]";
  }
  // app id
	@OrmField(fieldName = "app_id")
	public int app_id;
	// status
	@OrmField(fieldName = "status")
	public int status;
	// 时间戳
	@OrmField(fieldName = "ts")
	public Date ts;
}
