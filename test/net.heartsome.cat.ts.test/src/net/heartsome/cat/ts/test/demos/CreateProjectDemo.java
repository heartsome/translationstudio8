package net.heartsome.cat.ts.test.demos;

import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBotAssert;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class CreateProjectDemo {

	private SWTWorkbenchBot bot;
	
	@Before
	public void setup() {
//		SWTBotPreferences.PLAYBACK_DELAY = 500;
		bot = new SWTWorkbenchBot();
	}
	
	@Test
	public void createProject() throws Exception {
		String projectName = "prjBot-001";
		
		bot.menu("File").menu("New").click();
		SWTBotShell shell = bot.shell("New");
		shell.activate();
		// From menu open File > New dialog, verify whether the dialog has been opened.
		
		bot.tree().select("Project");
		SWTBotAssert.assertEnabled(bot.button("Next >"));
		// After selecting Project, the Next button should be enabled.
		
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		SWTBotAssert.assertEnabled(bot.button("Finish"));
		// Enter the Project Name, then Finish button should be enabled.
		
		bot.button("Finish").click();
		SWTBotAssert.assertVisible(bot.tree().select(projectName));
		// Click Finish button and verify whether the project's been successfully created.
	}

	@After
	public void teardown() {
		bot.sleep(1000);
	}
}
