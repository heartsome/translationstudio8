/*
 * Created on 01-dic-2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.heartsome.cat.ts.ui.plugin.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Gonzalo
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ProgressDialog  {

    public static short NO_BARS = 0;

    public static short SINGLE_BAR = 1;

    public static short DOUBLE_BARS = 2;
    
    public static short TRIPLE_BARS = 3;

    Shell shell;

    Shell proShell;

    private Label mainLabel;
    private ProgressBar mainBar;

    private Label progressLabel;
    private ProgressBar progressBar;

    private Label thirdLabel;
    private ProgressBar thirdBar;
    
    private Display display;
    
    private Label titleLabel;

    public ProgressDialog(Shell currentShell, String title,
            String progressMessage, short style) {

        display = currentShell.getDisplay();

        proShell = new Shell(display, SWT.BORDER | SWT.APPLICATION_MODAL | SWT.NO_TRIM);
        proShell.setCursor(new Cursor(proShell.getDisplay(), SWT.CURSOR_WAIT));
        proShell.setLayout(new GridLayout());
		Point location = currentShell.getLocation();
		Point size = currentShell.getSize();
		location.x = location.x + size.x/3;
		location.y = location.y + size.y/3;
        proShell.setLocation(location);
		
        shell = currentShell;
        shell.setCursor(new Cursor(proShell.getDisplay(), SWT.CURSOR_WAIT));
        
        Composite holder = new Composite(proShell,SWT.BORDER);
        holder.setLayout(new GridLayout());
        holder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
        
        titleLabel = new Label(holder, SWT.BOLD);
        titleLabel.setText(title);
        titleLabel.setBackground(new Color(display,0x52,0x81,0x83)); // dull green
        titleLabel.setForeground(new Color(display,0xFF,0xFF,0xFF)); // white
        titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));

        if (style > DOUBLE_BARS) {
            thirdLabel = new Label(holder, SWT.NONE);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.FILL_HORIZONTAL);
            data.widthHint = 200;
            thirdLabel.setLayoutData(data);
            thirdLabel.setText(progressMessage);

            thirdBar = new ProgressBar(holder, SWT.NONE);
            data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.FILL_HORIZONTAL);
            data.widthHint = 200;
            thirdBar.setLayoutData(data);
            thirdBar.setMinimum(0);
            thirdBar.setMaximum(100);
        }
        
        if (style > SINGLE_BAR) {
            mainLabel = new Label(holder, SWT.NONE);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.FILL_HORIZONTAL);
            data.widthHint = 200;
            mainLabel.setLayoutData(data);
            if (style == 2){
            	mainLabel.setText(progressMessage);
            }

            mainBar = new ProgressBar(holder, SWT.NONE);
            data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.FILL_HORIZONTAL);
            data.widthHint = 200;
            mainBar.setLayoutData(data);
            mainBar.setMinimum(0);
            mainBar.setMaximum(100);
        }
        
        progressLabel = new Label(holder, SWT.NONE);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL);
        data.widthHint = 200;
        progressLabel.setLayoutData(data);
        progressLabel.setText(progressMessage);

        if ( style != NO_BARS) {
            progressBar = new ProgressBar(holder, SWT.NONE);
            data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
            data.widthHint = 200;
            progressBar.setLayoutData(data);
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
        }
        proShell.pack();
    }

    public void open() {
        proShell.open();
		display.update();
	}

    public void updateMain(int value) {
        mainBar.setSelection(value);
    	mainBar.setToolTipText(Integer.toString(mainBar.getSelection()) + "%"); //$NON-NLS-1$
		display.update();
        while (display.readAndDispatch()) {
            // do nothing
        }
    }

    public void updateProgress(int value) {
        progressBar.setSelection(value);
    	progressBar.setToolTipText(Integer.toString(progressBar.getSelection()) + "%");         //$NON-NLS-1$
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }

    public void updateProgressMessage(String message) {
        progressLabel.setText(message);
        shell.layout();
        display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }

    public void updateThirdProgress(int value) {
        thirdBar.setSelection(value);
    	thirdBar.setToolTipText(Integer.toString(thirdBar.getSelection()) + "%");         //$NON-NLS-1$
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }

    public void updateThirdMessage(String message) {
        thirdLabel.setText(message);
        shell.layout();
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }
    
    public void updateMainMessage(String message) {
        mainLabel.setText(message);
        shell.layout();
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }
    
    public void updateTitle(String message) {
        titleLabel.setText(message);
        shell.layout();
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }
    
    public void updateProgress(int value, String message) {
        progressBar.setSelection(value);
        progressLabel.setText(message);
		display.update();
        while (display.readAndDispatch()) {
        	// do nothing
        }
    }


    public void showFinish(String finishMessage) {
        progressLabel.setText(finishMessage);
        shell.layout();
		display.update();
        shell.setCursor(new Cursor(proShell.getDisplay(), SWT.CURSOR_ARROW));
        proShell.setCursor(new Cursor(proShell.getDisplay(), SWT.CURSOR_ARROW));
    }

    public void close() {
        shell.setCursor(new Cursor(proShell.getDisplay(), SWT.CURSOR_ARROW));
        proShell.close();        
        proShell.dispose();
    }
	
	public boolean isDisposed() {
		return proShell.isDisposed();
	}

	public Shell getShell() {
		return proShell;
	}
}