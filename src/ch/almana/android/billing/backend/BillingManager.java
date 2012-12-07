package ch.almana.android.billing.backend;

import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import ch.almana.android.billing.backend.Consts.PurchaseState;
import ch.almana.android.billing.cache.DatabasePurchaseObserver;
import ch.almana.android.billing.cache.ProductCache;
import ch.almana.android.billing.products.Product;
import ch.almana.android.util.Debug;

public class BillingManager {

	public enum Managed {
		MANAGED, UNMANAGED
	}

	private BillingService mBillingService;
	private ProductCache productCache;
	private final Handler mHandler;
	private DatabasePurchaseObserver databasePurchaseObserver;
	private final Context ctx;
	private boolean started = false;

	public BillingManager(Context ctx) {
		super();
		mHandler = new Handler();
		this.ctx = ctx;
		mBillingService = new BillingService(ctx);

		productCache = new ProductCache(ctx);
		productCache.restoreDatabase(mBillingService, false);
		checkBillingSupported();
		registerReceiver();
		started = true;
	}

	public boolean reqister() {
		if (!started) {
			mBillingService = new BillingService(ctx);
			productCache = new ProductCache(ctx);
			registerReceiver();
			started = true;
			return true;
		}
		return false;
	}

	public void release() {
		unregisterReceiver();
		productCache.finish();
		mBillingService.stopSelf();
		started = false;
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
		if (Debug.isUnsinedPackage(ctx)) {
			productCache.setInitalised();
			productCache.purchasedItem(mSku, PurchaseState.PURCHASED, System.currentTimeMillis(), "");
			Toast.makeText(ctx, "Dummy purchase!", Toast.LENGTH_LONG).show();
			return true;
		}
		return mBillingService.requestPurchase(mSku, null);
	}


	public Map<String, Product> getOwnedItems() {
		return productCache.getOwnedItems();
	}

	private void registerReceiver() {
		if (databasePurchaseObserver == null) {
			databasePurchaseObserver = new DatabasePurchaseObserver(ctx, mHandler, productCache);
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
