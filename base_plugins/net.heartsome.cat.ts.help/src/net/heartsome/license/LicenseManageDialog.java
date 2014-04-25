package net.heartsome.license;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.heartsome.cat.common.ui.Activator;
import net.heartsome.cat.common.ui.dialog.HsPreferenceDialog;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.generator.LicenseIdGenerator;
import net.heartsome.license.resource.Messages;
import net.heartsome.license.utils.StringUtils;
import net.heartsome.license.webservice.ServiceUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class LicenseManageDialog extends Dialog {
	
	private int type;
	private String utilDate;
	private String licenseId;
	private boolean isShowBack = false;
	private Point p;
	
	private Text text;
	private Text text1;
	private Text text2;
	private Text text3;
	private Text text4;
	private Text text5;
	private Text text6;
	private Label label4;
	private ProgressBar bar;
	private Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
	public LicenseManageDialog(Shell parent, int type, String utilDate, String licenseId) {
		super(parent);
		this.type = type;
		this.utilDate = utilDate;
		this.licenseId = licenseId;
	}
	
	public LicenseManageDialog(Shell parent, int type, String utilDate, String licenseId, boolean isShowBack, Point p) {
		super(parent);
		this.type = type;
		this.utilDate = utilDate;
		this.licenseId = licenseId;
		this.isShowBack = isShowBack;
		this.p = p;
	}
	
	@Override
	protected Point getInitialLocation(Point initialSize) {
		if (p == null) {
			return super.getInitialLocation(initialSize);
		} else {
			return p;
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (isShowBack) {
			Button backBtn = createButton(parent, IDialogConstants.BACK_ID, 
					Messages.getString("license.OfflineActiveDialog.backBtn"), false);
			
			backBtn.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					setReturnCode(OK);
					Point p = getShell().getLocation();
					close();
					ActiveMethodDialog dialog = new ActiveMethodDialog(getShell(), p);
					int result = dialog.open();
					if (result != IDialogConstants.OK_ID) {
						System.exit(0);
					}
				}
				
			});
		}
		
		Button button = createButton(parent, 11, Messages.getString("license.LicenseManageDialog.netconnection"), false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				PreferenceManager mgr = window.getWorkbench().getPreferenceManager();
				IPreferenceNode node = null;
				
				@SuppressWarnings("unchecked")
				List<IPreferenceNode> lstNodes = mgr.getElements(PreferenceManager.PRE_ORDER);
				for (IPreferenceNode n : lstNodes) {
					if(n.getId().equals("org.eclipse.ui.net.proxy_preference_page_context")){
						node = n;
					}
				}
				if(node == null){
					return;
				}
				mgr = new PreferenceManager();
				mgr.addToRoot(node);
				
				HsPreferenceDialog dlg = new HsPreferenceDialog(window.getShell(), mgr);
				dlg.create();
				final List<Image> imageList = new ArrayList<Image>();
				dlg.getTreeViewer().setLabelProvider(new PreferenceLabelProvider() {
					Image image = null;
					public Image getImage(Object element) {
						String id = ((IPreferenceNode) element).getId();						
						if ("org.eclipse.ui.net.proxy_preference_page_context".equals(id)) {
							// 网络连接							
							image = Activator.getImageDescriptor("icons/network.png").createImage();
							imageList.add(image);
							return image;
						} else {
							return null;
						}
					}
				});				
				dlg.open();
				for (Image img : imageList) {
					if (img != null && !img.isDisposed()) {
						img.dispose();
					}
				}
				imageList.clear();
			}
		});
		boolean isDefault = false;
		if (type == Constants.STATE_NOT_ACTIVATED || type == Constants.STATE_INVALID
				|| type == Constants.STATE_EXPIRED || type == Constants.EXCEPTION_INT14
				|| type == Constants.EXCEPTION_INT15 || type == Constants.EXCEPTION_INT1
				|| type == Constants.EXCEPTION_INT2 || type == Constants.EXCEPTION_INT3
				|| type == Constants.EXCEPTION_INT4) {
			createButton(parent, IDialogConstants.OK_ID, 
					Messages.getString("license.LicenseManageDialog.activeBtn"),true);
			isDefault = true;
		}
		createButton(parent, IDialogConstants.CANCEL_ID,
				Messages.getString("license.LicenseManageDialog.exitBtn"), !isDefault).setFocus();
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginTop = 10;
		tparent.setLayout(layout);
		
		GridData data = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL);
		tparent.setLayoutData(data);
		
		createStatusComp(tparent);
		createActiveComp(tparent);	
		createBarComp(tparent);
		
		return super.createDialogArea(parent);
	}
	
	private void createStatusComp(Composite parent) {
		Group statusGroup = new Group(parent, SWT.NONE);
		statusGroup.setText(Messages.getString("license.LicenseManageDialog.statusGroup"));
		GridData dataStatusGroup = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL);
		dataStatusGroup.heightHint = 150;
		statusGroup.setLayoutData(dataStatusGroup);
		statusGroup.setLayout(new GridLayout());
		
		GridData data = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL);
		RowLayout layout = new RowLayout();
		layout.center = true;
		
		Composite comp0 = new Composite(statusGroup, SWT.NONE);
		comp0.setLayoutData(data);
		comp0.setLayout(layout);
		
		Label statusLbl = new Label(comp0, SWT.NONE);
		statusLbl.setText(Messages.getString("license.LicenseManageDialog.statusLabel"));
		
		if (type == Constants.STATE_NOT_ACTIVATED) {
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.notActiveLabel"));
		} else if (type == Constants.STATE_VALID) {
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.activeLabel"));
			
			new Label(comp0, SWT.NONE).setLayoutData(new RowData(30, SWT.DEFAULT));
			
			Button btnCancelActive = new Button(comp0, SWT.NONE);
			btnCancelActive.setText(Messages.getString("license.LicenseManageDialog.cancelActiveButton"));
			btnCancelActive.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean result = MessageDialog.openConfirm(getShell(), Messages.getString("license.LicenseManageDialog.confirm"),
							Messages.getString("license.LicenseManageDialog.confirmMessage"));
					if (result) {
						try {
							int re = ServiceUtil.cancel();
							if (re == Constants.LOGOUT_SUCCESS) {
								MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
										Messages.getString("license.LicenseManageDialog.unactiveSuccess"));
								LicenseManageDialog.this.close();
								PlatformUI.getWorkbench().restart();
							} else {
								MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
										Messages.getString("license.LicenseManageDialog.unactiveFail"));
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							Throwable t = e1;
							while(t.getCause() != null) {
								t = t.getCause();
							}
							if (t instanceof java.security.cert.CertificateException) {
								MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleNet"), 
										MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoNet"), Constants.EXCEPTION_STRING16));
							} else {
								MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleNet"), 
										MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoNet"), Constants.EXCEPTION_STRING17));
							}
						}
					}
				}
				
			});
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			comp1.setLayoutData(data);
			comp1.setLayout(layout);
			
			Label typeLbl = new Label(comp1, SWT.NONE);
			typeLbl.setText(Messages.getString("license.LicenseManageDialog.typeLabel"));
			
			Label typeLbl1 = new Label(comp1, SWT.NONE);
			typeLbl1.setText(Messages.getString(utilDate == null ? 
					"license.LicenseManageDialog.typeBusiness" : "license.LicenseManageDialog.typeTemp"));
			
			if (utilDate != null ) {
				Composite comp2 = new Composite(statusGroup, SWT.NONE);
				comp2.setLayoutData(data);
				comp2.setLayout(layout);
				
				Label typeLbl2 = new Label(comp2, SWT.NONE);
				typeLbl2.setText(Messages.getString("license.LicenseManageDialog.utilDate"));
				
				Label dateLbl = new Label(comp2, SWT.NONE);
				dateLbl.setText(utilDate);
			}
		}  else if (type == Constants.STATE_INVALID) {
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.invalidLicense"));
		} else if (type == Constants.STATE_EXPIRED) {
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.expired"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			comp1.setLayoutData(data);
			comp1.setLayout(layout);
			
			Label typeLbl = new Label(comp1, SWT.NONE);
			typeLbl.setText(Messages.getString("license.LicenseManageDialog.typeLabel"));
			
			Label typeLbl1 = new Label(comp1, SWT.NONE);
			typeLbl1.setText(Messages.getString(utilDate == null ? 
					"license.LicenseManageDialog.typeBusiness" : "license.LicenseManageDialog.typeTemp"));
			
			if (utilDate != null ) {
				Composite comp2 = new Composite(statusGroup, SWT.NONE);
				comp2.setLayoutData(data);
				comp2.setLayout(layout);
				
				Label typeLbl2 = new Label(comp2, SWT.NONE);
				typeLbl2.setText(Messages.getString("license.LicenseManageDialog.utilDate"));
				
				Label dateLbl = new Label(comp2, SWT.NONE);
				dateLbl.setText(utilDate);
			}
		} else if (type == Constants.EXCEPTION_INT16 || type == Constants.EXCEPTION_INT17) {
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.unvalidate"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			GridData data1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
			comp1.setLayoutData(data1);
			GridLayout layout1 = new GridLayout();
			comp1.setLayout(layout1);
			
			Label noticeLbl = new Label(comp1, SWT.WRAP);
			GridData dataLabel = new GridData(GridData.FILL_HORIZONTAL);
			noticeLbl.setLayoutData(dataLabel);
			noticeLbl.setText(MessageFormat.format(Messages.getString("license.LicenseManageDialog.noticeLbl"), StringUtils.getErrorCode(type)));
		} else if (type == Constants.EXCEPTION_INT14) { 
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.licenseException"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			GridData data1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
			comp1.setLayoutData(data1);
			GridLayout layout1 = new GridLayout();
			comp1.setLayout(layout1);
			
			Label noticeLbl = new Label(comp1, SWT.WRAP);
			GridData dataLabel = new GridData(GridData.FILL_HORIZONTAL);
			noticeLbl.setLayoutData(dataLabel);
			noticeLbl.setText(MessageFormat.format(Messages.getString("license.LicenseManageDialog.noSameVersion"), 
					getVersionContent(System.getProperty("TSEdition")), getVersionContent(new LicenseIdGenerator(licenseId).getVersion())));
		} else if (type == Constants.EXCEPTION_INT15) { 
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.licenseException"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			GridData data1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
			comp1.setLayoutData(data1);
			GridLayout layout1 = new GridLayout();
			comp1.setLayout(layout1);
			
			Label noticeLbl = new Label(comp1, SWT.WRAP);
			GridData dataLabel = new GridData(GridData.FILL_HORIZONTAL);
			noticeLbl.setLayoutData(dataLabel);
			noticeLbl.setText(Messages.getString("license.LicenseManageDialog.maccodeError"));
		} else if (type == Constants.EXCEPTION_INT1 || type == Constants.EXCEPTION_INT2 
				|| type == Constants.EXCEPTION_INT3 || type == Constants.EXCEPTION_INT4) { 
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.licenseException"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			GridData data1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
			comp1.setLayoutData(data1);
			GridLayout layout1 = new GridLayout();
			comp1.setLayout(layout1);
			
			Label noticeLbl = new Label(comp1, SWT.WRAP);
			GridData dataLabel = new GridData(GridData.FILL_HORIZONTAL);
			noticeLbl.setLayoutData(dataLabel);
			noticeLbl.setText(MessageFormat.format(Messages.getString("license.LicenseManageDialog.licenseExceptionInfo1"), StringUtils.getErrorCode(type)));
		} else { 
			Label statusLbl1 = new Label(comp0, SWT.NONE);
			statusLbl1.setText(Messages.getString("license.LicenseManageDialog.licenseException"));
			
			Composite comp1 = new Composite(statusGroup, SWT.NONE);
			GridData data1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
			comp1.setLayoutData(data1);
			GridLayout layout1 = new GridLayout();
			comp1.setLayout(layout1);
			
			Label noticeLbl = new Label(comp1, SWT.WRAP);
			GridData dataLabel = new GridData(GridData.FILL_HORIZONTAL);
			noticeLbl.setLayoutData(dataLabel);
			noticeLbl.setText(MessageFormat.format(Messages.getString("license.LicenseManageDialog.licenseExceptionInfo"), StringUtils.getErrorCode(type)));
		}
	}
	
	private void createActiveComp(Composite parent) {
		Group licenseIdGroup = new Group(parent, SWT.NONE);
		licenseIdGroup.setText(Messages.getString("license.LicenseManageDialog.licenseIdGroup"));
		GridData dataLicenseIdGroup = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL);
		dataLicenseIdGroup.heightHint = 110;
		licenseIdGroup.setLayoutData(dataLicenseIdGroup);
		GridLayout layoutGroup = new GridLayout(2, false);
		layoutGroup.marginLeft = 5;
		layoutGroup.marginHeight = 10;
		licenseIdGroup.setLayout(layoutGroup);
		
		Label label = new Label(licenseIdGroup,SWT.NONE);
		label.setText(Messages.getString("license.LicenseManageDialog.licenseIdLabel"));
		
		createIdInputComp(licenseIdGroup);
		
		Composite compLink = new Composite(licenseIdGroup, SWT.NONE); 
		RowLayout layoutLink = new RowLayout();
		layoutLink.marginTop = 20;
		layoutLink.marginRight = 0;
		layoutLink.marginLeft = 0;
		layoutLink.marginBottom = 10;
		compLink.setLayout(layoutLink);
		GridData linkData = new GridData(GridData.FILL_HORIZONTAL);
		linkData.horizontalSpan = 2;
		compLink.setLayoutData(linkData);
		
		Label label1 = new Label(compLink, SWT.NONE);
		label1.setText(Messages.getString("license.LicenseManageDialog.label1"));
		
		if (!"L".equals(System.getProperty("TSEdition"))) {
			Label link1 = new Label(compLink, SWT.NONE);
			link1.setText(Messages.getString("license.LicenseManageDialog.link1"));
			link1.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			link1.setCursor(cursor);
			link1.addMouseListener(new MouseListener() {
	
				public void mouseUp(MouseEvent e) {
					
				}
	
				public void mouseDown(MouseEvent e) {
					Program.launch(Messages.getString("license.LicenseManageDialog.urlr8buy") + 
							"&PRODUCT=" + ProtectionFactory.getProduct() + "&PLATFORM=" + ProtectionFactory.getPlatform());
				}
	
				public void mouseDoubleClick(MouseEvent e) {
	
				}
			});
			
			Label label2 = new Label(compLink, SWT.NONE);
			label2.setText(Messages.getString("license.LicenseManageDialog.label2"));
		}
		
		Label link2 = new Label(compLink, SWT.NONE);
		link2.setText(Messages.getString("license.LicenseManageDialog.link2"));
		link2.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		link2.setCursor(cursor);
		link2.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
				
			}

			public void mouseDown(MouseEvent e) {
				Program.launch(Messages.getString("license.LicenseManageDialog.urlr8trial") + 
						"&PRODUCT=" + ProtectionFactory.getProduct() + "&PLATFORM=" + ProtectionFactory.getPlatform());
			}

			public void mouseDoubleClick(MouseEvent e) {

			}
		});
		
		Label label3 = new Label(compLink, SWT.NONE);
		label3.setText(Messages.getString("license.LicenseManageDialog.label3"));
	}
	
	private void createBarComp(Composite parent) {
		Composite barComp = new Composite(parent, SWT.NONE);
		GridLayout barLayout = new GridLayout();
		barLayout.marginTop = 10;
		barComp.setLayout(barLayout);
		barComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label4 = new Label(barComp, SWT.NONE);
		label4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		bar = new ProgressBar(barComp, SWT.NONE);
		bar.setMaximum(10);
		bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setVisible(false);
	}
	
	private void setVisible(boolean b) {
		if (b) {
			label4.setVisible(true);
			label4.setText(Messages.getString("license.LicenseManageDialog.label4"));
			bar.setVisible(true);
		} else {
			label4.setVisible(false);
			label4.setText("");
			bar.setVisible(false);
			bar.setSelection(0);
		}
	}

	private void createIdInputComp(Composite parent) {
		if (type == Constants.STATE_NOT_ACTIVATED || type == Constants.STATE_INVALID
				|| type == Constants.STATE_EXPIRED || type == Constants.EXCEPTION_INT14
				|| type == Constants.EXCEPTION_INT15 || type == Constants.EXCEPTION_INT1
				|| type == Constants.EXCEPTION_INT2 || type == Constants.EXCEPTION_INT3
				|| type == Constants.EXCEPTION_INT4) {
			Composite comp = new Composite(parent, SWT.NONE); 
			GridLayout layout = new GridLayout(11, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			comp.setLayout(layout);
			
			GridData compData = new GridData();
			comp.setLayoutData(compData);
			
			GridData textData = new GridData();
			textData.widthHint = 40;
			
			GridData labelData = new GridData();
			labelData.widthHint = 5;
			
			text1 = new Text(comp,SWT.BORDER);
			text1.setLayoutData(textData);
			text1.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text1.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text1.setText(s.substring(0, 4));
									text2.setFocus();
									text2.setText(s.substring(4));
								} else if (length == 4) {
									text2.setFocus();
								}
							}
						} else {
							text1.setText(s.substring(0, i));
							break;
						}
					}
				}
				
			});
			
			Label label = new Label(comp,SWT.NONE);
			label.setLayoutData(labelData);
			
			text2 = new Text(comp,SWT.BORDER);
			text2.setLayoutData(textData);
			text2.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text2.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text2.setText(s.substring(0, 4));
									text3.setFocus();
									text3.setText(s.substring(4));
								} else if (length == 4) {
									text3.setFocus();
								}
							}
						} else {
							text2.setText(s.substring(0, i));
						}
					}
				}
				
			});
			
			label = new Label(comp,SWT.NONE);
			label.setLayoutData(labelData);
			
			text3 = new Text(comp,SWT.BORDER);
			text3.setLayoutData(textData);
			text3.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text3.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text3.setText(s.substring(0, 4));
									text4.setFocus();
									text4.setText(s.substring(4));
								} else if (length == 4) {
									text4.setFocus();
								}
							}
						} else {
							text3.setText(s.substring(0, i));
						}
					}
				}
				
			});
			
			label = new Label(comp,SWT.NONE);
			label.setLayoutData(labelData);
			
			text4 = new Text(comp,SWT.BORDER);
			text4.setLayoutData(textData);
			text4.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text4.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text4.setText(s.substring(0, 4));
									text5.setFocus();
									text5.setText(s.substring(4));
								} else if (length == 4) {
									text5.setFocus();
								}
							}
						} else {
							text4.setText(s.substring(0, i));
						}
					}
				}
				
			});
			
			label = new Label(comp,SWT.NONE);
			label.setLayoutData(labelData);
			
			text5 = new Text(comp,SWT.BORDER);
			text5.setLayoutData(textData);
			text5.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text5.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text5.setText(s.substring(0, 4));
									text6.setFocus();
									text6.setText(s.substring(4));
								} else if (length == 4) {
									text6.setFocus();
								}
							}
						} else {
							text5.setText(s.substring(0, i));
						}
					}
				}
				
			});
			
			label = new Label(comp,SWT.NONE);
			label.setLayoutData(labelData);
			
			text6 = new Text(comp,SWT.BORDER);
			text6.setLayoutData(textData);
			text6.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					String s = text6.getText();
					s = s.replaceAll("-", "");
					int length = s.length();
					for (int i = 0; i < 4; i++) {
						if (i >= length) {
							break;
						}
						char c = s.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							if (i == 3) {
								if (length > 4) {
									text6.setText(s.substring(0, 4));
								}
							}
						} else {
							text6.setText(s.substring(0, i));
						}
					}
				}
				
			});
		} else {
			text = new Text(parent,SWT.NONE);
			text.setText(StringUtils.groupString(licenseId));
			text.setBackground(parent.getBackground());
			text.setEditable(false);
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.LicenseManageDialog.title"));
	}

	@Override
	protected Point getInitialSize() {
		return new Point(630, 470);
	}

	@Override
	protected void okPressed() {
		LicenseIdValidator va = new LicenseIdValidator(getLicenseId());
		if (va.checkLicense()) {
			if (va.checkEdition()) {
				setVisible(true);
				try {
					int r = ServiceUtil.active(getLicenseId(), bar);
					if (r == Constants.ACTIVE_OK_INT) {
						setVisible(false);
						super.okPressed();
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.activeSuccess"));
					} else if (r == Constants.RETURN_MUTILTEMPBUNDLE_INT){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.tempMutilActive"));
					} else if (r == Constants.RETURN_INVALIDBUNDLE_INT){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.activedLicense"));
					} else if (r == Constants.RETURN_INVALIDLICENSE_INT){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.infoInvalid"));
					} else if (r == Constants.RETURN_EXPIREDLICENSE_INT){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.expired"));
					} else if (r == Constants.RETURN_STOPLICENSE_INT){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.stopLicense"));
					} else if (r == Constants.EXCEPTION_INT8){
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								Messages.getString("license.LicenseManageDialog.activeByAdmin"));
						System.exit(0);
					} else {
						setVisible(false);
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								MessageFormat.format(Messages.getString("license.LicenseManageDialog.activeFail"), StringUtils.getErrorCode(r)));
					}
				} catch (Exception e) {
					setVisible(false);
					e.printStackTrace();
					
					Throwable t = e;
					while(t.getCause() != null) {
						t = t.getCause();
					}
					if (t instanceof java.security.cert.CertificateException) {
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleNet"), 
								MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoNet"), Constants.EXCEPTION_STRING16));
					} else {
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleNet"), 
								MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoNet"), Constants.EXCEPTION_STRING17));
					}
				}
			} else {
//				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleInvalid"), 
//						MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoInvalid1"), 
//								getVersionContent(System.getProperty("TSEdition")), getVersionContent(new LicenseIdGenerator(getLicenseId()).getVersion())));
				String edition1 = getVersionContent(System.getProperty("TSEdition"));
				String editionInput = new LicenseIdGenerator(getLicenseId()).getVersion();
				String edition2 = getVersionContent(editionInput);
				String message = MessageFormat.format(Messages.getString("license.LicenseManageDialog.infoInvalid1"), edition1, edition2);
				ArrayList<HashMap<String, Integer>> list = new ArrayList<HashMap<String, Integer>>();
				HashMap<String, Integer> map1 = new HashMap<String, Integer>();
				map1.put("start", message.indexOf(edition1));
				map1.put("length", edition1.length());
				list.add(map1);
				HashMap<String, Integer> map2 = new HashMap<String, Integer>();
				map2.put("start", message.indexOf(edition2));
				map2.put("length", edition2.length());
				list.add(map2);
				HashMap<String, Integer> map3 = new HashMap<String, Integer>();
				map3.put("start", message.lastIndexOf(edition1));
				map3.put("length", edition1.length());
				list.add(map3);
				HashMap<String, Integer> map4 = new HashMap<String, Integer>();
				map4.put("start", message.lastIndexOf(edition2));
				map4.put("length", edition2.length());
				list.add(map4);
				new CustomMessageDialog(getShell(), Messages.getString("license.LicenseManageDialog.titleInvalid"), message, list, editionInput).open();
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleInvalid"), 
					Messages.getString("license.LicenseManageDialog.infoInvalid"));
		}
	}
	
	private String getLicenseId() {
		return text1.getText() + text2.getText() + text3.getText() 
				+ text4.getText() + text5.getText() + text6.getText();
	}

	public static void main(String[] argv) {
		Shell shell = new Shell();
		new LicenseManageDialog(shell, 5, null, "121312345678901234567890").open();
	}
	
	@Override
	public boolean close() {
		if(cursor != null && !cursor.isDisposed()){
			cursor.dispose();
		}
		return super.close();
	}
	
	private String getVersionContent(String version) {
		if ("U".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Ultimate");
		} else if ("F".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Professional");
		} else if ("P".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Personal");
		} else {
			return Messages.getString("license.LicenseManageDialog.Lite");
		}
	}
}
