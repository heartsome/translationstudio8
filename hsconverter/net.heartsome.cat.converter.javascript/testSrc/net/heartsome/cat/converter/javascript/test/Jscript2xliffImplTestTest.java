package net.heartsome.cat.converter.javascript.test;

import java.util.Hashtable;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Jscript2xliffImplTestTest {
	Hashtable<String, String> params;

	@Before
	public void setUp() throws Exception {
		params = new Hashtable<String, String>();
		params.put("srcLang", "en");
		params.put("skeleton", "test");
		params.put("srcEncoding", "utf-8");
	}

	// 单行注释前不存在有效代码
	@Test
	public void testRunWithSingleComment_1() {
		params.put("source", "//alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 单行注释前存在有效代码
	@Test
	public void testRunWithSingleComment_2() {
		params.put("source",
				"alert(\"Error Handled\") //alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单行注释前存在可翻译的单行注释符
	@Test
	public void testRunWithSingleComment_3() {
		params.put("source",
				"alert(\"Error // Handled\"); //alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单行注释和多行注释字符在同一行
	@Test
	public void testRunWithSingleComment_4() {
		params.put("source",
				"alert(\"Error // Handled\"); //*/alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 多行注释前和后不存在有效代码
	@Test
	public void testRunWithMultiRowComment_1() {
		params.put("source", "/*\nalert(\"Error Handled\")\n*/");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 多行注释前和后存在有效代码
	@Test
	public void testRunWithMultiRowComment_2() {
		params
				.put(
						"source",
						"alert(\"Error Handled\");/*\nalert(\"Error Handled\");\n*/alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(2, Long.parseLong(result.firstElement()));
	}

	// 多行注释前的有效代码存在注释字符
	@Test
	public void testRunWithMultiRowComment_3() {
		params
				.put(
						"source",
						"alert(\"Error // /* */Handled\");/*\nalert(\"Error Handled\")\n*/alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(2, Long.parseLong(result.firstElement()));
	}

	// 多行注释和单行注释在同一行，且多行注释在前
	@Test
	public void testRunWithMultiRowComment_4() {
		params
				.put(
						"source",
						"alert(\"Error Handled\"); /* Error Handled */ alert(\"Error Handled\"); //alert(\"Error Handled\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(2, Long.parseLong(result.firstElement()));
	}

	// 多行注释中嵌套了多行注释的开始符
	@Test
	public void testRunWithMultiRowComment_5() {
		params.put("source",
				"alert(\"Error Handled\"); /* Error Handled // /* */");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 多行注释开始符后紧接反斜杠，混淆多行注释的开始和结束
	@Test
	public void testRunWithMultiRowComment_6() {
		params.put("source",
				"alert(\"Error Handled\"); /*/ Error Handled // /* */");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 多行注释没有结束符
	@Test
	public void testRunWithMultiRowComment_7＿WithoutClosed() {
		params.put("source",
				"alert(\"Error Handled\"); /*/ Error Handled // /* ");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// “纯”双引号
	@Test
	public void testRunWithQuote_1() {
		params.put("source", "alert(\"Error Handled\");");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// “纯”单引号
	@Test
	public void testRunWithQuote_2() {
		params.put("source", "alert('Error Handled');");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 双引号中嵌单引号
	@Test
	public void testRunWithQuote_3() {
		params.put("source", "alert(\"Error 'abc' Handled\");");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单引号中嵌双引号
	@Test
	public void testRunWithQuote_4() {
		params.put("source", "alert('Error \"abc\" Handled')");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 双引号中嵌转义的双引号
	@Test
	public void testRunWithQuote_5() {
		params.put("source", "alert(\"Error \\\"abc\\\" Handled\");");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单引号中嵌转义的单引号
	@Test
	public void testRunWithQuote_6() {
		params.put("source", "alert('Error \\\'abc\\\' Handled')");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 双引号中嵌单引号及转义的双引号
	@Test
	public void testRunWithQuote_7() {
		params.put("source", "alert(\"Error 'abc' \\\"abc\\\" Handled\");");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 双引号中嵌单引号，且双引号之前存在被转义的单引号
	@Test
	public void testRunWithQuote_8() {
		params.put("source", "ale\\'test\\'rt(\"Error 'abc' Handled\");");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单引号中嵌入转义的单引号和双引号
	@Test
	public void testRunWithQuote_9() {
		params.put("source", "alert('Error \"abc\" \\\'abc\\\' Handled')");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 双引号中包含转义的反斜杠
	@Test
	public void testRunWithQuote_10() {
		params.put("source", "alert(\"\\\\\")");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单引号中包含转义的反斜杠
	@Test
	public void testRunWithQuote_11() {
		params.put("source", "alert('\\\\')");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 单引号中结束
	@Test
	public void testRunWithQuote_12() {
		params.put("source", "var type='abc'");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 无引号
	@Test
	public void testRunWithoutQuote() {
		params.put("source", "alert()");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 引号内的字符串跨行
	@Test
	public void testRunWithNewline() {
		params.put("source", "alert('Error \\\nHandled') //sdfldsjsdklj");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 引号内的字符串跨行但没有"\"标识
	@Test
	public void testRunWithNewlineWithoutClosed() {
		params.put("source", "alert('Error \nHandled') //sdfldsjsdklj");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 避免错误的字符串导致死循环的情况
	@Test
	public void testRunWithBadString＿1() {
		params.put("source", "\\\'quote 'test' ok?");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 测试人员在测试时发现的问题
	@Test
	public void testRunWithBadString＿2() {
		params
				.put(
						"source",
						"addPreprocessHandler( 'designMode != \"On\"', 'designMode != \"on\"', true, function(t){indexOf.call=call;return indexOf.call(t.text, 'kevinroth.com')>-1;} );");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(3, Long.parseLong(result.firstElement()));
	}

	// 测试人员在测试时发现的问题:正则表达式中存有奇数个引号的情况
	@Test
	public void testRunWithBadString＿3() {
		params.put("source",
				"e.element.text.match(/minorVersion\\s*:\\s*'(0|X)/)");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 测试人员在测试时发现的问题:正则表达式中存有注释字符，如 //
	@Test
	public void testRunWithBadString＿4() {
		params.put("source", "actionSubstring.replace(/\\//g,'%252F');");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

	// 测试人员在测试时发现的问题:区分正斜杠为正则表达式的开始和除号
	@Test
	public void testRunWithSlash_1() {
		params.put("source", "var type=100/10");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(0, Long.parseLong(result.firstElement()));
	}

	// 测试人员在测试时发现的问题:区分正斜杠为正则表达式的开始和除号
	@Test
	public void testRunWithSlash_2() {
		params
				.put(
						"source",
						"var type=100     /*tes\nt*/\n/10;/*multi comment*/var type2=20\n/2;\nalert(type)\nvar type=/a'[\\\n/]\\\n/\nalert(type+'abc')");
		Vector<String> result = Jscript2xliffImplTest.run(params);
		Assert.assertEquals(1, Long.parseLong(result.firstElement()));
	}

}
