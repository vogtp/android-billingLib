package ch.almana.android.billing.products;

import android.util.SparseArray;
import ch.almana.android.billing.products.exception.NoSuchProductListException;
import ch.almana.android.billing.products.exception.ProductListExistsException;

public class ProductManager {

	private final SparseArray<ProductList> productList;

	private static ProductManager instance;

	public static final ProductManager getInstance() {
		if (instance == null) {
			instance = new ProductManager();
		}
		return instance;
	}

	public ProductManager() {
		super();
		productList = new SparseArray<ProductList>();
	}

	public void addProductList(int listid) {
		if (hasProductList(listid)) {
			throw new ProductListExistsException(listid);
		}
		productList.append(listid, new ProductList());
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
		return productList.get(listid, new ProductList());
	}

	public void addProduct(int listid, Product product) {
		if (!hasProductList(listid)) {
			throw new NoSuchProductListException(listid);
		}
		productList.put(listid, new ProductList());
	}

	public void addProducts(int listid, ProductList products) {
		if (!hasProductList(listid)) {
			addProductList(listid);
		}
		productList.put(listid, products);
	}


	public void notifyProductChanged(Product product) {
		// TODO Auto-generated method stub

	}

	public void purchaseChanged(int listid, String pid, int count) {
		if (pid == null) {
			return;
		}
		Product[] products = getProducts(listid);
		for (int i = 0; i < products.length; i++) {
			if (pid.equals(products[i].getProductId())) {
				products[i].setCount(count);
			}
		}
	}

}
