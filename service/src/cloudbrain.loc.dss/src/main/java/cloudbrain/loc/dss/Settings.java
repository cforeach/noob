package cloudbrain.loc.dss;

import net.svr.mon.cfg.CfgLoader;

public abstract class Settings {

	private static final String SECTION = "DSS";

	public static boolean runBrowserAppTask() {
		return CfgLoader.getInstance().getValue(SECTION, "RunBrowserAppTask", true);
	}

	/**
	 * 浏览器应用定时任务周期，单位：分钟
	 */
	public static int getBrowserAppTaskPeriod() {
		return CfgLoader.getInstance().getValue(SECTION, "BrowserAppTaskPeriod", 1);
	}

	public static String getAppID() {
		return CfgLoader.getInstance().getValue(SECTION, "AppID", "9");
	}

	public static String getAppKey() {
		return CfgLoader.getInstance().getValue(SECTION, "AppKey", "77B4310A7C2D030F623E4389880F16E9");
	}

	public static String getBrowserHost() {
		return CfgLoader.getInstance().getValue(SECTION, "BrowserHost", "3gdata.3gtest2.gionee.com");
	}

	public static String getBrowserPath() {
		return CfgLoader.getInstance().getValue(SECTION, "BrowserPath", "/Api/appsearch/increpush");
	}

	//500错误
	public static String get500Report() {
    return CfgLoader.getInstance().getValue(SECTION, "500", "服务器返回500");
  }
}
