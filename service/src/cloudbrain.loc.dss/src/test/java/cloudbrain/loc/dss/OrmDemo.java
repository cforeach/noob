package cloudbrain.loc.dss;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName = "test", tableName = "test")
public class OrmDemo extends OrmBase {
	@OrmField(fieldName = "F1")
	public String F1;

	@OrmField(fieldName = "F2")
	public int F2;
}
