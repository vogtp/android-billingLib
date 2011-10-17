package ch.almana.android.billing;

public interface PurchaseListener {

	void purchaseChanged(String pid, int count);

}
