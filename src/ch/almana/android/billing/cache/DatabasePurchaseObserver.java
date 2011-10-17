package ch.almana.android.billing.cache;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import ch.almana.android.billing.backend.BillingService.RequestPurchase;
import ch.almana.android.billing.backend.BillingService.RestoreTransactions;
import ch.almana.android.billing.backend.Consts;
import ch.almana.android.billing.backend.Consts.PurchaseState;
import ch.almana.android.billing.backend.Consts.ResponseCode;
import ch.almana.android.billing.backend.PurchaseObserver;

public class DatabasePurchaseObserver extends PurchaseObserver {
	private final static String TAG = "Billing";
	private final ProductCache productCache;

	public DatabasePurchaseObserver(Activity activity, Handler handler, ProductCache productCache) {
		super(activity, handler);
		this.productCache = productCache;
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, long purchaseTime, String developerPayload) {
		if (purchaseState == PurchaseState.PURCHASED) {
			productCache.purchasedItem(itemId, purchaseState, purchaseTime, developerPayload);
		}
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		if (Consts.DEBUG) {
			Log.d(TAG, request.mProductId + ": " + responseCode);
		}
		if (responseCode == ResponseCode.RESULT_OK) {
		} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
		} else {
		}
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.d(TAG, "completed RestoreTransactions request");
			}
			// Update the shared preferences so that we don't perform
			// a RestoreTransactions again.
			//						SharedPreferences.Editor edit = getPreferences().edit();
			//						edit.putBoolean(DB_INITIALIZED, true);
			//						edit.commit();
			productCache.setInitalised();
		} else {
			if (Consts.DEBUG) {
				Log.d(TAG, "RestoreTransactions error: " + responseCode);
			}
		}
	}

}