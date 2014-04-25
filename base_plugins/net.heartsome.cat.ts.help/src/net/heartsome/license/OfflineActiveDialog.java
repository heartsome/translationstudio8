package net.heartsome.license;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.encrypt.OffLineEncrypt;
import net.heartsome.license.generator.IKeyGenerator;
import net.heartsome.license.generator.KeyGeneratorImpl;
import net.heartsome.license.generator.LicenseIdGenerator;
import net.heartsome.license.resource.Messages;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.utils.StringUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 脱机激活对话框
 * @author karl
 * @version
 * @since JDK1.6
 */
public class OfflineActiveDialog extends Dialog {
	
	private Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
	private Text text1;
	private Text text2;
	private Text text3;
	private Text text4;
	private Text text5;
	private Text text6;
	private Point p;

	protected OfflineActiveDialog(Shell parentShell) {
		super(parentShell);

	}
	
	protected OfflineActiveDialog(Shell parentShell, Point p) {
		super(parentShell);
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
		
		super.createButtonsForButtonBar(parent);
		Button nextBtn = getButton(IDialogConstants.OK_ID);
		nextBtn.setText(Messages.getString("license.LicenseAgreementDialog.nextBtn"));
		Button exitBtn = getButton(IDialogConstants.CANCEL_ID);
		exitBtn.setText(Messages.getString("license.LicenseAgreementDialog.exitBtn"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginTop = 10;
		tparent.setLayout(layout);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(tparent);

		Composite compNav = new Composite(tparent, SWT.NONE);
		GridLayout navLayout = new GridLayout();
		compNav.setLayout(navLayout);
		
		createNavigation(compNav);

		Group groupLicenseId = new Group(tparent, SWT.NONE);
		groupLicenseId.setText(Messages.getString("license.OfflineActiveDialog.licenseIdGroup"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupLicenseId);
		GridLayout layoutGroup = new GridLayout(2, false);
		layoutGroup.marginLeft = 5;
		layoutGroup.marginHeight = 100;
		groupLicenseId.setLayout(layoutGroup);

		Label label = new Label(groupLicenseId, SWT.NONE);
		label.setText(Messages.getString("license.LicenseManageDialog.licenseIdLabel"));

		createIdInputComp(groupLicenseId);

		Composite compLink = new Composite(groupLicenseId, SWT.NONE);
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
					Program.launch(Messages.getString("license.LicenseManageDialog.urlr8buy") + "&PRODUCT="
							+ ProtectionFactory.getProduct() + "&PLATFORM=" + ProtectionFactory.getPlatform());
				}

				public void mouseDoubleClick(MouseEvent e) {

				}
			});
		} else {
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
		}

		Label label3 = new Label(compLink, SWT.NONE);
		label3.setText(Messages.getString("license.LicenseManageDialog.label3"));

		return tparent;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 470);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.LicenseManageDialog.title"));
	}
	
	private void createNavigation(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.operatenavigation"));
		
		RowLayout layout = new RowLayout();
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(layout);
		
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.inputlicenseid"));
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.getactivekey"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.getgrantfile"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.activefinish"));
	}
	
	private void createIdInputComp(Composite parent) {
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
	}

	private String getLicenseId() {
		return text1.getText() + text2.getText() + text3.getText() 
				+ text4.getText() + text5.getText() + text6.getText();
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
	
	@Override
	protected void okPressed() {
		LicenseIdValidator va = new LicenseIdValidator(getLicenseId());
		if (va.checkLicense()) {
			if (va.getType()) {
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						Messages.getString("license.OfflineActiveDialog.onlyCommercial"));
			} else {
				if (va.checkEdition()) {
					String series = ProtectionFactory.getSeries();
					if (series == null || "".equals(series)) {
						MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
								MessageFormat.format(Messages.getString("license.OfflineActiveDialog.getActiveKeyFail"), StringUtils.getErrorCode(Constants.EXCEPTION_INT5)));
						return;
					}
					
					String installKey = null;
					OffLineActiveService v = new OffLineActiveService();
					if (FileUtils.isExsitInstall()) {
						int r = v.readInstallFile();
						if (r == Constants.EXCEPTION_INT3 || r == Constants.EXCEPTION_INT4) {
							MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
									MessageFormat.format(Messages.getString("license.OfflineActiveDialog.getActiveKeyFail"), StringUtils.getErrorCode(r)));
							return;
						} else {
							installKey = v.getInstallKey();
						}
					} else {
						int r = v.generateInstallFile();
						if (r == Constants.EXCEPTION_INT8) {
							MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
									Messages.getString("license.OfflineActiveDialog.getActiveKeyByAdmin"));
							System.exit(0);
						} else if (r == Constants.EXCEPTION_INT10 || r == Constants.EXCEPTION_INT11) {
							MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
									MessageFormat.format(Messages.getString("license.OfflineActiveDialog.getActiveKeyFail"), StringUtils.getErrorCode(r)));
							return;
						} else {
							installKey = v.getInstallKey();
						}
					}
					
					IKeyGenerator gen = new KeyGeneratorImpl();
					byte[] k = gen.generateKey(getLicenseId(), series, installKey, OffLineEncrypt.publicKey);
					Point p = getShell().getLocation();
					super.okPressed();
					GetActiveKeyDialog dialog = new GetActiveKeyDialog(getShell(), StringUtils.toHexString(k), p);
					int result = dialog.open();
					if (result != IDialogConstants.OK_ID) {
						System.exit(0);
					}
				} else {
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
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.titleInvalid"), 
					Messages.getString("license.LicenseManageDialog.infoInvalid"));
		}
	}
	
}
