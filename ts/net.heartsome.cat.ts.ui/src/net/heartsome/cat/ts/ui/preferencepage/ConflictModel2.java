package net.heartsome.cat.ts.ui.preferencepage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.internal.keys.model.BindingElement;
import org.eclipse.ui.internal.keys.model.BindingModel;
import org.eclipse.ui.internal.keys.model.CommonModel;
import org.eclipse.ui.internal.keys.model.ConflictModel;
import org.eclipse.ui.internal.keys.model.KeyController;

/**
 * 此类与 org.eclipse.ui.internal.keys.model.ConflictModel 的区别为修改了 updateConflictsFor 方法，用于处理 Bug#2740
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ConflictModel2 extends ConflictModel {

	/**
	 * The set of conflicts for the currently selected element.
	 */
	private Collection conflicts;

	private BindingManager bindingManager;

	private BindingModel bindingModel;

	/**
	 * A mapping of binding element to known conflicts.
	 */
	private Map conflictsMap;

	/**
	 * @param kc
	 */
	public ConflictModel2(KeyController kc) {
		super(kc);
	}

	/**
	 * @return Returns the conflicts.
	 */
	public Collection getConflicts() {
		return conflicts;
	}

	/**
	 * Sets the conflicts to the given collection. Any conflicts in the
	 * collection that do not exist in the <code>bindingModel</code> are
	 * removed.
	 * 
	 * @param conflicts
	 *            The conflicts to set.
	 */
	public void setConflicts(Collection conflicts) {
		Object old = this.conflicts;
		this.conflicts = conflicts;

		if (this.conflicts != null) {
			Iterator i = this.conflicts.iterator();
			Map bindingToElement = bindingModel.getBindingToElement();
			while (i.hasNext()) {
				Object next = i.next();
				if (!bindingToElement.containsValue(next)
						&& !next.equals(getSelectedElement())) {
					i.remove();
				}
			}
		}

		controller.firePropertyChange(this, PROP_CONFLICTS, old, conflicts);
	}

	public void updateConflictsFor(BindingElement source) {
		updateConflictsFor(source, false);
	}

	public void updateConflictsFor(BindingElement oldValue,
			BindingElement newValue) {
		updateConflictsFor(oldValue, newValue, false);
	}

	public void updateConflictsFor(BindingElement source, boolean removal) {
		updateConflictsFor(null, source, removal);
	}

	private void updateConflictsFor(BindingElement oldValue,
			BindingElement newValue, boolean removal) {
		updateConflictsFor(newValue, oldValue == null ? null : oldValue
				.getTrigger(), newValue == null ? null : newValue.getTrigger(),
				removal);
	}

	public void updateConflictsFor(BindingElement newValue,
			TriggerSequence oldTrigger, TriggerSequence newTrigger,
			boolean removal) {
		Collection matches = (Collection) conflictsMap.get(newValue);
		if (matches != null) {
			if (newTrigger == null || removal) {
				// we need to clear this match
				matches.remove(newValue);
				conflictsMap.remove(newValue);
				if (matches == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_REMOVE,
							null, newValue);
				}
				if (matches.size() == 1) {
					BindingElement tbe = (BindingElement) matches.iterator()
							.next();
					conflictsMap.remove(tbe);
					tbe.setConflict(Boolean.FALSE);
					if (matches == conflicts) {
						setConflicts(null);
					}
				}
				return;
			} else if (oldTrigger != null && !newTrigger.equals(oldTrigger)) {
				// we need to clear this match
				matches.remove(newValue);
				conflictsMap.remove(newValue);

				if (matches == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_REMOVE,
							null, newValue);
				}
				if (matches.size() == 1) {
					BindingElement tbe = (BindingElement) matches.iterator()
							.next();
					conflictsMap.remove(tbe);
					tbe.setConflict(Boolean.FALSE);
					if (matches == conflicts) {
						setConflicts(null);
					}
				}
			} else {
				return;
			}
		}

		if (newValue.getTrigger() == null
				|| !(newValue.getModelObject() instanceof Binding)) {
			return;
		}
		Binding binding = (Binding) newValue.getModelObject();
		TriggerSequence trigger = binding.getTriggerSequence();

		matches = (Collection) bindingManager
				.getActiveBindingsDisregardingContext().get(trigger);
		ArrayList localConflicts = new ArrayList();
		if (matches != null) {
			localConflicts.add(newValue);
			Iterator i = matches.iterator();
			while (i.hasNext()) {
				Binding b = (Binding) i.next();
//				if (binding != b
//						&& b.getContextId().equals(binding.getContextId())
//						&& b.getSchemeId().equals(binding.getSchemeId())) {
//					Object element = bindingModel.getBindingToElement().get(b);
//					if (element != null) {
//						localConflicts.add(element);
//					}
//				}
//				Bug #2740 快捷键--快捷键设置问题：修改验证冲突快捷键的方法，使用以下的方式，原来使用的是上面注释的方式
				if (binding != b
						&& !b.getParameterizedCommand().getCommand().toString().equals(binding.getParameterizedCommand().getCommand().toString())) {
					Object element = bindingModel.getBindingToElement().get(b);
					if (element != null) {
						localConflicts.add(element);
					}
				}
			}
		}

		if (localConflicts.size() > 1) {
			// first find if it is already a conflict collection
			Collection knownConflicts = null;
			Iterator i = localConflicts.iterator();
			while (i.hasNext() && knownConflicts == null) {
				BindingElement tbe = (BindingElement) i.next();
				knownConflicts = (Collection) conflictsMap.get(tbe);
			}
			if (knownConflicts != null) {
				knownConflicts.add(newValue);
				conflictsMap.put(newValue, knownConflicts);
				newValue.setConflict(Boolean.TRUE);
				if (knownConflicts == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_ADD,
							null, newValue);
				} else if (newValue == getSelectedElement()) {
					setConflicts(knownConflicts);
				}
				return;
			}
			boolean isSelected = false;
			i = localConflicts.iterator();
			while (i.hasNext()) {
				BindingElement tbe = (BindingElement) i.next();
				if (tbe != null) {
					conflictsMap.put(tbe, localConflicts);
					tbe.setConflict(Boolean.TRUE);
				}
				if (tbe == getSelectedElement()) {
					isSelected = true;
				}
			}
			if (isSelected) {
				setConflicts(localConflicts);
			}
		}
	}

	public void init(BindingManager manager, BindingModel model) {
		bindingManager = manager;
		bindingModel = model;
		conflictsMap = new HashMap();
		Iterator i = bindingModel.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getModelObject() instanceof Binding) {
				updateConflictsFor(be);
			}
		}
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == ConflictModel2.this
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					if (event.getNewValue() != null) {
						updateConflictsFor(
								(BindingElement) event.getOldValue(),
								(BindingElement) event.getNewValue());
						setConflicts((Collection) conflictsMap.get(event
								.getNewValue()));
					} else {
						setConflicts(null);
					}
				} else if (BindingModel.PROP_BINDING_REMOVE.equals(event
						.getProperty())) {
					updateConflictsFor((BindingElement) event.getOldValue(),
							(BindingElement) event.getNewValue(), true);
				}
			}
		});
	}
}
