package net.heartsome.license.webservice;

public interface IService{

	
	public byte[] getServerPublicKey() throws Exception;
	
	
	public String buyLicense(String username, String email,String productInfo);
	
	/**
	 *    激活许可证
	 * @param info  客户端信息，包括许可证号、硬件指纹、安装码
	 * @param clientPublicKey 客户端公钥
	 * @return 激活信息
	 */
	public String activeLicense(String info, byte[] clientPublicKey) throws Exception;
	
	/**
	  *   校验许可证
	 * @param info 客户端信息，包括许可证号、最新的硬件指纹
	 * @param clientPublicKey 客户端公钥
	 * @return 校验信息
	 */
	public String checkLicense(String info, byte[] clientPublicKey) throws Exception;
	
	/**
	   *   注销许可证
	 * @param info 客户端信息，包括许可证号、最新的硬件指纹
	 * @param clientPublicKey 客户端公钥
	 * @return 注销信息
	 */
	public String logoutLicense(String info, byte[] clientPublicKey) throws Exception;
	
	
	/**
	   *   获取试用许可证的截止日期
	 * @param key
	 * @return
	 */
	public String getTempEndDate(String key, byte[] clientPublicKey) throws Exception;
}