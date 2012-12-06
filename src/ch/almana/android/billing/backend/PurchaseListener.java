package ch.almana.android.billing.backend;

public interface PurchaseListener {

	void purchaseChanged(String pid, int count);

	void billingSupported(boolean supported);

}
