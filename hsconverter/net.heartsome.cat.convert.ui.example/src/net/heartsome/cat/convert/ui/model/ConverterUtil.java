package net.heartsome.cat.convert.ui.model;

import net.heartsome.cat.converter.util.ConverterBean;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ConverterUtil {
	private ConverterUtil() {
		// TODO Auto-generated constructor stub
	}

	public static void bindValue(DataBindingContext context,ComboViewer comboViewer,
			ConverterViewModel model) {
//		ViewerSupport.bind(comboViewer, BeansObservables.observeList(
//				model, "supportTypes", String.class),
//				Properties.selfValue(String.class));
//		
//
//		context.bindValue(ViewersObservables
//				.observeSingleSelection(comboViewer), BeansObservables
//				.observeValue(model,
//						"selectedType"));
		
//		ObservableListContentProvider viewerContentProvider=new ObservableListContentProvider();
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setComparator(new ViewerComparator());
//		IObservableMap[] attributeMaps = BeansObservables.observeMaps(
//				viewerContentProvider.getKnownElements(),
//				ConverterBean.class, new String[] { "description" });
//		comboViewer.setLabelProvider(new ObservableMapLabelProvider(
//		attributeMaps));
//		comboViewer.setInput(Observables.staticObservableList(model.getSupportTypes(),ConverterBean.class));
		
		comboViewer.setInput(model.getSupportTypes());
		IViewerObservableValue selection=ViewersObservables.observeSingleSelection(comboViewer);
		IObservableValue observableValue=BeansObservables.observeDetailValue(selection, "name", ConverterBean.class);
		context.bindValue(observableValue, BeansObservables
				.observeValue(model,
						"selectedType"));
	}
}
