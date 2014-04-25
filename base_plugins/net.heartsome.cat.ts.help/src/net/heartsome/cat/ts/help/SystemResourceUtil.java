package net.heartsome.cat.ts.help;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;

import net.heartsome.license.LicenseAgreementDialog;
import net.heartsome.license.LicenseManageDialog;
import net.heartsome.license.LocalAuthorizationValidator;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.utils.DateUtils;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.webservice.ServiceUtil;

public class SystemResourceUtil {
	
	public static String dateTemp;
	 
	private static LocalAuthorizationValidator v = new LocalAuthorizationValidator();
	private static int re;
	private static boolean isExsit = false;
	
//	public static void beforeload() {
//		isExsit = FileUtils.isExsit();
//		if (isExsit) {
//			re = v.checkLicense();
//		}
//	}
	
//	public static void load(boolean isShow) {
//		if (isExsit) {
//			if (Constants.STATE_VALID == re) {
//				try {
//					final int ret = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
//					final String date = ServiceUtil.getTempEndDate(v.getLicenseId());
//					if (Constants.STATE_VALID == ret) {
//						if (isShow) {
//							Display.getDefault().asyncExec(new Runnable() { 
//								
//								public void run() {
//									LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_VALID, date, v.getLicenseId());
//									dialog.open();
//								}
//							});
//						}
//					} else if (ret == Constants.STATE_INVALID) {
//						FileUtils.removeFile();
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_INVALID, null, null);
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    } 
//							}
//						});
//					} else if (ret == Constants.STATE_EXPIRED) {
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_EXPIRED, date, null);
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    } 
//							}
//						});
//					} else if (Constants.EXCEPTION_INT15 == ret) {
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT15, null, null);
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    }
//							}
//						});
//					} else {
//						Display.getDefault().asyncExec(new Runnable() { 
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), ret, null, v.getLicenseId());
//								dialog.open();
//							    System.exit(0);
//							}
//						});
//					} 
//				} catch (Exception e) {
//					e.printStackTrace();
//					if (v.isTrial()) {
//						Throwable t = e;
//						while(t.getCause() != null) {
//							t = t.getCause();
//						}
//						if (t instanceof java.security.cert.CertificateException) {
//							Display.getDefault().asyncExec(new Runnable() { 
//								
//								public void run() {
//									LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT16, null, v.getLicenseId());
//									int result = dialog.open();
//									if (result != IDialogConstants.OK_ID) {
//										System.exit(0);
//									} 
//								}
//							});
//						} else {
//							Display.getDefault().asyncExec(new Runnable() { 
//								
//								public void run() {
//									LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT17, null, v.getLicenseId());
//									int result = dialog.open();
//									if (result != IDialogConstants.OK_ID) {
//										System.exit(0);
//									} 
//								}
//							});
//						}
//					} else {
//						if (isShow) {
//							Display.getDefault().asyncExec(new Runnable() { 
//								
//								public void run() {
//									LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_VALID, null, v.getLicenseId());
//									dialog.open();
//								}
//							});
//						}
//					}
//				}
//			} else if (Constants.STATE_INVALID == re) {
//				FileUtils.removeFile();
//				Display.getDefault().asyncExec(new Runnable() { 
//					
//					public void run() {
//						LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_INVALID, null, null);
//						int result = dialog.open();
//					    if (result != IDialogConstants.OK_ID) {
//					    	System.exit(0);
//					    }
//					}
//				});
//			} else if (Constants.EXCEPTION_INT15 == re
//					|| Constants.EXCEPTION_INT1 == re || Constants.EXCEPTION_INT2 == re
//					|| Constants.EXCEPTION_INT3 == re || Constants.EXCEPTION_INT4 == re) {
//				Display.getDefault().asyncExec(new Runnable() { 
//					
//					public void run() {
//						LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, null);
//						int result = dialog.open();
//					    if (result != IDialogConstants.OK_ID) {
//					    	System.exit(0);
//					    }
//					}
//				});
//			} else if (Constants.EXCEPTION_INT14 == re) {
//				Display.getDefault().asyncExec(new Runnable() { 
//					
//					public void run() {
//						LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, v.getLicenseId());
//						int result = dialog.open();
//					    if (result != IDialogConstants.OK_ID) {
//					    	System.exit(0);
//					    }
//					}
//				});
//			} else {
//				Display.getDefault().asyncExec(new Runnable() { 
//					public void run() {
//						LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, v.getLicenseId());
//						dialog.open();
//					    System.exit(0);
//					}
//				});
//			} 
//		} else {
//			Display.getDefault().asyncExec(new Runnable() { 
//				
//				public void run() {
//					LicenseAgreementDialog dialog = new LicenseAgreementDialog(Display.getDefault().getActiveShell());
//				    int result = dialog.open();
//				    if (result != IDialogConstants.OK_ID) {
//				    	System.exit(0);
//				    }
//				}
//			});
//		}
//	}
	
