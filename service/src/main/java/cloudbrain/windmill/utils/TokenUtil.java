package cloudbrain.windmill.utils;

public class TokenUtil {

	
	/**
	 * 获取解密后的unionID
	 * @param unionid
	 * @return
	 */
	public static String getUinionID(String unionid) {
		if(unionid==null) {
			return null;
		}
		String[] split = unionid.split(":");
		String str= split[0];
		String unionID = AESUtil.decrypt(str);
		return unionID;
	}
	
	
	/**
	 * 判断时间是否超时
	 * @param str
	 * @return
	 */
	public static boolean checkTime(String str) {
		
		//先判断unionid是否为空
		if(str==null) {
			return false;
		}
		//unionid不为空
		else {
			//以":"为单位切割字符串
			String[] split = str.split(":");
			
			//元素0是加密后的unionid,元素1是时间戳ts
			String unionid = split[0];
			String ts = split[1];
			//判断是否超时
			
			//System.out.println("用户登录ts=="+Long.valueOf(ts));
			//System.out.println("系统当前time=="+System.currentTimeMillis());
			
			if(Long.valueOf(ts)<System.currentTimeMillis()) {
				//登录时间+30天后小于当前时间，就超时了
				return false;
			}
			else {
				return true;
			}
		}
	}
}
