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
import ch.almana.android.billing.ProductManager;
import ch.almana.android.billing.R;
import ch.almana.android.billing.backend.PurchaseListener;
import ch.almana.android.billing.products.Product;
import ch.almana.android.billing.view.adapter.BillingProductAdaper;

public class BillingProductListActiviy extends ListActivity implements PurchaseListener {

	public static final String EXTRA_TITLE = "EXTRA_TITLE";
	public static final String EXTRA_PRODUCT_LIST_ID = "EXTRA_PRODUCT_LIST_ID";

	private BillingProductAdaper productAdaper;
	private int productListId;
	private ProductManager productManager;
	private final boolean billingInProgress = false;;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (title == null) {
			title = getString(R.string.title_products);
		}

		productListId = getIntent().getIntExtra(EXTRA_PRODUCT_LIST_ID, -1);

		productManager = ProductManager.getInstance(this);
		updateView();
	}


	private void updateView() {
		productAdaper = new BillingProductAdaper(this, productManager.getProducts(productListId));
		getListView().setAdapter(productAdaper);

	}

	@Override
	protected void onResume() {
		super.onResume();
		productManager.addPurchaseListener(this);
		updateView();
	}

	@Override
	protected void onDestroy() {
		productManager.removePurchaseListener(this);
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Product product = (Product) productAdaper.getItem(position);
		productManager.requestPurchase(product);
		if (product.isManaged() && product.getCount() > 0) {
			configureManagedProduct(product);
			productAdaper.notifyDataSetChanged();
		}
		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Hook to configure managed products
	 */
	protected void configureManagedProduct(Product product) {

	}

	@Override
	public void onBackPressed() {
		if (billingInProgress) {
			Toast.makeText(this, R.string.msg_billing_in_progress, Toast.LENGTH_LONG).show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void purchaseChanged(String pid, int count) {
		updateView();
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
		productManager.restoreTransactionsFromMarket();
	}

	public static Intent getIntent(Context ctx, Class<? extends BillingProductListActiviy> clazz, CharSequence title, int productListId) {
		Intent i = new Intent(ctx, clazz);
		i.putExtra(EXTRA_TITLE, title);
		i.putExtra(EXTRA_PRODUCT_LIST_ID, productListId);
		return i;
	}

}
