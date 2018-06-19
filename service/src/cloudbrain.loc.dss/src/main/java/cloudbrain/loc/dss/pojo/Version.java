package cloudbrain.loc.dss.pojo;

import java.util.Date;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName = "LOC_DB", tableName = "loc_version")
public class Version extends OrmBase {

	@OrmField(fieldName = "name")
	public String name;

	@OrmField(fieldName = "version")
	public Date version;

}
