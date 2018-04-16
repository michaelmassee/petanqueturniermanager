/**
* Erstellung : 05.04.2018 / Michael Massee
**/

package de.petanqueturniermanager.helper.sheet;

import static com.google.common.base.Preconditions.*;

import java.lang.ref.WeakReference;

public class WeakRefHelper<T> {

	private final WeakReference<T> wkRef;

	public WeakRefHelper(T referent) {
		checkNotNull(referent);
		this.wkRef = new WeakReference<T>(referent);
	}

	public WeakRefHelper(WeakReference<T> wkRef) {
		checkNotNull(wkRef);
		this.wkRef = wkRef;
	}

	public final T getObject() {
		if (!this.wkRef.isEnqueued()) {
			return this.wkRef.get();
		}
		// darf nicht passieren
		throw new NullPointerException("Weakref is Null");
	}
}