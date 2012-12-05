package ch.almana.android.billing.products.exception;

public class ProductListExistsException extends RuntimeException {

	public ProductListExistsException(int listid) {
		super("ProductId: " + listid);
	}

}