//	public static void load() {
//		new Thread(new Runnable() {
//			public void run() {
//				if (DateUtils.getDate().equals(dateTemp)) {
//					return;
//				} else {
//					dateTemp = DateUtils.getDate();
//				}
//
//				if (FileUtils.isExsit()) {
//					final LocalAuthorizationValidator v = new LocalAuthorizationValidator();
//					final int re = v.checkLicense();
//					if (Constants.STATE_VALID == re) {
//						try {
//							final int ret = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
//							final String date = ServiceUtil.getTempEndDate(v.getLicenseId());
//							if (Constants.STATE_VALID == ret) {	
//							} else if (ret == Constants.STATE_INVALID) {
//								FileUtils.removeFile();
//								Display.getDefault().asyncExec(new Runnable() { 
//									
//									public void run() {
//										LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_INVALID, null, null);
//										int result = dialog.open();
//									    if (result != IDialogConstants.OK_ID) {
//									    	System.exit(0);
//									    } 
//									}
//								});
//							} else if (ret == Constants.STATE_EXPIRED) {
//								Display.getDefault().asyncExec(new Runnable() { 
//									
//									public void run() {
//										LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_EXPIRED, date, null);
//										int result = dialog.open();
//									    if (result != IDialogConstants.OK_ID) {
//									    	System.exit(0);
//									    } 
//									}
//								});
//							} else if (Constants.EXCEPTION_INT15 == ret) {
//								Display.getDefault().asyncExec(new Runnable() { 
//									
//									public void run() {
//										LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT15, null, null);
//										int result = dialog.open();
//									    if (result != IDialogConstants.OK_ID) {
//									    	System.exit(0);
//									    }
//									}
//								});
//							} else {
//								Display.getDefault().asyncExec(new Runnable() { 
//									public void run() {
//										LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), ret, null, v.getLicenseId());
//										dialog.open();
//									    System.exit(0);
//									}
//								});
//							} 
//						} catch (Exception e) {
//							e.printStackTrace();
//							if (v.isTrial()) {
//								Throwable t = e;
//								while(t.getCause() != null) {
//									t = t.getCause();
//								}
//								if (t instanceof java.security.cert.CertificateException) {
//									Display.getDefault().asyncExec(new Runnable() { 
//										
//										public void run() {
//											LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT16, null, v.getLicenseId());
//											int result = dialog.open();
//											if (result != IDialogConstants.OK_ID) {
//												System.exit(0);
//											} 
//										}
//									});
//								} else {
//									Display.getDefault().asyncExec(new Runnable() { 
//										
//										public void run() {
//											LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.EXCEPTION_INT17, null, v.getLicenseId());
//											int result = dialog.open();
//											if (result != IDialogConstants.OK_ID) {
//												System.exit(0);
//											} 
//										}
//									});
//								}
//							} 
//						}
//					} else if (Constants.STATE_INVALID == re) {
//						FileUtils.removeFile();
//						Display.getDefault().asyncExec(new Runnable() { 
//
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_INVALID, null, null);
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    }
//							}
//						});
//					} else if (Constants.EXCEPTION_INT15 == re
//							|| Constants.EXCEPTION_INT1 == re || Constants.EXCEPTION_INT2 == re
//							|| Constants.EXCEPTION_INT3 == re || Constants.EXCEPTION_INT4 == re) {
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, null);
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    }
//							}
//						});
//					} else if (Constants.EXCEPTION_INT14 == re) {
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, v.getLicenseId());
//								int result = dialog.open();
//							    if (result != IDialogConstants.OK_ID) {
//							    	System.exit(0);
//							    }
//							}
//						});
//					} else {
//						Display.getDefault().asyncExec(new Runnable() { 
//							
//							public void run() {
//								LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, v.getLicenseId());
//								dialog.open();
//							    System.exit(0);
//							}
//						});
//					} 
//				} else {
//					Display.getDefault().asyncExec(new Runnable() { 
//
//						public void run() {
//							LicenseAgreementDialog dialog = new LicenseAgreementDialog(Display.getDefault().getActiveShell());
//						    int result = dialog.open();
//						    if (result != IDialogConstants.OK_ID) {
//						    	System.exit(0);
//						    }
//						}
//					});
//				}
//			}	
//		}).start();	
		
//	}
	
