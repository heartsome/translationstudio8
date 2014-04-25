package net.heartsome.cat.ts.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TSTest {

	private static SWTWorkbenchBot bot;
	
	@BeforeClass
	public static void setup() {
		bot = new SWTWorkbenchBot();
	}
	
	@Test
	public void FirstTest() {
		bot.menu("File").isVisible();
	}
	
	@Ignore
	@Test
	public void SecondTest() {
		assertTrue(false);
	}
	
	@Test
	public void ThirdTest() {
		assertTrue(true);
	}
}
