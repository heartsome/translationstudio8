   -libraryjars "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/rt.jar"
   -printmapping "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/build/obfuscate.map"
   -applymapping "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/config/hsts8/obfuscate_v8.2.5.map"
-dontshrink
-dontoptimize
-defaultpackage obClasses
-allowaccessmodification
-useuniqueclassmembernames
-dontusemixedcaseclassnames
-keeppackagenames

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keep public interface * extends com.sun.jna.Library {*;}

-keep class **.Messages
-keep class org.eclipse.ui.internal.navigator.resources.resource.WorkbenchNavigatorMessages {
	public <fields>;
}

# ??¨ä??å­???¾å??ä¸????ä»¶ç??å¯¼å?ºå?????ç±»å??ï¼?public?????¹æ??????????? ################################################
#################################################################################################
## net.heartsome.cat.common.core
-keep class net.heartsome.cat.common.bean.*, net.heartsome.cat.common.core.*, 
net.heartsome.cat.common.core.exception.*, net.heartsome.cat.common.file.*, 
net.heartsome.cat.common.innertag.*, net.heartsome.cat.common.innertag.factory.*, 
net.heartsome.cat.common.locale.*, net.heartsome.cat.common.operator.*,
net.heartsome.cat.common.resources.*, net.heartsome.cat.common.tm.*, 
net.heartsome.cat.common.util.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.common.ui
-keep class net.heartsome.cat.common.ui.*, net.heartsome.cat.common.ui.dialog.*,
net.heartsome.cat.common.ui.handlers.*, net.heartsome.cat.common.ui.innertag.*,
net.heartsome.cat.common.ui.listener.*, net.heartsome.cat.common.ui.utils.*,
net.heartsome.cat.common.ui.wizard.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.common.ui.navigator.resources
-keep class org.eclipse.ui.internal.navigator.resources.*, org.eclipse.ui.internal.navigator.resources.actions.*,
org.eclipse.ui.internal.navigator.resources.plugin.*, org.eclipse.ui.internal.navigator.resources.workbench.*,
org.eclipse.ui.internal.navigator.workingsets.*, org.eclipse.ui.navigator.resources.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.help
-keep class net.heartsome.cat.ts.help.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.xml
-keep class net.heartsome.xml.vtdimpl.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.convert.ui.example
-keep class net.heartsome.cat.convert.ui.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter
-keep class net.heartsome.cat.converter.*, net.heartsome.cat.converter.util.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.msoffice2007
-keep class net.heartsome.cat.converter.msoffice2007.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.openoffice
-keep class net.heartsome.cat.converter.openoffice.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.rtf
-keep class net.heartsome.cat.converter.rtf.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.ui
-keep class net.heartsome.cat.convert.extenstion.*, net.heartsome.cat.convert.ui.command.*,
net.heartsome.cat.convert.ui.model.*, net.heartsome.cat.convert.ui.utils.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.word2007
-keep class net.heartsome.cat.converter.word2007.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.converter.xml
-keep class net.heartsome.cat.converter.xml.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database
-keep class net.heartsome.cat.database.*, net.heartsome.cat.database.bean.*,
net.heartsome.cat.database.service.*, net.heartsome.cat.database.tmx.*,
net.heartsome.cat.document.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database.ui
-keep class net.heartsome.cat.database.ui.bean.*, net.heartsome.cat.database.ui.core.*,
net.heartsome.cat.database.ui.dialog.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database.ui.tb
-keep class net.heartsome.cat.database.ui.tb.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database.ui.tm
-keep class net.heartsome.cat.database.ui.tm.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.p2update
-keep class net.heartsome.cat.p2update.autoupdate.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts
-keep class net.heartsome.cat.ts.util.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.core
-keep class net.heartsome.cat.ts.core.*,net.heartsome.cat.ts.core.bean.*,
net.heartsome.cat.ts.core.file.*, net.heartsome.cat.ts.core.qa.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.tb
-keep class net.heartsome.cat.ts.tb.importer.*, net.heartsome.cat.ts.tb.importer.extension.*, 
net.heartsome.cat.ts.tb.match.*, net.heartsome.cat.ts.tb.match.extension.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.tm
-keep class net.heartsome.cat.ts.tm.bean.*, net.heartsome.cat.ts.tm.complexMatch.*,
net.heartsome.cat.ts.tm.importer.*, net.heartsome.cat.ts.tm.importer.extension.*,
net.heartsome.cat.ts.tm.match.*, net.heartsome.cat.ts.tm.match.extension.*,
net.heartsome.cat.ts.tm.simpleMatch.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui
-keep class de.jaret.util.misc.*, net.heartsome.cat.ts.ui.*,
net.heartsome.cat.ts.ui.bean.*, net.heartsome.cat.ts.ui.composite.*,
net.heartsome.cat.ts.ui.dialog.*, net.heartsome.cat.ts.ui.editors.*,
net.heartsome.cat.ts.ui.extensionpoint.*, net.heartsome.cat.ts.ui.grid.*,
net.heartsome.cat.ts.ui.innertag.*, net.heartsome.cat.ts.ui.innertag.tagstyle.*,
net.heartsome.cat.ts.ui.jaret.renderer.*, net.heartsome.cat.ts.ui.preferencepage.*,
net.heartsome.cat.ts.ui.preferencepage.colors.*, net.heartsome.cat.ts.ui.preferencepage.dictionaries.*,
net.heartsome.cat.ts.ui.preferencepage.languagecode.*, net.heartsome.cat.ts.ui.preferencepage.translation.*,
net.heartsome.cat.ts.ui.resource.*, net.heartsome.cat.ts.ui.tagstyle.*, 
net.heartsome.cat.ts.ui.util.*, net.heartsome.cat.ts.ui.view.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui.plugin
-keep class net.heartsome.cat.ts.ui.plugin.*, net.heartsome.cat.ts.ui.plugin.bean.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui.qa
-keep class net.heartsome.cat.ts.ui.qa.*, net.heartsome.cat.ts.ui.qa.fileAnalysis.*,
net.heartsome.cat.ts.ui.qa.preference.*, net.heartsome.cat.ts.ui.qa.views.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui.term
-keep class net.heartsome.cat.ts.ui.term.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui.translation
-keep class net.heartsome.cat.ts.ui.translation.*, net.heartsome.cat.ts.ui.translation.bean.*,
net.heartsome.cat.ts.ui.translation.view.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.ts.ui.xliffeditor.nattable
-keep class net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.*, net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.*,
net.heartsome.cat.ts.ui.xliffeditor.nattable.qa.*, net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.* 
{
	public <fields>;
	public <methods>;
}

