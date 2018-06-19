package cloudbrain.loc.dss.pojo;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName = "LOC_DB", tableName = "loc_category")
public class Category extends OrmBase {

	@OrmField(fieldName = "id")
	public int id;
	@OrmField(fieldName = "name")
	public String name;
	@OrmField(fieldName = "parent_id")
	public Integer parentId;
	@OrmField(fieldName = "level")
	public Integer level;

	@Override
	public String toString() {
		return "Loc_category [id=" + id + ", name=" + name + ", parentId=" + parentId + ", level=" + level + "]";
	}

}
