package net.heartsome.cat.ts.ui.composite;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * 所有非向导框的弹出窗体所用的图标显示区
 * @author  robert	2012-01-12
 * @version 
 * @since   JDK1.6
 */
public class DialogLogoCmp extends Composite{
	//logo显示区的名称
	private String title;
	//logo显示区的描述
	private String message;
	private Label titileLbl;
	private Label tipTxt;
	private Image logo;
	private Composite rightLogoCmp;
	
	public DialogLogoCmp(Composite parent, int style, String title, String message,Image logo) {
		super(parent, style | SWT.BORDER);		
		this.title = title;
		this.message = message;
		this.logo = logo;
		init();
	}
	
	public void init(){
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 70).applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).applyTo(this);
		Color textBgColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		this.setBackground(textBgColor);
		
		Composite leftLogoCmp = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftLogoCmp);
		leftLogoCmp.setBackground(textBgColor);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(10, 0, 8, 0).applyTo(leftLogoCmp);
		
		titileLbl = new Label(leftLogoCmp, SWT.NONE);
		titileLbl.setText(title == null ? "" : title);
		titileLbl.setBackground(textBgColor);
		titileLbl.setFont(JFaceResources.getBannerFont());
		
		tipTxt = new Label(leftLogoCmp, SWT.WRAP);
		tipTxt.setText(message == null ? "" : message);
		GridDataFactory.fillDefaults().grab(true, true).indent(8, 4).applyTo(tipTxt);
		tipTxt.setBackground(textBgColor);
		tipTxt.setToolTipText(message == null ? "" : message);
		tipTxt.setFont(JFaceResources.getDialogFont());
		
		rightLogoCmp = new Composite(this, SWT.NONE);
		rightLogoCmp.setBackground(textBgColor);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).grab(false, true).applyTo(rightLogoCmp);
		if(logo!=null){
			rightLogoCmp.setBackgroundImage(logo);			
		}
	}
	
	/**
	 * 给弹出窗体的logo区设置标题
	 * @param title ;
	 */
	public void setTitle(String title){
		this.title = title;
		titileLbl.setText(title);
	}
	
	/**
	 * 给弹出窗体的logo区设置提示信息
	 * @param message ;
	 */
	public void setMessage(String message){
		this.message = message;
		tipTxt.setText(message);
	}
	
	/**
	 * 设置Logo图标
	 * @param logo 图标;
	 */
	public void setLogo(Image logo){
		if(rightLogoCmp != null && !rightLogoCmp.isDisposed()){
			rightLogoCmp.setBackgroundImage(logo);
			if(this.logo != null){
				logo.dispose();
			}
			this.logo = logo;
			rightLogoCmp.getParent().layout();
		}
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose(){
		if(this.logo != null && !this.logo.isDisposed()){
			this.logo.dispose();
		}
		super.dispose();
	}
}
