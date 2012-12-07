package ch.almana.android.billing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import ch.almana.android.billing.backend.BillingManager;
import ch.almana.android.billing.backend.PurchaseListener;
import ch.almana.android.billing.log.Logger;
import ch.almana.android.billing.products.Product;
import ch.almana.android.billing.products.ProductList;
import ch.almana.android.billing.products.exception.NoSuchProductListException;
import ch.almana.android.billing.products.exception.ProductListExistsException;

public class ProductManager implements PurchaseListener {

	private static Set<PurchaseListener> productListeners = new HashSet<PurchaseListener>();
	private final HashMap<Integer, ProductList> productList;

	private final BillingManager bm;

	private int billingInProgress = 0;

	private static ProductManager instance;

	public static final ProductManager getInstance(Context ctx) {
		if (instance == null) {
			instance = new ProductManager(ctx.getApplicationContext());
		}
		return instance;
	}

	private ProductManager(Context ctx) {
		super();
		this.productList = new HashMap<Integer, ProductList>();
		this.bm = new BillingManager(ctx);
		bm.addPurchaseListener(this);
	}

	public void addProductList(int listid) {
		if (hasProductList(listid)) {
			throw new ProductListExistsException(listid);
		}
		productList.put(listid, new ProductList());
	}

	public boolean hasProductList(int listid) {
		return productList.get(listid) != null;
	}

	public Product[] getProducts(int listid) {
		return getProductList(listid).getProducts();
	}

	public ProductList getProductList(int listid) {
		if (!hasProductList(listid)) {
			throw new NoSuchProductListException(listid);
		}
		return productList.get(listid);
	}

	public void addProduct(int listid, Product product) {
		if (!hasProductList(listid)) {
			throw new NoSuchProductListException(listid);
		}
		productList.put(listid, new ProductList());
	}

	public void addProducts(int listid, ProductList pl) {
		if (!hasProductList(listid)) {
			addProductList(listid);
		}
		Product[] ps = pl.getProducts();
		Map<String, Product> ownedItems = bm.getOwnedItems();
		for (int i = 0; i < ps.length; i++) {
			if (ownedItems.containsKey(ps[i].getProductId())) {
				ps[i].setCount(ownedItems.get(ps[i].getProductId()).getCount());
			}
		}
		productList.put(listid, pl);
	}

	@Override
	public void purchaseChanged(String pid, int count) {
		billingInProgress--;
		if (pid == null) {
			return;
		}
		for (Integer listid : productList.keySet()) {
			Product[] products = getProducts(listid);
			for (int i = 0; i < products.length; i++) {
				if (pid.equals(products[i].getProductId())) {
					products[i].setCount(count);
				}
			}
		}
		firePurchasedChanged(pid, count);
	}

	@Override
	public void billingSupported(boolean supported) {
		fireBillingSupported(supported);
	}

	public void addPurchaseListener(PurchaseListener listener) {
		if (listener == this) {
			return;
		}
		if (bm.reqister()) {
			// bm has been released bevore, readd us
			bm.addPurchaseListener(this);
		}
		productListeners.add(listener);
	}

	public void removePurchaseListener(PurchaseListener listener) {
		productListeners.remove(listener);
		if (!(billingInProgress > 0 || productListeners.size() > 0)) {
			bm.release();
		}
	}

	/**
	 * Refresh product status in app overwrite
	 */
	protected void reinitaliseOwnedItems() {
		for (Integer listid : productList.keySet()) {
			Product[] products = getProducts(listid);
			for (int i = 0; i < products.length; i++) {
				Product product = products[i];
				int count = bm.getCountOfProduct(product.getProductId());
				product.setCount(count);
				firePurchasedChanged(product.getProductId(), count);
			}
		}
	}

	public void requestPurchase(Product product) {
		if (!product.isManaged() || product.getCount() < 1) {
			try {
				billingInProgress++;
				if (billingInProgress < 1) {
					billingInProgress = 1;
				}
				bm.requestPurchase(product.getProductId());
			} catch (Throwable e) {
				billingInProgress--;
				Logger.e("Error requesting purchase", e);
			}
		}
	}

	public void restoreTransactionsFromMarket() {
		bm.restoreTransactionsFromMarket();
		reinitaliseOwnedItems();
	}

	public void fireBillingSupported(boolean supported) {
		for (PurchaseListener listener : productListeners) {
			listener.billingSupported(supported);
		}
	}

	private void firePurchasedChanged(String pid, int count) {
		for (PurchaseListener listener : productListeners) {
			listener.purchaseChanged(pid, count);
		}
	}

}
