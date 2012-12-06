package ch.almana.android.billing.view.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.PurchaseListener;
import ch.almana.android.billing.R;
import ch.almana.android.billing.log.Logger;
import ch.almana.android.billing.products.Product;
import ch.almana.android.billing.products.ProductManager;
import ch.almana.android.billing.view.adapter.BillingProductAdaper;

public class BillingProductListActiviy extends ListActivity implements PurchaseListener {

	public static final String EXTRA_TITLE = "EXTRA_TITLE";
	public static final String EXTRA_PRODUCT_LIST_ID = "EXTRA_PRODUCT_LIST_ID";
	private BillingManager bm;
	private BillingProductAdaper productAdaper;
	private int productListId;
	private ProductManager productManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (title == null) {
			title = getString(R.string.title_products);
		}

		productListId = getIntent().getIntExtra(EXTRA_PRODUCT_LIST_ID, -1);

		productManager = ProductManager.getInstance();
		bm = new BillingManager(this);
		bm.addPurchaseListener(this);
		updateView();
	}

	/**
	 * Refresh product status in app overwrite
	 */
	protected void reinitaliseOwnedItems() {
		Product[] products = productManager.getProducts(productListId);
		for (int i = 0; i < products.length; i++) {
			Product product = products[i];
			int count = bm.getCountOfProduct(product.getProductId());
			product.setCount(count);
			productManager.notifyProductChanged(product);
		}

	}

	private void updateView() {
		productAdaper = new BillingProductAdaper(this, productManager.getProducts(productListId));
		getListView().setAdapter(productAdaper);

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
		bm.addPurchaseListener(this);
	}

	@Override
	protected void onDestroy() {
		bm.removePurchaseListener(this);
		bm.release();
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Product product = (Product) productAdaper.getItem(position);
		if (!product.isManaged() || product.getCount() < 1) {
			try {
				bm.requestPurchase(product.getProductId());
			} catch (Throwable e) {
				Logger.w("Error requesting purchase", e);
			}
		}
		if (product.isManaged() && product.getCount() > 0) {
			configureManagedProduct(product);
			productManager.notifyProductChanged(product);
		}
		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Hook to configure managed products
	 */
	protected void configureManagedProduct(Product product) {

	}

	@Override
	public void purchaseChanged(String pid, int count) {
		updateView();
		productManager.purchaseChanged(productListId, pid, count);
		productAdaper.notifyDataSetChanged();
	}

	@Override
	public void billingSupported(boolean supported) {
		if (!supported) {
			Toast.makeText(this, "Billing not supported!", Toast.LENGTH_LONG).show();
			getListView().setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.refresh_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuItemRefresh) {
			refreshFromMarket();
			return true;
		}
		return false;
	}

	private void refreshFromMarket() {
		bm.restoreTransactionsFromMarket();
		reinitaliseOwnedItems();
	}

	public static Intent getIntent(Context ctx, Class<? extends BillingProductListActiviy> clazz, CharSequence title, int productListId) {
		Intent i = new Intent(ctx, clazz);
		i.putExtra(EXTRA_TITLE, title);
		i.putExtra(EXTRA_PRODUCT_LIST_ID, productListId);
		return i;
	}

}
