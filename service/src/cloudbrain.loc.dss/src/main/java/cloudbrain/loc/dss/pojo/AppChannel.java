package cloudbrain.loc.dss.pojo;

import java.util.Date;

import net.svr.mon.orm.OrmBase;
import net.svr.mon.orm.OrmField;
import net.svr.mon.orm.OrmTable;

@OrmTable(dbName="LOC_DB",tableName="loc_app_channel")
public class AppChannel extends OrmBase{
  //id pk
@OrmField(fieldName="id")
public int id;
// appid 可以重复 
@OrmField(fieldName="appid")
public int app_id;

@OrmField(fieldName="channel")
public int channel ;
@OrmField(fieldName="order")
public Integer order ;
@OrmField(fieldName="expose")
public String expose ;
@OrmField(fieldName="download")
public String download;
@OrmField(fieldName="install")
public String install;
@OrmField(fieldName="status")
public int status ;
@OrmField(fieldName="ts")
public Date ts;
@OrmField(fieldName="note")
public String note;
}
