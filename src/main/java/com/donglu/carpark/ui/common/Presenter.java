package com.donglu.carpark.ui.common;


import org.eclipse.swt.widgets.Composite;

public interface Presenter {
	void go(Composite c);
	default Object getModel(){return null;}
	default Composite getViewComposite(){return null;}; 
}
