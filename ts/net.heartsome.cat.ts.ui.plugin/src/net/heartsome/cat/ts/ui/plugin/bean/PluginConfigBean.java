package net.heartsome.cat.ts.ui.plugin.bean;

/**
 * 插件配置的bean
 * @author robert 2012-03-03
 * @version
 * @since JDK1.6
 */
public class PluginConfigBean {
	/** 插件的ID */
	private String id;
	/** 插件的名称 */
	private String name;
	/** 插件的命令行 */
	private String commandLine;
	/** 输出 */
	private String output;
	/** 输入 */
	private String input;
	/** 快捷键 */
	private String shortcutKey;
	/** 交换文件 */
	private String outputPath;
	
	public PluginConfigBean() {
	}
	
	public PluginConfigBean(String id, String name, String commandLine, String input, String output, String outputPath, String shortcutKey){
		this.id = id;
		this.name = name;
		this.commandLine = commandLine;
		this.output = output;
		this.input = input;
		this.shortcutKey = shortcutKey;
		this.outputPath = outputPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getShortcutKey() {
		return shortcutKey;
	}

	public void setShortcutKey(String shortcutKey) {
		this.shortcutKey = shortcutKey;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PluginConfigBean) {
			PluginConfigBean bean = (PluginConfigBean) obj;
			
			if (bean.getName().equals(this.name) && bean.getCommandLine().equals(this.commandLine)
					&& bean.getInput().equals(this.input) && bean.getOutput().equals(this.output)
					&& bean.getOutputPath().equals(this.outputPath) && bean.getShortcutKey().equals(this.shortcutKey)) {
				return true;
			}
		}
		return false;
	}
}
