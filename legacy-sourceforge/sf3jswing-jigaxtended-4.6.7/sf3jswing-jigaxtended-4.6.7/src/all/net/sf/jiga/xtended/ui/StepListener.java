package net.sf.jiga.xtended.ui;
import java.util.EventListener;
/** Listens to a new step change in one Steppable object. Extends EventListener */
public interface StepListener extends EventListener {
    /** Informs of one step change occured */
	public void stepChanged();
}
