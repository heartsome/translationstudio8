package net.heartsome.cat.ts.ui.innertag.tagstyle;

import net.heartsome.cat.ts.ui.innertag.ISegmentViewer;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

public class TagStyleConfigurator {

	public static void configure(TextLayout textLayout) {
		String text = textLayout.getText();
		Document doc = new Document(text);
		ITokenScanner scanner = getRecipeScanner(doc);
		scanner.setRange(doc, 0, doc.getLength());
		IToken token;
		while ((token = scanner.nextToken()) != Token.EOF) {
			int offset = scanner.getTokenOffset();
			int length = scanner.getTokenLength();
			Object data = token.getData();
			if (data != null && data instanceof TextStyle) {
				TextStyle textStyle = (TextStyle) data;
				textLayout.setStyle(textStyle, offset, offset + length - 1);
			}
		}
	}

	public static void configure(ISegmentViewer viewer) {
		getPresentationReconciler(viewer);
	}

	private static IPresentationReconciler getPresentationReconciler(ISegmentViewer viewer) {
		// 构造函数中，已经默认设置 IDocumentExtension3.DEFAULT_PARTITIONING
		PresentationReconciler reconciler = new PresentationReconciler();
		PresentationRepairer repairer = new PresentationRepairer(getRecipeScanner(viewer.getDocument()), viewer);
		reconciler.setRepairer(repairer, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.install(viewer);
		return reconciler;
	}

	private static ITokenScanner getRecipeScanner(final IDocument doc) {
//		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final InnerTagScanner scanner = new InnerTagScanner();
//		store.addPropertyChangeListener(new IPropertyChangeListener() {
//
//			public void propertyChange(PropertyChangeEvent event) {
//				String property = event.getProperty();
//				if (IPreferenceConstants.TAG_FOREGROUND.equals(property)
//						|| IPreferenceConstants.TAG_BACKGROUND.equals(property)) {
//					scanner.updateToken(IPreferenceConstants.TAG_FOREGROUND, IPreferenceConstants.TAG_BACKGROUND);
//
//					if (doc != null) { // 刷新
//						try {
//							doc.replace(doc.getLength(), 0, "");
//						} catch (BadLocationException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		});
		return scanner;
	}
}