//	public static String[] load(IProgressMonitor monitor) {
//		String[] str = new String[3];
//		if (monitor != null) {
//			if (monitor.isCanceled()) {
//				str[0] = String.valueOf(Constants.CANCEL);
//				return str;
//			}
//			monitor.worked(1);
//		}
//
//		if (FileUtils.isExsit()) {
//			if (monitor != null) {
//				if (monitor.isCanceled()) {
//					str[0] = String.valueOf(Constants.CANCEL);
//					return str;
//				}
//				monitor.worked(1);
//			}
//			LocalAuthorizationValidator v = new LocalAuthorizationValidator();
//			int re = v.checkLicense();
//			if (monitor != null) {
//				if (monitor.isCanceled()) {
//					str[0] = String.valueOf(Constants.CANCEL);
//					return str;
//				}
//				monitor.worked(2);
//			}
//			if (Constants.STATE_VALID == re) {
//				try {
//					str[2] = v.getLicenseId();
//					int ret = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
//					str[0] = String.valueOf(ret);
//					if (monitor != null) {
//						if (monitor.isCanceled()) {
//							str[0] = String.valueOf(Constants.CANCEL);
//							return str;
//						}
//						monitor.worked(2);
//					}
//					String date = ServiceUtil.getTempEndDate(v.getLicenseId());
//					if (monitor != null) {
//						if (monitor.isCanceled()) {
//							str[0] = String.valueOf(Constants.CANCEL);
//							return str;
//						}
//						monitor.worked(2);
//					}
//					str[1] = date;
//					return str;
//				} catch (Exception e) {
//					e.printStackTrace();
//					if (v.isTrial()) {
//						if (monitor != null) {
//							if (monitor.isCanceled()) {
//								str[0] = String.valueOf(Constants.CANCEL);
//								return str;
//							}
//							monitor.worked(1);
//						}
//						Throwable t = e;
//						while(t.getCause() != null) {
//							t = t.getCause();
//						}
//						
//						if (t instanceof java.security.cert.CertificateException) {
//							str[0] = String.valueOf(Constants.EXCEPTION_INT16);
//						} else {
//							str[0] = String.valueOf(Constants.EXCEPTION_INT17);
//						}
//						return str;
//					} else {
//						if (monitor != null) {
//							if (monitor.isCanceled()) {
//								str[0] = String.valueOf(Constants.CANCEL);
//								return str;
//							}
//							monitor.worked(1);
//						}
//						str[0] = String.valueOf(Constants.STATE_VALID);
//						return str;
//					}
//				}
//			} else if (Constants.STATE_INVALID == re) {
//				if (monitor != null) {
//					if (monitor.isCanceled()) {
//						str[0] = String.valueOf(Constants.CANCEL);
//						return str;
//					}
//					monitor.worked(1);
//				}
//				str[0] = String.valueOf(Constants.STATE_INVALID);
//				return str;
//			} else { 
//				if (monitor != null) {
//					if (monitor.isCanceled()) {
//						str[0] = String.valueOf(Constants.CANCEL);
//						return str;
//					}
//					monitor.worked(1);
//				}
//				str[2] = v.getLicenseId();
//				str[0] = String.valueOf(re);
//				return str;
//			} 
//		} else {
//			if (monitor != null) {
//				if (monitor.isCanceled()) {
//					str[0] = String.valueOf(Constants.CANCEL);
//					return str;
//				}
//				monitor.worked(1);
//			}
//			
//			str[0] = String.valueOf(Constants.STATE_FILE_NOT_EXSIT);
//			return str;
//		}
//	}
	
//	public static void showDialog(String[] str) {
//		int re = Integer.parseInt(str[0]);
//		if (Constants.STATE_VALID == re) {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_VALID, str[1], str[2]);
//			dialog.open();
//		} else if (Constants.STATE_INVALID == re) {
//			FileUtils.removeFile();
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_INVALID, null, null);
//			int result = dialog.open();
//		    if (result != IDialogConstants.OK_ID) {
//		    	System.exit(0);
//		    } 
//		} else if (Constants.STATE_EXPIRED == re) {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), Constants.STATE_EXPIRED, str[1], null);
//			int result = dialog.open();
//		    if (result != IDialogConstants.OK_ID) {
//		    	System.exit(0);
//		    } 
//		} else if (Constants.EXCEPTION_INT16 == re || Constants.EXCEPTION_INT17 == re) {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, str[2]);
//			int result = dialog.open();
//			if (result != IDialogConstants.OK_ID) {
//				System.exit(0);
//			} 
//		} else if (Constants.CANCEL == re) {
//			return;
//		} else if (Constants.STATE_FILE_NOT_EXSIT == re) {
//			LicenseAgreementDialog dialog = new LicenseAgreementDialog(Display.getDefault().getActiveShell());
//		    int result = dialog.open();
//		    if (result != IDialogConstants.OK_ID) {
//		    	System.exit(0);
//		    } 
//		} else if (Constants.EXCEPTION_INT15 == re
//				|| Constants.EXCEPTION_INT1 == re || Constants.EXCEPTION_INT2 == re
//				|| Constants.EXCEPTION_INT3 == re || Constants.EXCEPTION_INT4 == re) {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, null);
//			int result = dialog.open();
//		    if (result != IDialogConstants.OK_ID) {
//		    	System.exit(0);
//		    }
//		} else if (Constants.EXCEPTION_INT14 == re) {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, str[2]);
//			int result = dialog.open();
//		    if (result != IDialogConstants.OK_ID) {
//		    	System.exit(0);
//		    }
//		} else {
//			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, str[2]);
//			dialog.open();
//			System.exit(0);
//		} 
//	}
}
