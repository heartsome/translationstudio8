package net.heartsome.cat.converter.javascript.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import net.heartsome.cat.converter.javascript.Jscript2xliffAbstract;

public class Jscript2xliffImplTest extends Jscript2xliffAbstract {
	public Jscript2xliffImplTest(Map<String, String> params) {
		inputFile = params.get("source"); //$NON-NLS-1$
		sourceLanguage = params.get("srcLang"); //$NON-NLS-1$
		skeletonFile = params.get("skeleton"); //$NON-NLS-1$
		// fixed a bug 1293 by john.
		encoding = params.get("srcEncoding"); //$NON-NLS-1$
		input = new StringReader(inputFile);
		buffer = new BufferedReader(input);

		output = new ByteArrayOutputStream();

		skeleton = new ByteArrayOutputStream();
	}

	public static Vector<String> run(Hashtable<String, String> params) {
		System.out.println("##############################################");
		Vector<String> result = new Vector<String>();
		Jscript2xliffAbstract js2Xliff = new Jscript2xliffImplTest(params);
		try {
			js2Xliff.run(null);
		} catch (Exception e) {
			System.out.println("??????????????????????????????????????????");
			System.out.println("error:转换文件失败。");
			System.out.println("??????????????????????????????????????????");
			e.printStackTrace();
		}
		System.out.println("-----------------------------------------------");
		System.out.println(js2Xliff.output.toString());
		System.out.println("-----------------------------------------------");
		System.out.println(js2Xliff.skeleton.toString());
		System.out.println("-----------------------------------------------");
		result.add(String.valueOf(js2Xliff.segId));
		System.out.println("##############################################");
		return result;
	}
}