#################################################################################################
##########################################################################################




##############å¦??????????å¹³å?°ä½¿??¨ç??ä¸????hudson?????????å¹³å?°ï????£ä??ä½¿ç??eclipse export???è¦?æ·»å??####################
#-keep public class * implements org.osgi.framework.BundleActivator {*;}
#-keep class net.heartsome.cat.ts.Application
###########################################################################################

-keep class net.heartsome.cat.ts.ui.qa.TermConsistenceQA
-keep class net.heartsome.cat.ts.ui.qa.ParagraphConsistenceQA
-keep class net.heartsome.cat.ts.ui.qa.NumberConsistenceQA
-keep class net.heartsome.cat.ts.ui.qa.TagConsistenceQA
-keep class net.heartsome.cat.ts.ui.qa.NonTranslationQA
-keep class net.heartsome.cat.ts.ui.qa.SpaceOfParaCheckQA
-keep class net.heartsome.cat.ts.ui.qa.ParaCompletenessQA
-keep class net.heartsome.cat.ts.ui.qa.TgtTextLengthLimitQA
-keep class net.heartsome.cat.ts.ui.qa.SpellQA
-keep class net.heartsome.cat.ts.ui.qa.fileAnalysis.*

-dontnote

-keep class net.heartsome.license.webservice.IService {
    public <methods>;
}

