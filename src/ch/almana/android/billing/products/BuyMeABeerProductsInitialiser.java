package ch.almana.android.billing.products;

import android.content.Context;
import ch.almana.android.billing.ProductManager;
import ch.almana.android.billing.R;
import ch.almana.android.billing.backend.BillingManager.Managed;

public class BuyMeABeerProductsInitialiser {

	public static final int PRODUCTS_BUYMEABEER = 4711;

	private boolean initialised = false;
	protected ProductManager productManager;

	public void initProducts(Context ctx) {
		if (!initialised) {
			initialised = true;
			productManager = ProductManager.getInstance(ctx);
			inititaliseProductLists(ctx);
		}
	}

	protected void inititaliseProductLists(Context ctx) {
		productManager.addProducts(PRODUCTS_BUYMEABEER, getBuyMeABeerProducts(ctx));
	}

	private ProductList getBuyMeABeerProducts(Context ctx) {
		ProductList beer = new ProductList(new Product("kids.cookie", ctx.getString(R.string.name_buy_cookie), ctx.getString(R.string.desc_buy_cookie), Managed.UNMANAGED),
				new Product("bar.beer", ctx.getString(R.string.name_buy_me_a_beer), ctx.getString(R.string.desc_buy_beer), Managed.UNMANAGED));
		beer.add(new Product("bar.whiskey", ctx.getString(R.string.name_buy_me_a_whiskey), ctx.getString(R.string.desc_buy_whiskey), Managed.UNMANAGED));
		beer.add(new Product("kids.toys", ctx.getString(R.string.name_buy_toy), ctx.getString(R.string.desc_buy_toy), Managed.UNMANAGED));
		return beer;
	}
	
}
