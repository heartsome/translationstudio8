package net.heartsome.cat.ts.test.demos;

import net.heartsome.test.swtbot.finders.HsSWTWorkbenchBot;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class CursorPosition {
	private HsSWTWorkbenchBot bot;

	@Before
	public void setUp() {
//		SWTBotPreferences.PLAYBACK_DELAY = 500;
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = new HsSWTWorkbenchBot();
	}
/*
	@Ignore
	@Test
	public void SetCursorPositionInSourceCell() {
		int rowPosition = 2;
		int columnPosition = 1;
		int inLine = 0;
		int inColumn = 2;
		String text = "segment";
		
		NatTable nt = (NatTable) bot.widget(widgetOfType(NatTable.class));
		SWTBotNatTable sbnt = new SWTBotNatTable(nt);
		sbnt.click(rowPosition, columnPosition);
//		Screen s = new Screen();
//		try {
//			s.type(null, "\n", 0);
//		} catch (FindFailed e) {
//			e.printStackTrace();
//		}
		System.out.println("Before " + bot.styledText().cursorPosition());
		net.heartsome.test.swtbot.widgets.SWTBotStyledText srcText = bot.styledText();
		srcText.navigateTo(inLine, inColumn);
		System.out.println("After " + bot.styledText().cursorPosition());
		int textIndex = srcText.indexOf(text);
		System.out.println("Index: " + textIndex);
		System.out.println("Position: " + srcText.getPositionByIndex(textIndex));
		
		Pattern inlineElements = Pattern.compile("(\u00A0\\d+)?\u00A0(x|bx|ex|g|bpt|ept|mrk|sub|ph|it)\u00A0(\\d+\u00A0)?");
		Matcher m = inlineElements.matcher(bot.styledText().getText());
		int i = 0;
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			System.out.println("Found " + ++i + ": " + start + " ~ " + end);
		}
		
	}*/
	
	@Test
	public void GetViews() {
		System.out.println("Views: " + bot.views());
	}

	@After
	public void tearDown() {
		bot.sleep(500);
	}
}
