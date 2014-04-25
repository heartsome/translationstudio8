package net.heartsome.cat.ts.test.demos;

import static org.junit.Assert.assertTrue;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class MenuTest {

	private static SWTWorkbenchBot bot;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
	}

	@Test
	public void validateFileMenuStartsEnabled() {
		assertTrue(bot.menu("File").isEnabled());
	}
}
