/**
 * DateUtilsBasic.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.org.tools.utils.constant.Constants;
import cn.org.tools.utils.string.StringUtilsBasic;

/**
 * 所有关于日期的基本操作.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class DateUtilsBasic {
	
	/**
	 * 构造方法.
	 */
	protected DateUtilsBasic() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/** The Constant YYYY_MM_DD_HH_MM_SS. */
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	/** The Constant FULL. */
	public static final int FULL = DateFormat.FULL;

	/**
	 * 获取当前时间.
	 * @return String 
	 * 				以yyyy-MM-dd HH:mm:ss格式标准
	 */
	public static String getCurDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		return sdf.format(new Date());

	}

	/**
	 * 获取当前时间前time的时间.
	 * @param time
	 *         		毫秒数
	 * @return String
	 * 				已格式化的时间字符串。
	 */
	public static String getCurDateBefore(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		return sdf.format(new Date().getTime() - time);
	}

	/**
	 * 获取当前日期
	 * @param format
	 *            	日期/时间格式
	 * @return String
	 * 				已格式化的时间字符串。
	 */
	public static String getCurDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());

	}

	/**
	 * 将给定日期格式化为字符串.
	 * @param date
	 *            要格式化为时间字符串的时间值。
	 * @param format
	 *            日期/时间格式
	 * @return String
	 * 			  已格式化的时间字符串。
	 */
	public static String getDateStr(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);

	}

	/**
	 * 获取现在的年份.
	 * @return int
	 * 			 公元年
	 */
	public static int getCurYear() {
		return new GregorianCalendar().get(Calendar.YEAR);
	}

	/**
	 * 获取现在的月份.
	 * @return int 
	 * 			  0表示年的第一个月
	 */
	public static int getCurMonth() {
		return new GregorianCalendar().get(Calendar.MONTH);
	}

	/**
	 * 获取当前日期所在月份的天数，假如今天为 2011 年 5 月 3 日,则返回值为 3
	 * @return int 
	 * 			  当前日期在当前月份中的天数，一个月中第一天的值为 1。 
	 */
	public static int getCurDay() {
		return new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * GMT时间就是英国格林威治时间，也就是世界标准时间，是本初子午线上的地方时，是0时区的区时，与中国的标准时间北京时间（东八区）相差8小时,
	 * 即晚8小时。 获取现在的格林威治标准时间，也称格林尼治平均时.
	 * @return String 
	 * 				以 yyyyMMddTHHmmssZ 格式标准
	 */
	public static String getCurGMTDate() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") + calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") + (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR);
		String date = longyear + mon + mday + "T" + hour + min + sec + "Z";
		return date;
	}

	/**
	 * 将GMT格式时间的字符串转换为当前时间的字符串.
	 * @param strGMTDate
	 *            	以yyyyMMddTHHmmssZ格式标准
	 * @return String 
	 * 				以默认语言环境的默认格式化风格的格式，如果发生异常，返回空串""
	 */
	public static String changeGMTDate2LocalDate(String strGMTDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try {
			int second = Integer.parseInt(strGMTDate.substring(13, 15));
			int minute = Integer.parseInt(strGMTDate.substring(11, 13));
			int hour = Integer.parseInt(strGMTDate.substring(9, 11));
			int date = Integer.parseInt(strGMTDate.substring(6, 8));
			int month = Integer.parseInt(strGMTDate.substring(4, 6)) - 1;
			int year = Integer.parseInt(strGMTDate.substring(0, 4));
			calendar.set(year, month, date, hour, minute, second);

			DateFormat dt = DateFormat.getDateTimeInstance();

			return dt.format(calendar.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}

	}

	/**
	 * 获取日期字符串中的日，如果月 MM 大于12，则向年 yyyy 进位，日大于当月的最大日期，也向前进位 该方法不使用给定字符串的整个文本。.
	 * @param strdate
	 *         		截取前 yyyy-MM-dd 模式的字符，将后面的字符舍去
	 * @return int 
	 * 				日期在月份中的天数，1表示第一天, 如果 strdate 不能成功的转换成 Date，返回-1
	 */
	public static int getDayOfDate(String strdate) {
		int result = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(strdate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			result = calendar.get(Calendar.DATE);
			return result;
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return -1;
		}

	}

	/**
	 * 获取字符串所表示的日期所在月份第一天的字符串(该类中的 getFirstDayOfMonth 也是同样的意义).
	 * @param strdate
	 *            	要解析的日期字符串
	 * @return String 
	 * 				如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 * @see DateUtilsBasic#getFirstDayOfMonth(String)
	 */
	public static String getMonthBegin(String strdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
		Date date = null;
		try {
			date = sdf.parse(strdate);
			return sdf1.format(date) + "-01";
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 获取字符串所表示的日期所在月份最后一天的字符串(该类中的 getEndDateOfMonth 和 getLastDayOfMonth 也是同样的意义).
	 * @param strdate
	 *            	要解析的日期字符串
	 * @return String 
	 * 				如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 * @see DateUtilsBasic#getEndDateOfMonth(String)
	 * @see DateUtilsBasic#getLastDayOfMonth(String)
	 */
	public static String getMonthEnd(String strdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
		Calendar calendar = Calendar.getInstance();
		Date date = null;
		try {
			date = sdf.parse(strdate);
			calendar.setTime(date);
			return sdf1.format(date) + "-" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 比较 strBegin 表示的日期与 strEnd 表示的日期相差多少天.
	 * @param strBegin
	 *            	开始日期字符串
	 * @param strEnd
	 *            	结束日期字符串
	 * @param defaultValue
	 *            	如果解析字符串时发生异常，返回该值。
	 * @return int 
	 * 				如果strBegin在strEnd之后，返回负数
	 */
	public static int getDifferDays(String strBegin, String strEnd, int defaultValue) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = null, date2 = null;
		try {
			date1 = f.parse(strBegin);
			date2 = f.parse(strEnd);
			int days = (int) ((date2.getTime() - date1.getTime()) / 86400000);
			return days;
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return defaultValue;
		}

	}

	/**
	 * 比较 strEnd 表示的日期是否在 strBegin 表示的日期之前.
	 * @param strBegin
	 *            开始日期(格式为 "HH:mm:ss")
	 * @param strEnd
	 *            截止日期(格式为 "HH:mm:ss")
	 * @return boolean
	 * 			  当且仅当 strEnd 表示的瞬间比 strBegin 表示的瞬间早，才返回 true；否则返回 false
	 * 			  (strEnd 与 strBegin 表示的日期完全相同时，返回 false；解析字符串时发生异常，返回false)。 
	 */
	public static boolean bforeDifferTimes(String strBegin, String strEnd) {
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		Date date1 = null, date2 = null;
		try {
			date1 = f.parse(strBegin);
			date2 = f.parse(strEnd);
			return date2.before(date1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 比较 str2 表示的日期是否在 str1 表示的日期之前.
	 * @param str1
	 *            日期字符串1
	 * @param str2
	 *            日期字符串2
	 * @param format
	 *            日期/时间格式
	 * @return boolean
	 * 			  当且仅当 str2 表示的瞬间比 str1 表示的瞬间早，才返回 true；否则返回 false
	 * 			  (str2 与 str1 表示的日期完全相同时，返回 false；解析字符串时发生异常，返回false)。 
	 */
	public static boolean bforeDifferTimes(String str1, String str2, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date date1 = null, date2 = null;
		try {
			date1 = f.parse(str1);
			date2 = f.parse(str2);
			return date2.before(date1);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 比较 str2 表示的日期是否在 str1 表示的日期之前.
	 * @param str1
	 *            日期字符串1
	 * @param str2
	 *            日期字符串2
	 * @param format
	 *            日期/时间格式
	 * @return boolean
	 * 			  当且仅当 str2 表示的瞬间比 str1 表示的瞬间早，或者 str2 与 str1 表示的日期完全相同时，才返回 true；否则返回 false
	 * 			  (解析字符串时发生异常，返回false)。 
	 */
	public static boolean bforeOrEqualDifferTimes(String str1, String str2, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date date1 = null, date2 = null;
		try {
			date1 = f.parse(str1);
			date2 = f.parse(str2);
			return date2.before(date1) || date1.equals(date2);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 检查 str1 表示的日期是否与 str2 表示的日期相同.
	 * @param str1
	 *            日期字符串1
	 * @param str2
	 *            日期字符串2
	 * @param format
	 *            日期/时间格式
	 * @return boolean
	 * 			  日期相同，返回 true；其他情况，返回 false
	 */
	public static boolean checkEqauls(String str1, String str2, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date date1 = null, date2 = null;
		try {
			date1 = f.parse(str1);
			date2 = f.parse(str2);
			return date1.getTime() == date2.getTime();
		} catch (ParseException e) {
			return false;
		}
	}
	
	/**
	 * 获取 nowdate 延后 delay 个工作日的时间，星期六，星期天不算在工作日，星期一，二，三，四，五算工作日.
	 * @param nowdate
	 *            日期
	 * @param delay
	 *            延后的工作日
	 * @return Date
	 * 			  nowdate 延后 delay 个工作日的时间
	 */
	public static Date getNextDay(Date nowdate, int delay) {
		long myTime;
		Calendar calendar = new GregorianCalendar();
		for (int i = 0; i < delay; i++) {
			myTime = (nowdate.getTime() / 1000) + i * 24 * 60 * 60;
			calendar.setTimeInMillis(myTime);
			if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				nowdate.setTime(myTime * 1000);
			}
		}
		return nowdate;
	}

	/**
	 * 获取指定日期所在月份的天数.
	 * @param date
	 *            	日期格式为 "yyyy-mm-dd hh:MM:ss"
	 * @return int
	 * 				指定日期所在月份的天数.
	 */
	public static int getDaysForDate(String date) {
		Date curDate = strToDate(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(curDate);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 建议改名 getDelaySchoolYear 获取当前时间，经过 num 偏移的学年的字符串.
	 * @param num
	 *            偏移量
	 * @return String
	 * 			  当前时间偏移 num 后得到的学年字符串	
	 */
	public static String getYear(int num) {
		Calendar calendar = new GregorianCalendar();
		int year = calendar.get(Calendar.YEAR) + num;
		int month = calendar.get(Calendar.MONTH);
		String yearstr = "";
		if (month < 8) {
			yearstr = String.valueOf(-1) + "/" + String.valueOf(year);
		} else {
			yearstr = String.valueOf(year) + "/" + String.valueOf(year + 1);
		}
		return yearstr;
	}

	/**
	 * 建议改名 getSchoolYear 获得时间所对应的学年.
	 * @param parseDate
	 *            要解析的日期
	 * @return 	String
	 * 			  如果传入值为null，返回当前学年
	 */
	public static String getYear(Date parseDate) {
		if (parseDate == null) {
			return getYear(0);
		}
		String result = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(parseDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		if (month < 8) {
			result = String.valueOf(year - 1) + "/" + String.valueOf(year);
		} else {
			result = String.valueOf(year) + "/" + String.valueOf(year + 1);
		}
		return result;
	}

	/**
	 * 判断传入的日期是否是今天或在今天之前.
	 * @param date
	 *            传入的日期
	 * @param model
	 *            日期的模式，例如 "yyyy-MM-dd HH:mm:ss"
	 * @param defaultVaule
	 *            如果解析字符串时发生异常，返回该值。
	 * @return boolean
	 * 			  如果date表示的日期在今天或今天之前，返回 true；否则返回 false
	 */
	public static boolean beforeToday(String date, String model, boolean defaultVaule) {

		SimpleDateFormat sdf = new SimpleDateFormat(model);
		Date today = new Date();
		Date parseDate = null;
		try {
			parseDate = sdf.parse(date);
			return parseDate.before(today);
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return defaultVaule;
		}
	}

	/**
	 * 获得数字 i 所代表的星期，所对应的短字母.
	 * @param i
	 *          数字
	 * @return String
	 * 				如果i = 1，返回 "S"(即 "SUNDAY" 的首字母)；
	 * 				如果i = 2，返回 "M"(即 "MONDAY" 的首字母)；
	 * 				...
	 * 				如果i > 7 或 i < 1，返回空串"";
	 */
	public static String getDayName(int i) {
		String dayname = "";
		switch (i) {
		case Calendar.MONDAY:
			dayname = "M";
			break;
		case Calendar.TUESDAY:
			dayname = "T";
			break;
		case Calendar.WEDNESDAY:
			dayname = "W";
			break;
		case Calendar.THURSDAY:
			dayname = "T";
			break;
		case Calendar.FRIDAY:
			dayname = "F";
			break;
		case Calendar.SATURDAY:
			dayname = "S";
			break;
		case Calendar.SUNDAY:
			dayname = "S";
			break;
		default:
			break;
		}
		return dayname;
	}

	/**
	 * 获取月份的完整名.
	 * @param month
	 *            	0 表示第一月January
	 * @return String
	 * 				月份的完整名称，如果month < 0 或 month >= 12，返回空串
	 */
	public static String getMonthStr(int month) {
		String[] monthName = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December", };

		if (month < 0 | month >= 12) {
			return "";
		} else {
			return monthName[month];
		}
	}

	/**
	 * 转换日期格式.
	 * @param date
	 *            要转换的日期
	 * @return String
	 * 				返回格式为 1 December 1990 的日期字符串，若 date 为空，则返回空串""
	 */
	public static String getDate(String date) {
		if (date == null || date.equals("")) {
			return "";
		}
		String datestr = "";
		Date datetime = DateUtilsBasic.strToDate(date);
		Calendar c = Calendar.getInstance();
		c.setTime(datetime);
		datestr += getCurDay(c);
		datestr += " " + getMonthStr(getCurMonth(c));
		datestr += " " + getCurYear(c);
		return datestr;
	}

	/**
	 * 获取上一学年.
	 * @param year
	 *            	学年,yyyy/yyyy模式
	 * @return String
	 * 				学年 year 的上一学年,yyyy/yyyy模式
	 */
	public static String getLastYear(String year) {
		String[] yearArray = year.split("/");
		return (Integer.parseInt(yearArray[0]) - 1) + "/" + (Integer.parseInt(yearArray[1]) - 1);
	}

	/**
	 * 获取下一学年.
	 * @param year
	 *            the year
	 * @return String
	 * 			  学年 year 的下一学年,yyyy/yyyy模式
	 */
	public static String getNextYear(String year) {
		String[] yearArray = year.split("/");
		return (Integer.parseInt(yearArray[0]) + 1) + "/" + (Integer.parseInt(yearArray[1]) + 1);
	}

	/**
	 * 建议改名, 实现有错 判断时间1是否在时间2之前.
	 * @param date1
	 *            String 类型的时间1 格式为"yyyy-mm-dd"
	 * @param date2
	 *            String 类型的时间1 格式为"yyyy-mm-dd"
	 * @return boolean
	 * 			  ture: date2 > date 1; false: date2 <= date1
	 */
	public static boolean getCompareResult(String date1, String date2) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(strToDate(date1));
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(strToDate(date2));
		if (calendar2.after(calendar1)) {
			return true;
		}
		return false;
	}

	/**
	 * 建议改名 将毫秒时间转换为时间字符串.
	 * @param date
	 *            	1970-01-01 00:00:00开始的毫秒数
	 * @return String 
	 * 				时间字符串，以yyyy-MM-dd HH:mm:ss格式
	 */
	public static String longToStr(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(calendar.getTime());
	}

	/**
	 * 返回指定时间的下一个月.
	 * @param curDate
	 *            	指定的时间字符串
	 * @return String 
	 * 				时间字符串，以 yyyy-MM-dd 格式
	 */
	public static String getNextMonth(String curDate) {
		Calendar now = Calendar.getInstance();
		now.setTime(DateUtilsBasic.strToDate(curDate));
		now.add(Calendar.MONTH, 1);
		return DateUtilsBasic.dateToStr(new Date(now.getTimeInMillis()));
	}
	
	/**
	 * 返回指定时间的延后或前移几月的时间.
	 * @param curDate 指定的时间字符串
	 * @param num 延后或前移的月数
	 * @return 时间字符串，以 yyyy-MM-dd 格式;
	 */
	public static String getNextMonth(String curDate, int num) {
		Calendar now = Calendar.getInstance();
		now.setTime(DateUtilsBasic.strToDate(curDate));
		now.add(Calendar.MONTH, num);
		return DateUtilsBasic.dateToStr(new Date(now.getTimeInMillis()));
	}

	/**
	 * 返回指定时间的上一个月.
	 * @param curDate
	 *            	指定的时间字符串
	 * @return String 
	 * 				时间字符串，以 yyyy-MM-dd 格式
	 */
	public static String getPreMonth(String curDate) {
		Calendar now = Calendar.getInstance();
		now.setTime(DateUtilsBasic.strToDate(curDate));
		now.add(Calendar.MONTH, -1);
		return DateUtilsBasic.dateToStr(new Date(now.getTimeInMillis()));
	}

	/**
	 * 检查 curDate 是否在 startDate 前1天之后，endDate 后1天之前.
	 * @param startDate
	 *            	开始日期字符串，以 yyyy-MM-dd 格式
	 * @param endDate
	 *            	结束日期字符串，以 yyyy-MM-dd 格式
	 * @param curDate
	 *            	要判断的日期字符串，以 yyyy-MM-dd 格式
	 * @return boolean
	 * 				如果 curDate 在 startDate 前1天之后，在 endDate 后1天之前，返回true；否则返回 false.
	 */
	public static boolean isBetween(String startDate, String endDate, String curDate) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(strToDate(startDate));
		startCalendar.add(Calendar.DAY_OF_YEAR, -1);
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(strToDate(endDate));
		endCalendar.add(Calendar.DAY_OF_YEAR, 1);
		Calendar curCalendar = Calendar.getInstance();
		curCalendar.setTime(strToDate(curDate));
		if (startCalendar.before(curCalendar) && curCalendar.before(endCalendar)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取日期所在月的第一天(该类中的 getMonthBegin 也是同样的意义).
	 * @param str
	 * 				日期字符串，以 yyyy-MM-dd 格式
	 * @return String 
	 * 				以 yyyy-MM-dd 格式, 如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 * @see DateUtilsBasic#getMonthBegin(String)
	 */
	public static String getFirstDayOfMonth(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		try {
			Date date = format.parse(str);
			calendar.setTime(date);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			return format.format(calendar.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 获取日期所在月的最后一天(该类中的 getEndDateOfMonth 和 getMonthEnd 也是同样的意义).
	 * @param str
	 *            	日期字符串，以 yyyy-MM-dd 格式
	 * @return String 
	 * 				以 yyyy-MM-dd 格式, 如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 * @see DateUtilsBasic#getEndDateOfMonth(String)
	 * @see DateUtilsBasic#getMonthEnd(String)
	 */
	public static String getLastDayOfMonth(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		try {
			Date date = format.parse(str);
			calendar.setTime(date);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.MONTH, 1);
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			return format.format(calendar.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 得到指定日期的星期六.
	 * @param date
	 *            	日期字符串，以 yyyy-MM-dd 格式
	 * @return String 
	 * 				以 yyyy-MM-dd 格式，星期六是该周的最后一天, 如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 */
	public static String getSaterdayOfWeek(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date datePoint = dateFormat.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(datePoint);
			int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
			c.add(Calendar.DATE, -dayofweek + 6);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(c.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 得到指定日期所在周的星期天.
	 * @param date
	 *            	日期字符串，以 yyyy-MM-dd 格式
	 * @return String 
	 * 				以 yyyy-MM-dd 格式，星期天是该周的第一天, 如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 */
	public static String getSundayOfWeek(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date datePoint = dateFormat.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(datePoint);
			int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
			c.add(Calendar.DATE, -dayofweek);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(c.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}

	}

	/**
	 * 得到指定日期所在周的第一天（星期天）顺延days天的日期.
	 * @param date
	 *            	日期字符串，以 yyyy-MM-dd 格式
	 * @param days
	 *            	顺延的天数，(0--星期天,1--星期一,2--星期二,以此类推)
	 * @return String 
	 * 				以 yyyy-MM-dd 格式, 如果传入参数为 null 或者解析字符串时发生异常，返回空串""
	 */
	public static String getDateOfWeek(String date, int days) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date datePoint = dateFormat.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(datePoint);
			int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
			c.add(Calendar.DATE, -dayofweek + days);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(c.getTime());
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 获取日历 calendar 中的年份.
	 * @param calendar
	 *            	日历
	 * @return int 
	 * 				若calendar为空，返回当前年份
	 */
	public static int getCurYear(Calendar calendar) {
		if (calendar == null) {
			return getCurYear();
		}
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取日历 calendar 中的月份.
	 * @param calendar
	 *            	日历
	 * @return int
	 * 				月份从0开始，若calendar为空，返回当前月份
	 */
	public static int getCurMonth(Calendar calendar) {
		if (calendar == null) {
			return getCurMonth();
		}
		return calendar.get(Calendar.MONTH);
	}

	/**
	 * 获取日历 calendar 中的日期.
	 * @param calendar
	 *            日历
	 * @return int
	 * 			  若 calendar 为空，返回当前日期
	 */
	public static int getCurDay(Calendar calendar) {
		if (calendar == null) {
			return getCurDay();
		}
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 该方法逻辑有问题 获取现在时间.
	 * @return Date
	 * 				返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date getNowDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentTime = new Date();

		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTimeTwo = formatter.parse(dateString, pos);
		return currentTimeTwo;
	}

	/**
	 * 获取现在时间.
	 * @return Date
	 * 				返回时间类型 yyyy-MM-dd
	 */
	public static Date getNowDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTimeTwo = formatter.parse(dateString, pos);
		return currentTimeTwo;
	}

	/**
	 * 获取现在时间.
	 * @return String
	 * 				返回时间字符串 yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取现在时间.
	 * @return String
	 * 				返回短时间字符串格式 yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss.
	 * @return String
	 * 				返回短时间字符串格式 HH:mm:ss.
	 */
	public static String getTimeShort() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss.
	 * @param strDate
	 *            	指定的日期字符串
	 * @return Date
	 * 				返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date strToDateLong(String strDate) {
		if (strDate == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss.
	 * @param dateDate
	 *            	指定的日期
	 * @return String
	 * 				返回时间字符串 yyyy-MM-dd HH:mm:ss
	 */
	public static String dateToStrLong(java.util.Date dateDate) {
		if (dateDate == null) {
			dateDate = new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式时间转换为字符串 yyyy-MM-dd.
	 * @param dateDate
	 *            指定的日期
	 * @return String
	 * 				返回短时间字符串格式 yyyy-MM-dd
	 */
	public static String dateToStr(java.util.Date dateDate) {
		if (dateDate == null) {
			dateDate = new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式字符串转换为时间.
	 * @param strDate
	 *            	短时间字符串格式 yyyy-MM-dd
	 * @return Date
	 * 				返回时间类型 yyyy-MM-dd, 如果 strDate 为 null，返回 null
	 */
	public static Date strToDate(String strDate) {
		return strToDate(strDate, "yyyy-MM-dd");
	}

	/**
	 * 将时间字符串按指定格式转换为日期
	 * @param strDate
	 *            时间字符串
	 * @param format
	 *            日期格式
	 * @return Date
	 * 			  返回按指定格式得到的时间, 如果 strDate 为 null，返回 null
	 */
	public static Date strToDate(String strDate, String format) {
		if (strDate == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 得到现在时间.
	 * @return Date
	 * 				精确到毫秒
	 */
	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * 提取一个月中的最后一天.
	 * @param day
	 *           	毫秒数
	 * @return Date
	 * 				表示时间值的 Date。
	 */
	public static Date getLastDate(long day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(day);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime();
	}

	/**
	 * 得到现在的时间字符串.
	 * @return String
	 * 				时间字符串格式 yyyyMMdd HHmmss
	 */
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 得到现在小时.如当前时间为 14:45:55,则返回 14
	 * @return String
	 * 				当前时间的小时数
	 */
	public static String getHour() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String hour;
		hour = dateString.substring(11, 13);
		return hour;
	}

	/**
	 * 得到现在分钟..如当前时间为 14:45:55,则返回 45
	 * @return String 
	 * 				在00到59之间
	 */
	public static String getMinute() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String min;
		min = dateString.substring(14, 16);
		return min;
	}

	/**
	 * 得到现在的秒数.如当前时间为 14:45:55,则返回 55
	 * @return String 
	 * 				在00到59之间
	 */
	public static String getSecond() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String min;
		min = dateString.substring(17, 19);
		return min;
	}

	/**
	 * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写。.
	 * @param sformat
	 *            	时间格式，若为空，则默认为 yyyyMMddhhmmss
	 * @return String
	 * 				按指定格式返回的当前时间的格式
	 */
	public static String getUserDate(String sformat) {
		if (sformat == null) {
			sformat = "yyyyMMddhhmmss";
		}
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(sformat);
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 建议修改方法名 两个小时时间间的差值,必须保证两个时间都是"HH:MM"的格式，返回字符串型的小时.
	 * @param st1
	 *            时间字符串1
	 * @param st2
	 *            时间字符串2
	 * @return String
	 * 				如果 st1 表示的小时数小于或者等于 st2 表示的小时数，返回0，否则返回 st1 和 st2 表示的小时间的差值
	 */
	public static String getTwoHour(String st1, String st2) {
		String[] kk = null;
		String[] jj = null;
		kk = st1.split(":");
		jj = st2.split(":");
		if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
			return "0";
		} else {
			double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60;
			double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60;
			if ((y - u) > 0) {
				return y - u + "";
			} else {
				return "0";
			}
		}
	}

	/**
	 * 得到两个日期间的间隔天数.
	 * @param sj1
	 *            	日期字符串1
	 * @param sj2
	 *            	日期字符串2
	 * @return String
	 * 				两个日期间的间隔天数。如果解析字符串时发生异常，返回空串""
	 */
	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		long day = 0;
		try {
			java.util.Date date = myFormatter.parse(sj1);
			java.util.Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
			return day + "";
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}

	}

	/**
	 * 时间前推或后推 minute 分钟.
	 * @param strdate
	 *            	时间字符串
	 * @param minute
	 *            	前推或后推的分钟数
	 * @return String 
	 * 				如果 strdate 不能成功的转换成 Date，或 minute 不能成功的转换为 Integer, 返回空串""
	 */
	public static String getPreTime(String strdate, String minute) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String mydate1 = "";
		try {
			Date date1 = format.parse(strdate);
			long longTime = (date1.getTime() / 1000) + Integer.parseInt(minute) * 60;
			date1.setTime(longTime * 1000);
			mydate1 = format.format(date1);
			return mydate1;
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}

	}

	/**
	 * 得到一个时间延后或前移几天的时间
	 * @param nowdate
	 *            	时间字符串
	 * @param delay
	 *            	前移或后延的天数.
	 * @return String
	 * 				nowdate 延后或前移 delay 天的时间。如果解析字符串时发生异常，返回空串""
	 */
	public static String getNextDay(String nowdate, String delay) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String mdate = "";
			Date d = strToDate(nowdate);
			long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
			d.setTime(myTime * 1000);
			mdate = format.format(d);
			return mdate;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 判断是否润年.
	 * @param ddate
	 *            	时间字符串, “yyyy-MM-dd” 格式
	 * @return boolean<br>
	 * 				true: ddate 表示的年份是闰年；
	 * 				false: ddate 表示的年份不是闰年；
	 */
	public static boolean isLeapYear(String ddate) {

		/**
		 * 详细设计： 1.被400整除是闰年， 2不能被400整除，能被100整除不是闰年 3.不能被100整除，能被4整除则是闰年 4.不能被4整除不是闰年
		 */
		Date d = strToDate(ddate);
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(d);
		int year = gc.get(Calendar.YEAR);
		if ((year % 400) == 0) {
			return true;
		} else if (year % 100 == 0) {
			return false;
		} else {
			return ((year % 4) == 0);
		}
	}

	/**
	 * 返回美国时间格式 26 Apr 2006.
	 * @param str
	 *            	时间字符串
	 * @return String
	 * 				如果传入的字符串为 2011-06-23，则返回 23JUN11
	 */
	public static String getEDate(String str) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(str, pos);
		String j = strtodate.toString();
		String[] k = j.split(" ");
		return k[2] + k[1].toUpperCase() + k[5].substring(2, 4);
	}

	/**
	 * 获取一个月的最后一天(该类中的 getMonthEnd 和 getLastDayOfMonth 也是同样的意义).
	 * @param dat
	 *            	时间字符串，格式为 yyyy-MM-dd
	 * @return String
	 * 				dat 所表示的月份的最后一天，如 dat="2011-05-04",则返回 "2011-05-31"
	 * @see DateUtilsBasic#getMonthEnd(String)
	 * @see DateUtilsBasic#getLastDayOfMonth(String)
	 */
	public static String getEndDateOfMonth(String dat) {
		String str = dat.substring(0, 8);
		String month = dat.substring(5, 7);
		int mon = Integer.parseInt(month);
		if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8 || mon == 10 || mon == 12) {
			str += "31";
		} else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
			str += "30";
		} else {
			if (isLeapYear(dat)) {
				str += "29";
			} else {
				str += "28";
			}
		}
		return str;
	}

	/**
	 * 判断两个日期是否在同一个周.
	 * @param date1
	 *            	日期1
	 * @param date2
	 *            	日期2
	 * @return boolean<br>
	 * 				true: 两个日期在同一周;false: 两个日期不在同一周;</br>
	 * 				注：星期天是一周的第一天，星期六是一周的最后一天.
	 */
	public static boolean isSameWeekDates(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		if (0 == subYear) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
			// 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 产生周序列,即得到当前时间所在的年度是第几周.
	 * @return String
	 * 				格式为：年份 + 周的序号
	 */
	public static String getSeqWeek() {
		Calendar c = Calendar.getInstance(Locale.CHINA);
		String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
		if (week.length() == 1) {
			week = "0" + week;
		}
		String year = Integer.toString(c.get(Calendar.YEAR));
		return year + week;
	}

	/**
	 * 建议修改方法名 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号.
	 * @param sdate
	 *            日期字符串
	 * @param num
	 *            取"0"到“6”之间的字符串，"0"代表星期天，星期天是一周的第一天
	 * @return String 
	 * 			  以 yyyy-MM-dd 模式， 如果 sdate 不能成功的转换为 Date 或 num 不在"0"到"6"之间，返回空串""
	 */
	public static String getWeek(String sdate, String num) {
		//先转换为时间
		Date dd = DateUtilsBasic.strToDate(sdate);
		if (dd == null) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		c.setTime(dd);
		if (num.equals("1")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else if (num.equals("2")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		} else if (num.equals("3")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		} else if (num.equals("4")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		} else if (num.equals("5")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		} else if (num.equals("6")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		} else if (num.equals("0")) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		} else {
			return "";
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}

	/**
	 * 返回日期所处的星期.
	 * @param sdate
	 *            	日期字符串
	 * @return String 
	 * 				完整的英语星期(Sunday、Monday、Tuesday、Wednesday、Thursday、Friday  和 Saturday)，如果 sdate 不能转换为 Date，返回空串""。
	 */
	public static String getWeek(String sdate) {
		// 先转换为时间
		Date date = DateUtilsBasic.strToDate(sdate);
		if (date == null) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return new SimpleDateFormat("EEEE").format(c.getTime());
	}

	/**
	 * 返回日期所代表的星期.
	 * @param sdate
	 *            	日期字符串
	 * @return String 
	 * 				完整的中文星期("星期日"、"星期一"、"星期二"、"星期三"、"星期四"、"星期五"  和 "星期六")，如果sdate不能成功的转换为Date，返回空串""
	 */
	public static String getWeekStr(String sdate) {
		String result = "";
		Date date = strToDate(sdate);
		if (date == null) {
			return result;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		int day = c.get(Calendar.DAY_OF_WEEK);
		switch (day) {
		case Calendar.SUNDAY:
			return "星期日";
		case Calendar.MONDAY:
			return "星期一";
		case Calendar.TUESDAY:
			return "星期二";
		case Calendar.WEDNESDAY:
			return "星期三";
		case Calendar.THURSDAY:
			return "星期四";
		case Calendar.FRIDAY:
			return "星期五";
		case Calendar.SATURDAY:
			return "星期六";
		default:
			break;
		}
		return "";

	}

	/**
	 * 两个日期之间的天数.
	 * @param end
	 *            	日期字符串1，以 yyyy-MM-dd 格式
	 * @param begin
	 *            	日期字符串2，以 yyyy-MM-dd 格式
	 * @param defaultValue
	 *            	解析过程中发生异常所返回的值
	 * @return long 
	 * 				返回 begin 和 end 所表示的日期之间的天数；如果 end，begin 不能成功的转换成 Date，返回 defaultValue
	 */
	public static long getDays(String end, String begin, long defaultValue) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date endDate = null;
		java.util.Date beginDate = null;
		try {
			endDate = format.parse(end);
			beginDate = format.parse(begin);
			long day = (endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000);
			return day + 1;
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return defaultValue;
		}

	}

	/**
	 * 该方法返回 sdate 所表示日期所在月份的第一天所属星期的星期日（星期日是一周的第一天）的日期，如 sdate="2011-04-15",则返回"2011-03-27".
	 * @param sdate
	 *            日期字符串
	 * @return String 
	 * 				以 yyyy-MM-dd 格式，如果sdate不能成功的转换为Date，返回空串""
	 */
	public static String getNowMonth(String sdate) {
//		该方法返回sdate所表示日期所在月的第一天，所属星期的星期日（星期日是一周的第一天）; 形成如下的日历，
//		根据传入的一个时间返回一个结构 星期日 星期一 星期二 星期三 星期四 星期五
//		星期六 下面是当月的各个时间 此函数返回该日历第一行星期日所在的日期.
		// 取该时间所在月的一号
		String monthBegin = getMonthBegin(sdate);
		String weekBegin = getWeek(monthBegin, "0");
		return weekBegin;
	}

	/**
	 * 取得数据库主键 生成格式为 yyyymmddhhmmss+k 位随机数.
	 * @param k
	 *            表示是取几位随机数，可以自己定
	 * @return String
	 * 				随机数
	 */

	public static String getNo(int k) {
		return getUserDate("yyyyMMddhhmmss") + getRandom(k);
	}

	/**
	 * 返回一个随机数.
	 * @param i
	 *            随机数的位数
	 * @return String
	 * 				随机数
	 */
	public static String getRandom(int i) {
		Random jjj = new Random();
		if (i == 0) {
			return "";
		}
		String jj = "";
		for (int k = 0; k < i; k++) {
			jj = jj + jjj.nextInt(9);
		}
		return jj;
	}

	/**
	 * 判断 date 是否可以成功转换为日期.
	 * @param date
	 *            日期字符串
	 * @return 	boolean
	 * 				true: date 可以转换为日期，false: date 不可以转换为日期
	 */
	public static boolean rightDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (date == null) {
			return false;
		}
		if (date.length() > 10) {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} else {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		}
		try {
			sdf.parse(date);
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/**
	 * 判断 datestr 所表示的日期是否是周末.
	 * @param datestr
	 *            	日期字符串
	 * @return boolean
	 * 				true: datestr 所表示的日期是周末；false: datestr 所表示的日期不是周末
	 */
	public static boolean isWeekend(String datestr) {
		if (datestr == null || "".equals(datestr.trim())) {
			return false;
		}
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = myFormatter.parse(datestr);
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
		return false;
	}

	/**
	 * 返回两个日期间的天数，去除周末的天数.
	 * @param startdate
	 *            	开始日期，格式 “yyyy-MM-dd”
	 * @param enddate
	 *            	结束日期，格式 “yyyy-MM-dd”
	 * @return int
	 * 				两个日期间去除周末的天数，如果 startdate 和 enddate 其中有一个是空或者解析过程中发生异常，返回 0
	 */
	public static int getdaysslice(String startdate, String enddate) {
		if (startdate == null || "".equals(startdate.trim())) {
			return 0;
		}
		if (enddate == null || "".equals(enddate.trim())) {
			return 0;
		}
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date start = null;
		java.util.Date end = null;
		try {
			start = myFormatter.parse(startdate);
			end = myFormatter.parse(enddate);
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return 0;
		}
		long daynum = (end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000);
		Calendar startcalendar = new GregorianCalendar();
		startcalendar.setTime(start);
		int n = 0;
		for (int i = 0; i <= daynum; i++) {
			if (startcalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| startcalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				startcalendar.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			startcalendar.add(Calendar.DAY_OF_MONTH, 1);
			n++;
		}
		return n;
	}

	/**
	 * 返回按给定连接符的日期字符串.
	 * @param date
	 *            需要转换的字符串，前 8 位需以 yyyyMMdd 格式，separator 日期格式中连接的字符 例如：20071227——>2007-12-27
	 * @param separator
	 *            连接符
	 * @return String
	 * 			  按给定连接符连接的日期字符串。如果 date 或者 separator 为空，返回date；如果 date 的长度小于8位，返回 date；
	 * 			  如果 date 的长度大于或等于8位，但其5、6位不在 01~12 范围内或者7、8位不在 01~31范围内，则同样返回 date；
	 * 			  例如 date="1234567"||"12341301"||"12340132"时，直接将 date 返回
	 */
	public static String formatToYYYYMMDD(String date, String separator) {
		if (null == date || "".equals(date) || null == separator || "".equals(separator)) {
			return date;
		}
		StringBuffer sb = new StringBuffer();
		String reg = "[0-9]{4}(01|02|03|04|05|06|07|08|09|10|11|12){1}(01|02|03|04|05|06|07|08|09|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31){1}";

		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(date);
		int offset = 0;
		while (m.find(offset)) {
			int sIndex = m.start();
			int eIndex = m.end();

			String tmpPrefix = date.substring(offset, sIndex);
			sb.append(tmpPrefix);

			String tmpDate = date.substring(sIndex, eIndex);

			String tmpYYYY = tmpDate.substring(0, 4);
			String tmpMM = tmpDate.substring(4, 6);
			String tmpDD = tmpDate.substring(6, 8);

			sb.append(tmpYYYY);
			sb.append(separator);
			sb.append(tmpMM);
			sb.append(separator);
			sb.append(tmpDD);
			offset = m.end();
		}

		String tmpSuffix = date.substring(offset, date.length());
		sb.append(tmpSuffix);
		return sb.toString();
	}

	/**
	 * 判断 day 是否在 curTime 之后，且不超过一天.
	 * @param curTime
	 *            时间毫秒数
	 * @param day
	 *            时间毫秒数
	 * @return boolean
	 * 			  如果 day 在 curTime 之后，且不超过一天，返回true.
	 */
	public static boolean inThisDay(long curTime, long day) {
		if (curTime >= day && curTime < day + 24 * 60 * 60 * 1000) {
			return true;
		}
		return false;
	}

	/**
	 * 将当前日期转换为 format 格式的字符串.
	 * @param format
	 *            	日期格式
	 * @return String
	 * 				format 格式的当前日期字符串
	 */
	public static String getCurrentDateFormated(String format) {
		Date date = new Date();
		return changedDateFormat(date, format);
	}

	/**
	 * 将日期 date 转换为 format 格式的字符串.
	 * @param date
	 *            日期
	 * @param format
	 *            日期格式
	 * @return String 
	 * 			  format 格式的日期字符串
	 */
	public static String changedDateFormat(Date date, String format) {
		try {
			if (date == null) {
				return "";
			}
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			String dateString = formatter.format(date);
			return dateString;
		} catch (Exception e) {
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 获取从 start 到 end 的时间范围内每间隔 pitch 分钟的时间字符串集合. 
	 * 如 getTimeList("2011-05-04 13:00:00", "2011-05-04 13:30:00", 5, "yyyy-MM-dd hh:mm:ss")，
	 * 则返回的 List 中存放的元素为：2011-05-04 01:00:00, 2011-05-04 01:05:00, 2011-05-04 01:10:00, 
	 * 2011-05-04 01:15:00, 2011-05-04 01:20:00, 2011-05-04 01:25:00, 2011-05-04 01:30:00。
	 * 注意小时的范围为 01~12
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param pitch
	 *            分钟间隔
	 * @param format
	 *            时间格式
	 * @return List&lt;String&gt;
	 * 			  从 start 到 end 的时间范围内每间隔 pitch 分钟的时间字符串集合.如果 start 与 end 所表示的时间相同，则 List 中只有 start 一个元素
	 * 				如果解析过程中出现异常，则 List 中无元素，size=0;
	 */
	public static List<String> getTimeList(String start, String end, int pitch, String format) {
		List<String> result = new ArrayList<String>();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			Date startDate = formatter.parse(start);
			Date endDate = formatter.parse(end);
			Calendar startCalendar = Calendar.getInstance();
			Calendar endCalendar = Calendar.getInstance();
			startCalendar.setTime(startDate);
			endCalendar.setTime(endDate);

			while (startCalendar.before(endCalendar)) {
				result.add(formatter.format(startCalendar.getTime()));
				startCalendar.add(Calendar.MINUTE, pitch);
			}
			if (startCalendar.equals(endCalendar)) {
				result.add(formatter.format(startCalendar.getTime()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取从 start 到 end 的时间范围内每间隔 pitch 分钟的时间字符串集合. 
	 * 如 getTimeList("01:00", "01:30", 5)，
	 * 则返回的 List 中存放的元素为：01:00, 01:05, 01:10, 01:15, 01:20, 01:25。
	 * 注意小时的范围为 00~23
	 * @param start
	 *            开始时间，格式为 HH:mm
	 * @param end
	 *            结束时间，格式为 HH:mm
	 * @param pitch
	 *            分钟间隔
	 * @return List&lt;String&gt;
	 * 			  从 start 到 end 的时间范围内每间隔 pitch 分钟的时间字符串集合.如果 start 与 end 所表示的时间相同或者解析过程中出现异常，则 List 中无元素，size=0;
	 */
	public static List<String> getTimeList(String start, String end, int pitch) {
		List<String> result = new ArrayList<String>();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		try {
			Date startDate = formatter.parse(start);
			Date endDate = formatter.parse(end);
			Calendar startCalendar = Calendar.getInstance();
			Calendar endCalendar = Calendar.getInstance();
			startCalendar.setTime(startDate);
			endCalendar.setTime(endDate);

			while (startCalendar.before(endCalendar)) {
				result.add(formatter.format(startCalendar.getTime()));
				startCalendar.add(Calendar.MINUTE, pitch);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取 sdate 所表示的日期对应的星期的序号(1表示星期天，2表示星期一，以此类推).
	 * @param sdate
	 *            	日期字符串
	 * @return int
	 * 				星期的序号
	 */
	public static int getWeekNumber(String sdate) {
		Date date = strToDate(sdate);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 根据出生日期计算年龄.
	 * @param strBirthDay
	 *            出生日期字符串
	 * @param format
	 *            日期格式
	 * @return int
	 * 			  年龄
	 * @throws Exception
	 *             the exception
	 */
	public static int getAge(String strBirthDay, String format) throws Exception {
		DateFormat df = new SimpleDateFormat(format);
		Date birthDay = df.parse(strBirthDay);
		return getAge(birthDay);
	}

	/**
	 * 取日期字符串的前16位，即 yyyy-MM-dd hh:mm
	 * 例如：date="2010-01-20 13:38:10.0"，返回 "2010-01-20 13:38"
	 * @param date
	 * 				日期字符串
	 * @return String
	 * 				date 的前 16 位字符串，如果 date 的位数小于 16 位，则返回 date，如果 date 为空，返回空串""
	 */
	public static String getDateHHMM(String date) {
		if (date == null || "".equals(date.trim())) {
			return "";
		}
		if (date.length() >= 16) {
			date = date.substring(0, 16);
		}
		return date;
	}
	
	/**
	 * 取日期字符串的前10位，即 yyyy-MM-dd
	 * 例如：date="2010-01-20 13:38:10.0"，返回 "2010-01-20"
	 * @param date
	 * 				日期字符串
	 * @return String
	 * 				date 的前 10 位字符串，如果 date 的位数小于 10 位，则返回 date，如果 date 为空，返回空串""
	 */
	public static String getDateYYMMDD(String date) {
		if (date == null || "".equals(date.trim())) {
			return "";
		}
		if (date.length() >= 10) {
			date = date.substring(0, 10);
		}
		return date;
	}

	/**
	 * 根据出生日期计算年龄.
	 * @param birthDay
	 *            出生日期
	 * @return int
	 * 			  年龄.
	 * @throws Exception
	 *            如果出生日期在当前日期之后，抛出异常
	 */
	public static int getAge(Date birthDay) throws Exception {
		Calendar cal = Calendar.getInstance();

		if (cal.before(birthDay)) {
			throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
		}

		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH);
		int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
		cal.setTime(birthDay);

		int yearBirth = cal.get(Calendar.YEAR);
		int monthBirth = cal.get(Calendar.MONTH);
		int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

		int age = yearNow - yearBirth;
		if (monthNow <= monthBirth) {
			if (monthNow == monthBirth) {
				if (dayOfMonthNow < dayOfMonthBirth) {
					age--;
				} 
			} else {
				age--;
			}
		} 
		return age;
	}

	/**
	 * 查看当前时间与指定时间的差值是否是间隔时间段的整数倍
	 * @param dateStr 
	 * 			指定时间字符串
	 * @param period 
	 * 			间隔时间段（天）
	 * @return boolean
	 * 			是否为整数倍
	 */
	public static boolean isEqualPeriodDate(String dateStr, long period) {
		if (StringUtilsBasic.checkNull(dateStr)) {
			String format = "yyyy-MM-dd";
			String now = DateUtilsBasic.getCurDate(format);
			dateStr = DateUtilsBasic.getDateYYMMDD(dateStr);
			return DateUtilsBasic.getDays(now, dateStr, 0) % period == 0;
		}
		return false;
	}
}
