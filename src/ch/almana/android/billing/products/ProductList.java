package ch.almana.android.billing.products;

import java.util.ArrayList;

public class ProductList /* implements Parcelable*/{

	private Product[] products;
	private ArrayList<Product> productList;

	/*
		public static final Parcelable.Creator<ProductList> CREATOR = new Parcelable.Creator<ProductList>() {
			@Override
			public ProductList createFromParcel(Parcel in) {
				return new ProductList(in);
			}

			@Override
			public ProductList[] newArray(int size) {
				return new ProductList[size];
			}
		};

		public ProductList(Parcel in) {
			super();
			Parcelable[] parcelableArray = in.readParcelableArray(ClassLoader.getSystemClassLoader());
			this.products = (Product[]) parcelableArray;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelableArray(getProducts(), 0);
		}

		@Override
		public int describeContents() {
			return 0;
		}
	*/

	public ProductList(Product... prods) {
		super();
		products = prods;
	}

	public Product[] getProducts() {
		if (products == null && productList == null) {
			return new Product[0];
		}
		if (products == null) {
			products = new Product[productList.size()];
			products = productList.toArray(products);
			productList = null;
		}
		return products;
	}

	public void add(Product product) {
		if (products != null) {
			productList = new ArrayList<Product>(products.length);
			for (int i = 0; i < products.length; i++) {
				productList.add(products[i]);
			}
			products = null;
		}
		if (productList == null) {
			productList = new ArrayList<Product>();
		}
		productList.add(product);
	}

}
