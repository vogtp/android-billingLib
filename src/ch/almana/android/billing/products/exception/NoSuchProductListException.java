package ch.almana.android.billing.products.exception;

public class NoSuchProductListException extends RuntimeException {

	private static final long serialVersionUID = -5280109447789268078L;

	public NoSuchProductListException(int listid) {
		super("ProductId: " + listid);
	}
}
