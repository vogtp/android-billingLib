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

/**
 * The ProductManager is the main Entry point for Billing<br/>
 * 
 * It handles products and billing
 * 
 * @author vogtp
 * 
 */
public class ProductManager {

	private static Set<PurchaseListener> productListeners = new HashSet<PurchaseListener>();
	private final HashMap<Integer, ProductList> productList;

	private final BillingManager bm;

	private int billingInProgress = 0;

	private static ProductManager instance;
	private final ProductManagerPurchaseListener purchaseListener;

	private class ProductManagerPurchaseListener implements PurchaseListener {

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
			if (!(billingInProgress > 0 || productListeners.size() > 0)) {
				bm.release();
			}
			firePurchasedChanged(pid, count);
		}

		@Override
		public void billingSupported(boolean supported) {
			fireBillingSupported(supported);
		}

	}

	/**
	 * Get singelton instance of the ProductManager
	 * 
	 * @param ctx
	 *            Context to getApplicationContext() from
	 * @return The {@link ProductManager}
	 */
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
		purchaseListener = new ProductManagerPurchaseListener();
		bm.addPurchaseListener(purchaseListener);
	}

	/**
	 * Adds a new empty {@link ProductList}
	 * 
	 * @param listid
	 *            The unique id of the list
	 * 
	 * @throws ProductListExistsException
	 *             if the list id already exists
	 */
	public void addProductList(int listid) {
		if (hasProductList(listid)) {
			throw new ProductListExistsException(listid);
		}
		productList.put(listid, new ProductList());
	}

	/**
	 * Check if the list id exists
	 * @param listid The unique id of the list
	 * @return <code>true</code> if the list id has already been added <code>false</code>
	 */
	public boolean hasProductList(int listid) {
		return productList.get(listid) != null;
	}

	/**
	 * Get the {@link Product} array of the {@link ProductList}
	 * 
	 * @param listid
	 *            The unique id of the list
	 * @return the {@link Product} array cannot be <code>null</code>
	 */
	public Product[] getProducts(int listid) {
		return getProductList(listid).getProducts();
	}

	/**
	 * Get a {@link ProductList}
	 * 
	 * @param listid
	 *            The unique id of the list
	 * @return the {@link ProductList} cannot be <code>null</code>
	 * @throws ProductListExistsException
	 *             if the list id already exists
	 */
	public ProductList getProductList(int listid) {
		if (!hasProductList(listid)) {
			throw new NoSuchProductListException(listid);
		}
		return productList.get(listid);
	}

	/**
	 * Add a single {@link Product} to the {@link ProductList}
	 * 
	 * @param listid
	 *            The unique id of the list
	 * @param product
	 *            the {@link Product} to be added
	 * @throws ProductListExistsException
	 *             if the list id already exists
	 */
	public void addProduct(int listid, Product product) {
		if (!hasProductList(listid)) {
			throw new NoSuchProductListException(listid);
		}
		productList.put(listid, new ProductList());
	}

	/**
	 * Add a array {@link Product}s to the {@link ProductList}
	 * 
	 * @param listid
	 *            The unique id of the list
	 * @param product
	 *            the {@link Product} to be added
	 * @throws ProductListExistsException
	 *             if the list id already exists
	 */
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


	/**
	 * Add a listener to {@link PurchaseListener} events<br>
	 * Starts background market services if needed
	 * 
	 * @param listener
	 *            {@link PurchaseListener} to add
	 */
	public void addPurchaseListener(PurchaseListener listener) {
		if (listener == purchaseListener) {
			return;
		}
		if (bm.reqister()) {
			// bm has been released bevore, readd us
			bm.addPurchaseListener(purchaseListener);
		}
		productListeners.add(listener);
	}

	/**
	 * Remove listener from {@link PurchaseListener} events<br>
	 * If needed and no purchase if in progress stop background market services
	 * 
	 * @param listener
	 *            {@link PurchaseListener} to remove
	 */
	public void removePurchaseListener(PurchaseListener listener) {
		productListeners.remove(listener);
		if (!(billingInProgress > 0 || productListeners.size() > 0)) {
			bm.release();
		}
	}

	/**
	 * Refresh product status from app overwrite FIXME does not work like this
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

	/**
	 * Request the {@link Product} to be bought
	 * 
	 * @param product
	 *            the {@link Product} to be bought
	 */
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

	/**
	 * Restore the status (e.g. amount bought) from the product from the network
	 * service
	 */
	public void restoreTransactionsFromMarket() {
		bm.restoreTransactionsFromMarket();
		reinitaliseOwnedItems();
	}

	protected void fireBillingSupported(boolean supported) {
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
