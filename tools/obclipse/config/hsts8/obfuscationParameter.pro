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

# 用于存放各个插件的导出包的类名，public的方法和变量 ################################################
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




##############如果目标平台使用的不是hudson的目标平台，那么使用eclipse export需要添加####################
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

#######################转换为xliff文件的对话框的分段规则的“浏览”按钮######################
-keep class net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacade
-keep class net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacadeImpl
#################################################################################

####################导出为rtf######################################
-keep class net.heartsome.cat.ts.ui.rtf.exporter.Export {*;}
#################################################################

# VTD 改为直接引用源码，不显示相关警告、也不要混淆相关代码
-dontwarn com.ximpleware.**
-dontwarn java_cup.*
-keep class com.ximpleware.** { *;}
-keep class java_cup.** { *;}
