package ch.almana.android.billing.backend;

import java.util.Map;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;
import ch.almana.android.billing.backend.Consts.PurchaseState;
import ch.almana.android.billing.cache.DatabasePurchaseObserver;
import ch.almana.android.billing.cache.ProductCache;
import ch.almana.android.billing.products.Product;
import ch.almana.android.util.Debug;

public class BillingManager {

	/** An array of product list entries for the products that can be purchased. */
	public enum Managed {
		MANAGED, UNMANAGED
	}

	private final BillingService mBillingService;
	private final ProductCache productCache;
	private final Handler mHandler;
	private DatabasePurchaseObserver databasePurchaseObserver;
	private final Activity act;

	public BillingManager(Activity act) {
		super();
		mHandler = new Handler();
		this.act = act;
		mBillingService = new BillingService();
		mBillingService.setContext(act.getApplicationContext());

		productCache = new ProductCache(act.getApplicationContext());
		productCache.restoreDatabase(mBillingService, false);
		checkBillingSupported();
		registerReceiver();
	}

	public void release() {
		unregisterReceiver();
		productCache.finish();
		mBillingService.stopSelf();
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public void restoreTransactionsFromMarket() {
		productCache.restoreDatabase(mBillingService, true);
	}

	public int getCountOfProduct(String pid) {
		Product product = productCache.get(pid);
		if (product == null) {
			return 0;
		}
		return product.getCount();
	}

	public boolean hasProduct(String pid) {
		return getCountOfProduct(pid) > 0;
	}

	public void setPurchaseObserver(PurchaseObserver purchaseObserver) {
		ResponseHandler.register(purchaseObserver);
	}

	private void checkBillingSupported() {
		mBillingService.checkBillingSupported();
	}

	public boolean requestPurchase(String mSku) {
		if (Debug.isUnsinedPackage(act)) {
			productCache.setInitalised();
			productCache.purchasedItem(mSku, PurchaseState.PURCHASED, System.currentTimeMillis(), "");
			Toast.makeText(act, "Dummy purchase!", Toast.LENGTH_LONG).show();
			return true;
		}
		return mBillingService.requestPurchase(mSku, null);
	}


	public Map<String, Product> getOwnedItems() {
		return productCache.getOwnedItems();
	}

	private void registerReceiver() {
		if (databasePurchaseObserver == null) {
			databasePurchaseObserver = new DatabasePurchaseObserver(act, mHandler, productCache);
			ResponseHandler.register(databasePurchaseObserver);
		}
	}

	private void unregisterReceiver() {
		if (databasePurchaseObserver != null) {
			ResponseHandler.unregister(databasePurchaseObserver);
			databasePurchaseObserver = null;
		}
	}

	public void addPurchaseListener(PurchaseListener listener) {
		productCache.addPurchaseListener(listener);
	}

	public void removePurchaseListener(PurchaseListener listener) {
		productCache.removePurchaseListener(listener);
	}

}
