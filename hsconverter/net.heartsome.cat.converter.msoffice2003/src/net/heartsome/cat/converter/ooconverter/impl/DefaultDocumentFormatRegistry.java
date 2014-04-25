/**
 * DefaultDocumentFormatRegistry.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter.impl;

/**
 * The Class DefaultDocumentFormatRegistry.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class DefaultDocumentFormatRegistry extends BasicDocumentFormatRegistry {

	/**
	 * Instantiates a new default document format registry.
	 */
	public DefaultDocumentFormatRegistry() {
		final DocumentFormat pdf = new DocumentFormat("Portable Document Format", "application/pdf", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pdf.setExportFilter(DocumentFamily.DRAWING, "draw_pdf_Export"); //$NON-NLS-1$
		pdf.setExportFilter(DocumentFamily.PRESENTATION, "impress_pdf_Export"); //$NON-NLS-1$
		pdf.setExportFilter(DocumentFamily.SPREADSHEET, "calc_pdf_Export"); //$NON-NLS-1$
		pdf.setExportFilter(DocumentFamily.TEXT, "writer_pdf_Export"); //$NON-NLS-1$
		addDocumentFormat(pdf);

		final DocumentFormat swf = new DocumentFormat("Macromedia Flash", "application/x-shockwave-flash", "swf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		swf.setExportFilter(DocumentFamily.DRAWING, "draw_flash_Export"); //$NON-NLS-1$
		swf.setExportFilter(DocumentFamily.PRESENTATION, "impress_flash_Export"); //$NON-NLS-1$
		addDocumentFormat(swf);

		final DocumentFormat xhtml = new DocumentFormat("XHTML", "application/xhtml+xml", "xhtml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		xhtml.setExportFilter(DocumentFamily.PRESENTATION, "XHTML Impress File"); //$NON-NLS-1$
		xhtml.setExportFilter(DocumentFamily.SPREADSHEET, "XHTML Calc File"); //$NON-NLS-1$
		xhtml.setExportFilter(DocumentFamily.TEXT, "XHTML Writer File"); //$NON-NLS-1$
		addDocumentFormat(xhtml);

		// HTML is treated as Text when supplied as input, but as an output it
		// is also
		// available for exporting Spreadsheet and Presentation formats
		final DocumentFormat html = new DocumentFormat("HTML", DocumentFamily.TEXT, "text/html", "html"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		html.setExportFilter(DocumentFamily.PRESENTATION, "impress_html_Export"); //$NON-NLS-1$
		html.setExportFilter(DocumentFamily.SPREADSHEET, "HTML (StarCalc)"); //$NON-NLS-1$
		html.setExportFilter(DocumentFamily.TEXT, "HTML (StarWriter)"); //$NON-NLS-1$
		addDocumentFormat(html);

		final DocumentFormat odt = new DocumentFormat(
				"OpenDocument Text", DocumentFamily.TEXT, "application/vnd.oasis.opendocument.text", "odt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		odt.setExportFilter(DocumentFamily.TEXT, "writer8"); //$NON-NLS-1$
		addDocumentFormat(odt);

		final DocumentFormat sxw = new DocumentFormat(
				"OpenOffice.org 1.0 Text Document", DocumentFamily.TEXT, "application/vnd.sun.xml.writer", "sxw"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sxw.setExportFilter(DocumentFamily.TEXT, "StarOffice XML (Writer)"); //$NON-NLS-1$
		addDocumentFormat(sxw);

		final DocumentFormat doc = new DocumentFormat(
				"Microsoft Word", DocumentFamily.TEXT, "application/msword", "doc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		doc.setExportFilter(DocumentFamily.TEXT, "MS Word 97"); //$NON-NLS-1$
		addDocumentFormat(doc);

		final DocumentFormat rtf = new DocumentFormat("Rich Text Format", DocumentFamily.TEXT, "text/rtf", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rtf.setExportFilter(DocumentFamily.TEXT, "Rich Text Format"); //$NON-NLS-1$
		addDocumentFormat(rtf);

		final DocumentFormat wpd = new DocumentFormat(
				"WordPerfect", DocumentFamily.TEXT, "application/wordperfect", "wpd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addDocumentFormat(wpd);

		final DocumentFormat txt = new DocumentFormat("Plain Text", DocumentFamily.TEXT, "text/plain", "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// set FilterName to "Text" to prevent OOo from tryign to display the
		// "ASCII Filter Options" dialog
		// alternatively FilterName could be "Text (encoded)" and FilterOptions
		// used to set encoding if needed
		txt.setImportOption("FilterName", "Text"); //$NON-NLS-1$ //$NON-NLS-2$
		txt.setExportFilter(DocumentFamily.TEXT, "Text"); //$NON-NLS-1$
		addDocumentFormat(txt);

		final DocumentFormat wikitext = new DocumentFormat("MediaWiki wikitext", "text/x-wiki", "wiki"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		wikitext.setExportFilter(DocumentFamily.TEXT, "MediaWiki"); //$NON-NLS-1$
		addDocumentFormat(wikitext);

		final DocumentFormat ods = new DocumentFormat(
				"OpenDocument Spreadsheet", DocumentFamily.SPREADSHEET, "application/vnd.oasis.opendocument.spreadsheet", "ods"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ods.setExportFilter(DocumentFamily.SPREADSHEET, "calc8"); //$NON-NLS-1$
		addDocumentFormat(ods);

		final DocumentFormat sxc = new DocumentFormat(
				"OpenOffice.org 1.0 Spreadsheet", DocumentFamily.SPREADSHEET, "application/vnd.sun.xml.calc", "sxc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sxc.setExportFilter(DocumentFamily.SPREADSHEET, "StarOffice XML (Calc)"); //$NON-NLS-1$
		addDocumentFormat(sxc);

		final DocumentFormat xls = new DocumentFormat(
				"Microsoft Excel", DocumentFamily.SPREADSHEET, "application/vnd.ms-excel", "xls"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		xls.setExportFilter(DocumentFamily.SPREADSHEET, "MS Excel 97"); //$NON-NLS-1$
		addDocumentFormat(xls);

		final DocumentFormat csv = new DocumentFormat("CSV", DocumentFamily.SPREADSHEET, "text/csv", "csv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		csv.setImportOption("FilterName", "Text - txt - csv (StarCalc)"); //$NON-NLS-1$ //$NON-NLS-2$
		csv.setImportOption("FilterOptions", "44,34,0"); // Field Separator: ','; Text Delimiter: '"'   //$NON-NLS-1$ //$NON-NLS-2$
		csv.setExportFilter(DocumentFamily.SPREADSHEET, "Text - txt - csv (StarCalc)"); //$NON-NLS-1$
		csv.setExportOption(DocumentFamily.SPREADSHEET, "FilterOptions", "44,34,0"); //$NON-NLS-1$ //$NON-NLS-2$
		addDocumentFormat(csv);

		final DocumentFormat tsv = new DocumentFormat(
				"Tab-separated Values", DocumentFamily.SPREADSHEET, "text/tab-separated-values", "tsv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tsv.setImportOption("FilterName", "Text - txt - csv (StarCalc)"); //$NON-NLS-1$ //$NON-NLS-2$
		tsv.setImportOption("FilterOptions", "9,34,0"); // Field Separator: '\t'; Text Delimiter: '"' //$NON-NLS-1$ //$NON-NLS-2$
		tsv.setExportFilter(DocumentFamily.SPREADSHEET, "Text - txt - csv (StarCalc)"); //$NON-NLS-1$
		tsv.setExportOption(DocumentFamily.SPREADSHEET, "FilterOptions", "9,34,0"); //$NON-NLS-1$ //$NON-NLS-2$
		addDocumentFormat(tsv);

		final DocumentFormat odp = new DocumentFormat(
				"OpenDocument Presentation", DocumentFamily.PRESENTATION, "application/vnd.oasis.opendocument.presentation", "odp"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		odp.setExportFilter(DocumentFamily.PRESENTATION, "impress8"); //$NON-NLS-1$
		addDocumentFormat(odp);

		final DocumentFormat sxi = new DocumentFormat(
				"OpenOffice.org 1.0 Presentation", DocumentFamily.PRESENTATION, "application/vnd.sun.xml.impress", "sxi"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sxi.setExportFilter(DocumentFamily.PRESENTATION, "StarOffice XML (Impress)"); //$NON-NLS-1$
		addDocumentFormat(sxi);

		final DocumentFormat ppt = new DocumentFormat(
				"Microsoft PowerPoint", DocumentFamily.PRESENTATION, "application/vnd.ms-powerpoint", "ppt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ppt.setExportFilter(DocumentFamily.PRESENTATION, "MS PowerPoint 97"); //$NON-NLS-1$
		addDocumentFormat(ppt);

		final DocumentFormat odg = new DocumentFormat(
				"OpenDocument Drawing", DocumentFamily.DRAWING, "application/vnd.oasis.opendocument.graphics", "odg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		odg.setExportFilter(DocumentFamily.DRAWING, "draw8"); //$NON-NLS-1$
		addDocumentFormat(odg);

		final DocumentFormat svg = new DocumentFormat("Scalable Vector Graphics", "image/svg+xml", "svg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		svg.setExportFilter(DocumentFamily.DRAWING, "draw_svg_Export"); //$NON-NLS-1$
		addDocumentFormat(svg);
	}
}
