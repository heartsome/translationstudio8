package net.heartsome.license.utils;

public class RandomUtils {
	
	public static String generateRandom(int num) {
		char[] temp = new char[] {'0','1','2','3','4','5','6','7','8','9',
				'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o',
				'p','q','r','s','t','u','v','w','x','y','z','A','B','C','D',
				'E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S',
				'T','U','V','W','X','Y','Z'};
		int size = temp.length;
		StringBuffer bu = new StringBuffer();
		for (int i = 0; i < num; i++) {
			int r = (int)(Math.random() * size);
			bu.append(temp[r]);
		}
		
		return bu.toString();
	}
}