-keep public class * implements java.beans.PropertyChangeListener {*;}
-keep public class * extends net.heartsome.cat.converter.util.AbstractModelObject {*;}
-keep public class * extends de.jaret.util.misc.PropertyObservableBase {*;}

#######################è½????ä¸?xliff???ä»¶ç??å¯¹è??æ¡???????æ®µè???????????æµ?è§??????????######################
-keep class net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacade
-keep class net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacadeImpl
#################################################################################

####################å¯¼å?ºä¸ºrtf######################################
-keep class net.heartsome.cat.ts.ui.rtf.exporter.Export {*;}
#################################################################

# VTD ??¹ä¸º??´æ?¥å????¨æ?????ï¼?ä¸???¾ç¤º??¸å?³è????????ä¹?ä¸?è¦?æ··æ????¸å?³ä»£???
-dontwarn com.ximpleware.**
-dontwarn java_cup.*
-keep class com.ximpleware.** { *;}
-keep class java_cup.** { *;}
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/obfuscate/annotations.jar"
   -include "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/obfuscate/annotations.pro"
   -libraryjars "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/jce.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/log4j-1.2.15.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/slf4j-api-1.5.8.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/slf4j-log4j12-1.5.8.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/junit.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/hslibrary3.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/xercesImpl.jar"
   -libraryjars "/Users/Mac/r8PackGit/translation-studio/tools/obclipse/resources/hsts8/resolver.jar"

   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/com.ibm.icu_4.4.2.v20110823.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/de.jaret.util.ui.table_0.85.0.R8b_v20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/heartsome.java.tools.plugin_8.0.0.R8b_v20121025.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/javax.servlet.jsp_2.0.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/javax.servlet_2.5.0.v201103041518.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.thirdpartlibrary_8.1.0.R8b_v20131111.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.library3_3.0.1.R8b_v20130408.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.sourceforge.nattable.core_8.0.1.R8b_v20130827.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.commons.codec_1.3.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.commons.el_1.0.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.commons.httpclient_3.1.0.v201012070820.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.commons.lang_2.4.0.v201005080502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.commons.logging_1.0.4.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.jasper_5.5.17.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.lucene.analysis_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.lucene.core_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.apache.lucene_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.compare.core_3.5.200.I20110208-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.commands_3.6.0.I20110111-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.databinding.beans_1.2.100.I20100824-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.databinding.observable_1.4.0.I20110222-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.databinding.property_1.4.0.I20110222-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.databinding_1.4.0.I20110111-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.expressions_3.4.300.v20110228.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filebuffers_3.5.200.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filesystem.linux.x86_1.4.0.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filesystem.linux.x86_64_1.2.0.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filesystem.win32.x86_1.1.300.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filesystem.win32.x86_64_1.1.300.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.filesystem_1.3.100.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.jobs_3.5.101.v20120113-1953.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net.linux.x86_1.1.200.I20110419-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net.linux.x86_64_1.1.0.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net.win32.x86_1.0.100.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net.win32.x86_64_1.0.100.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.net_1.2.100.I20110511-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.resources.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.resources_3.7.101.v20120125-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.runtime.compatibility.auth_3.2.200.v20110110.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.filetransfer_5.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.identity_3.1.100.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.provider.filetransfer.httpclient.ssl_1.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.provider.filetransfer.httpclient_4.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.provider.filetransfer.ssl_1.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.provider.filetransfer_3.2.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf.ssl_1.0.100.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ecf_3.1.300.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.app_1.3.100.v20110321.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.ds_1.3.1.R37x_v20110701.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.frameworkadmin.equinox_1.0.300.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.frameworkadmin_2.0.0.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.http.jetty_2.0.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.http.registry_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.http.servlet_1.1.200.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.jsp.jasper.registry_1.0.200.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.jsp.jasper_1.0.300.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.cocoa.macosx.x86_64_1.1.101.v20120109-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.cocoa.macosx_1.1.101.v20120109-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_1.1.100.v20110505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.1.100.v20110505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.win32.win32.x86_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.artifact.repository_1.1.101.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.console_1.0.300.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.core_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.director_2.1.1.v20111126-0211.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.engine_2.1.1.R37x_v20111003.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.garbagecollector_1.0.200.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.jarprocessor_1.0.200.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.metadata.repository_1.2.0.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.metadata_2.1.0.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.operations_2.1.1.R37x_v20111111.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.ql_2.0.100.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.repository_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.touchpoint.eclipse_2.1.1.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.touchpoint.natives_1.0.300.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.transport.ecf_1.0.0.v20111128-0624.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.ui.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.p2.ui_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.preferences_3.4.2.v20120111-2020.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.registry_3.5.101.R37x_v20110810-1611.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.security.macosx_1.100.100.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.security.ui_1.1.0.v20101004.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.security.win32.x86_1.0.200.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.security.win32.x86_64_1.0.0.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.security_1.1.1.R37x_v20110822-1018.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.simpleconfigurator.manipulator_2.0.0.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.simpleconfigurator_1.0.200.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.equinox.util_1.0.300.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.help.base_3.6.2.v201202080800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.help.ui_3.5.101.r37_20110819.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.help.webapp.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.help.webapp_3.6.1.r37_20110929.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.help_3.5.100.v20110426.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.jface.databinding_1.5.0.I20100907-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.jface.nl_en_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.jface.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.jface.text_3.7.2.v20111213-1208.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.jface_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ltk.core.refactoring.nl_en_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ltk.core.refactoring.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ltk.core.refactoring_3.5.201.r372_v20111101-0700.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.nebula.widgets.grid_8.0.0.R8b_v20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.nebula.widgets.tablecombo_8.0.0.R8b_v20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.osgi.nl_en_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.osgi.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.osgi.services_3.3.0.v20110513.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.rcp_3.7.2.v201202080800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.cocoa.macosx.x86_64_3.102.0.v20130605-1544.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.cocoa.macosx_3.102.0.v20130605-1544.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.gtk.linux.x86_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.gtk.linux.x86_64_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.win32.win32.x86_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt.win32.win32.x86_64_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.swt_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.text_3.5.101.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.cocoa_1.1.0.I20101109-0800.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.editors_3.7.0.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.forms_3.5.101.v20111011-1919.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.ide.nl_en_8.0.3.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.ide.nl_zh_8.0.3.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.ide_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.navigator_3.5.101.v20120106-1355.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.net.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.net_1.2.100.v20111208-1155.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.views.properties.tabbed_3.5.200.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.views_3.6.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.workbench.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.workbench.texteditor_3.7.0.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui.workbench_3.7.1.v20120104-1859.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.ui_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.eclipse.update.configurator_3.3.100.v20100512.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.mortbay.jetty.server_6.1.23.v201012071420.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.mortbay.jetty.util_6.1.23.v201012071420.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.sat4j.core_2.3.0.v20110329.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/org.sat4j.pb_2.3.0.v20110329.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.core_8.1.0.R8b_v20131116/lib/antlr-2.7.4.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.core_8.1.0.R8b_v20131116/lib/chardet-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.core_8.1.0.R8b_v20131116/lib/cpdetector_1.0.10.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.core_8.1.0.R8b_v20131116/lib/jargs-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.memoQ6_8.0.4.R8b_v20130731/lib/commons-compress-1.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609/lib/commons-io-1.3.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609/lib/juh-2.3.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609/lib/jurt-2.3.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609/lib/ridl-2.3.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609/lib/unoil-2.3.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.hsql_8.0.1.R8b_v20130625/lib/hsqldb.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.mssql_8.0.2.R8b_v20130722/lib/jtds-1.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.mysql_8.0.2.R8b_v20130723/lib/mysql-connector-java-5.1.10-bin.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.oracle_8.1.0.R8b_v20131116/lib/ojdbc14.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.postgreSQL_8.1.0.R8b_v20131116/lib/postgresql-8.4-701.jdbc4.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.sqlite_8.1.0.R8b_v20131116/lib/sqlite-jdbc-3.7.15-SNAPSHOT.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui.tm_8.1.0.R8b_v20131119/lib/datechooser-1.01.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui.tm_8.1.0.R8b_v20131119/lib/jarettable-0.85.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui.tm_8.1.0.R8b_v20131119/lib/jaretutil-0.32.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/poi-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/poi-excelant-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/poi-ooxml-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/poi-ooxml-schemas-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/poi-scratchpad-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119/lib/xmlbeans-2.3.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.core_8.1.0.R8b_v20131116/lib/saxon9he.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.exportproject_8.1.0.R8b_v20131119/lib/ant.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/bcprov-jdk14-136.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/commons-codec-1.3.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/commons-httpclient-3.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/commons-logging-1.0.4.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/jdom-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/jug.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/log4j-1.2.14.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/wsdl4j-1.5.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108/lib/xfire-all-1.2.6.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.machinetranslation_8.1.0.R8b_v20131121/lib/google-api-translate-java-0.97.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.machinetranslation_8.1.0.R8b_v20131121/lib/json-20090211.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.machinetranslation_8.1.0.R8b_v20131121/lib/microsoft-translator-java-api-0.6.1-jar-with-dependencies.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/dom4j-1.6.1.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/jna.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/KTable.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/poi-3.7-20101029.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/poi-ooxml-3.7-20101029.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/poi-ooxml-schemas-3.7-20101029.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/poi-scratchpad-3.7-20101029.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121/lib/xmlbeans-2.3.0.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui_8.1.0.R8b_v20131114/lib/jaretutil-0.32.jar"
   -libraryjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.xml_8.1.0.R8b_v20131105/lib/dom4j-1.6.1.jar"

   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.advanced_8.0.2.R8b_v20130411"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.advanced_8.0.2.R8b_v20130411"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.rtf_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.rtf_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.lockrepeat_8.0.1.R8b_v20130620"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.lockrepeat_8.0.1.R8b_v20130620"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.xml_8.1.0.R8b_v20131105"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.xml_8.1.0.R8b_v20131105"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.resx_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.resx_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.mysql_8.0.2.R8b_v20130723"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.mysql_8.0.2.R8b_v20130723"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.memoQ6_8.0.4.R8b_v20130731"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.memoQ6_8.0.4.R8b_v20130731"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.shield_8.0.2.R8b_v20130902"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield_8.0.2.R8b_v20130902"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.msoffice2003_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.hsql_8.0.1.R8b_v20130625"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.hsql_8.0.1.R8b_v20130625"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.plugin_8.0.3.R8b_v20130912"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.plugin_8.0.3.R8b_v20130912"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.shield.workbench_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.workbench_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.term_8.1.0.R8b_v20131120"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.term_8.1.0.R8b_v20131120"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.taggedrtf_8.1.0.R8b_v20131021"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.taggedrtf_8.1.0.R8b_v20131021"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui_8.0.2.R8b_v20130701"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.ui_8.0.2.R8b_v20130701"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.word2007_8.0.5.R8b_v20130926"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.word2007_8.0.5.R8b_v20130926"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.pptx_8.0.5.R8b_v20130730"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.pptx_8.0.5.R8b_v20130730"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.openoffice_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.openoffice_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.xml_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.xml_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.importproject_8.1.0.R8b_v20131113"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.importproject_8.1.0.R8b_v20131113"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.qa_8.1.0.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.quicktranslation_8.0.2.R8b_v20130905"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.quicktranslation_8.0.2.R8b_v20130905"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.idml_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.idml_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.DejaVuX2_8.0.2.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.DejaVuX2_8.0.2.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.docx_8.1.0.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.docx_8.1.0.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.MIF_8.0.4.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.MIF_8.0.4.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.ui.rcp_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.ui.rcp_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.shield.resources_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.resources_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts_8.0.1.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts_8.0.1.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.inx_8.0.2.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.inx_8.0.2.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.javascript_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.javascript_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.tb_8.0.1.R8b_v20130625"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.tb_8.0.1.R8b_v20130625"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.text_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.text_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.xliffeditor.nattable_8.1.0.R8b_v20131120"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.xliffeditor.nattable_8.1.0.R8b_v20131120"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.tm_8.0.3.R8b_v20130827"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.tm_8.0.3.R8b_v20130827"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui_8.1.0.R8b_v20131114"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui_8.1.0.R8b_v20131114"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.navigator.resources_8.1.0.R8b_v20131113"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.navigator.resources_8.1.0.R8b_v20131113"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.rc_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.rc_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.shield.help_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.help_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.core_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.core_8.1.0.R8b_v20131116"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.wordfast3_8.0.2.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.wordfast3_8.0.2.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.javaproperties_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.javaproperties_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.msexcel2007_8.0.3.R8b_v20130409"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.msexcel2007_8.0.3.R8b_v20130409"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.pretranslation_8.0.4.R8b_v20130904"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.pretranslation_8.0.4.R8b_v20130904"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.exportproject_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.exportproject_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.postgreSQL_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.postgreSQL_8.1.0.R8b_v20131116"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.core_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.core_8.1.0.R8b_v20131116"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui_8.1.0.R8b_v20131114"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui_8.1.0.R8b_v20131114"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.mssql_8.0.2.R8b_v20130722"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.mssql_8.0.2.R8b_v20130722"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.p2update_8.0.2.R8b_v20130613"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.p2update_8.0.2.R8b_v20130613"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.handlexlf_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.handlexlf_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.oracle_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.oracle_8.1.0.R8b_v20131116"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.tb_8.0.2.R8b_v20130625"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.tb_8.0.2.R8b_v20130625"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.ttx_8.0.5.R8b_v20130925"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.ttx_8.0.5.R8b_v20130925"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.help_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.help_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.fuzzyTranslation_8.0.2.R8b_v20130613"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.fuzzyTranslation_8.0.2.R8b_v20130613"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui.tm_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.ui.tm_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter_8.0.3.R8b_v20130826"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter_8.0.3.R8b_v20130826"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.html_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.html_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.ui_8.0.4.R8b_v20130729"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.ui_8.0.4.R8b_v20130729"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.ui.tb_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.ui.tb_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.tm_8.0.4.R8b_v20130801"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.tm_8.0.4.R8b_v20130801"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.trados2009_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.trados2009_8.1.0.R8b_v20131116"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.websearch_8.1.0.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.websearch_8.1.0.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.jumpsegment_8.0.2.R8b_v20130424"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.jumpsegment_8.0.2.R8b_v20130424"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.ui.translation_8.1.0.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.ui.translation_8.1.0.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.machinetranslation_8.1.0.R8b_v20131121"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.machinetranslation_8.1.0.R8b_v20131121"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.converter.po_8.0.3.R8b_v20130609"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.converter.po_8.0.3.R8b_v20130609"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database_8.1.0.R8b_v20131119"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database_8.1.0.R8b_v20131119"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.ts.help_8.1.0.R8b_v20131108"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.common.ui.navigator_8.0.0.R8b_v20121115"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.common.ui.navigator_8.0.0.R8b_v20121115"
   -injars  "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/net.heartsome.cat.database.sqlite_8.1.0.R8b_v20131116"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/r8_pack/1121U/repository/plugins/tmpOb/net.heartsome.cat.database.sqlite_8.1.0.R8b_v20131116"
