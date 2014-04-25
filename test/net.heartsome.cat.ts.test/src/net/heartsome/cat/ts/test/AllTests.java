package net.heartsome.cat.ts.test;

import junit.framework.TestSuite;
import net.heartsome.cat.ts.test.menu.db.DatabaseTest;
import net.heartsome.cat.ts.test.menu.db.ImportToDBTest;
import net.heartsome.cat.ts.test.menu.db.MemoryDatabaseTest;
import net.heartsome.cat.ts.test.menu.file.ProjectOperationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 包含 HSCAT8 TS 所有测试用例的测试套件。
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
@RunWith(Suite.class)
@SuiteClasses({
//	ProjectOperationTest.class,
//	DatabaseTest.class,
	MemoryDatabaseTest.class,
	ImportToDBTest.class })
public class AllTests extends TestSuite {
	
}
