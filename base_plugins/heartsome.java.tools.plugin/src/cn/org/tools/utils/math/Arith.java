/**
 * Arith.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.math;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 由于Java的简单类型不能够精确的对浮点数进行运算，该工具类提供精确的浮点数运算，包括加减乘除和四舍五入。.
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public final class Arith {

	/** 默认除法运算精度. */
	private static final int DEF_DIV_SCALE = 10;

	/**
	 * 构造方法，该类不能实例化
	 */
	private Arith() {

	}

	/**
	 * 提供精确的加法运算。
	 * @param v1
	 *            被加数
	 * @param v2
	 *            加数
	 * @return double
	 * 			  两个参数的和
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		double result = b1.add(b2).doubleValue();
		b1 = null;
		b2 = null;
		return result;
	}

	/**
	 * 获得随机数.
	 * @param num
	 *            生成的随机数最大值：num - 1 
	 * @return int
	 * 			  生成的随机数的范围：[0,num)
	 */
	public static int random(int num) {
		Random random = new Random();
		return Math.abs(random.nextInt()) % num;
	}

	/**
	 * 提供精确的减法运算。.
	 * @param v1
	 *            被减数
	 * @param v2
	 *            减数
	 * @return double
	 * 			  两个参数的差
	 */
	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		double result = b1.subtract(b2).doubleValue();
		b1 = null;
		b2 = null;
		return result;
	}

	/**
	 * 提供精确的乘法运算。.
	 * @param v1
	 *            被乘数
	 * @param v2
	 *            乘数
	 * @return double
	 * 			  两个参数的乘积
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		double result = b1.multiply(b2).doubleValue();
		b1 = null;
		b2 = null;
		return result;
	}

	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。.
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @return double
	 * 			  两个参数的商
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		System.out.println(5 / 4 + 1);
		System.out.println(random(6));
//		System.out.println(div(1, 9, -1));
	}

	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由 scale 参数指定精度，以后的数字四舍五入。.
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @param scale
	 *            表示需要精确到小数点以后几位。
	 * @return double
	 * 			  两个参数的商
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		double result = b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
		b1 = null;
		b2 = null;
		return result;
	}

	/**
	 * 提供精确的小数位四舍五入处理。.
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return double
	 * 			  四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		double result = b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
		b = null;
		one = null;
		return result;
	}
}
