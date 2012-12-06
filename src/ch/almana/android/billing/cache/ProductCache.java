package ch.almana.android.billing.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import ch.almana.android.billing.backend.BillingService;
import ch.almana.android.billing.backend.PurchaseListener;
import ch.almana.android.billing.backend.Consts.PurchaseState;
import ch.almana.android.billing.products.Product;

public class ProductCache {

	private final static String TAG = "Billing";
	private static final String PREF_STORE_LOCAL = "local";
	private static final String DB_INITIALIZED = "db_initialized";
	private static Set<PurchaseListener> purchaseListeners = new HashSet<PurchaseListener>();
	private final PurchaseDatabase mPurchaseDatabase;
	private final Map<String, Product> mOwnedItems;
	private final Context ctx;

	public ProductCache(Context ctx) {
		this.ctx = ctx;
		mPurchaseDatabase = new PurchaseDatabase(ctx);
		mOwnedItems = mPurchaseDatabase.getOwnedItems();
	}

	SharedPreferences getPreferences() {
		return ctx.getSharedPreferences(PREF_STORE_LOCAL, 0);
		//		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	/**
	 * If the database has not been initialized, we send a RESTORE_TRANSACTIONS
	 * request to Android Market to get the list of purchased items for this
	 * user. This happens if the application has just been installed or the user
	 * wiped data. We do not want to do this on every startup, rather, we want
	 * to do only when the database needs to be initialized.
	 * 
	 * @param mBillingService
	 */
	public void restoreDatabase(BillingService billingService, boolean force) {
		boolean initialized = getPreferences().getBoolean(DB_INITIALIZED, false);
		if (force || !initialized) {
			billingService.restoreTransactions();
			//debug: Toast.makeText(ctx, R.string.restoring_transactions, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Creates a background thread that reads the database and initializes the
	 * set of owned items.
	 */
	public void initializeOwnedItems() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mOwnedItems.putAll(mPurchaseDatabase.getOwnedItems());
			}
		}).start();
	}

	public Map<String, Product> getOwnedItems() {
		return mOwnedItems;
	}

	public void finish() {
		mPurchaseDatabase.close();
	}

	public void purchasedItem(String itemId, PurchaseState purchaseState, long purchaseTime, String developerPayload) {
		Product product = new Product(itemId);
		int quantity = mPurchaseDatabase.updatePurchase(/*itemId,*/itemId, purchaseState, purchaseTime, developerPayload);
		product.setCount(quantity);
		mOwnedItems.put(itemId, product);
		firePurchasedChanged(itemId, quantity);
	}

	public void setInitalised() {
		// Update the shared preferences so that we don't perform
		// a RestoreTransactions again.
		SharedPreferences.Editor edit = getPreferences().edit();
		edit.putBoolean(DB_INITIALIZED, true);
		edit.commit();
	}

	public Product get(String pid) {
		return mOwnedItems.get(pid);
	}

	private void firePurchasedChanged(String pid, int count) {
		for (PurchaseListener listener : purchaseListeners) {
			listener.purchaseChanged(pid, count);
		}
	}

	public void addPurchaseListener(PurchaseListener listener) {
		purchaseListeners.add(listener);
	}

	public void removePurchaseListener(PurchaseListener listener) {
		purchaseListeners.remove(listener);
	}

	public void fireBillingSupported(boolean supported) {
		for (PurchaseListener listener : purchaseListeners) {
			listener.billingSupported(supported);
		}
	}
}
