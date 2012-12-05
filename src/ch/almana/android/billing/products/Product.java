package ch.almana.android.billing.products;

import android.os.Parcel;
import android.os.Parcelable;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.BillingManager.Managed;

public class Product implements Comparable<Object>, Parcelable {

	private String productId;
	private String name;
	private int count;
	private String desc;
	private Managed managed;
	

	public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
		@Override
		public Product createFromParcel(Parcel in) {
			return new Product(in);
		}

		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}
	};
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(productId);
		dest.writeString(name);
		dest.writeString(desc);
		dest.writeInt(count);
		dest.writeInt(managed == Managed.MANAGED ? 1 : 0);
	}

	public Product(Parcel in) {
		super();
		this.productId = in.readString();
		this.name = in.readString();
		this.desc = in.readString();
		this.count = in.readInt();
		this.managed = in.readInt() == 1 ? Managed.MANAGED : Managed.UNMANAGED;
	}

	public Product(String productId, String name, String desc, int count, Managed managed) {
		super();
		this.productId = productId;
		this.name = name;
		this.desc = desc;
		this.count = count;
		this.managed = managed;
	}

	public Product(String productId) {
		this(productId, "", "", 0, Managed.MANAGED);
	}


	public Product(String name, int count) {
		this("", name, "", count, Managed.MANAGED);
	}

	public Product(String productId, String name, String desc, Managed managed) {
		this(productId, name, desc, 0, managed);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	@Override
	public String toString() {
		return name + " (" + count + ")";
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Managed getManaged() {
		return managed;
	}

	public void setManaged(Managed managed) {
		this.managed = managed;
	}

	public boolean isManaged() {
		return managed == Managed.MANAGED;
	}

	@Override
	public int compareTo(Object another) {
		if (another == null) {
			return -1;
		}
		if (another.getClass() == Product.class) {
			return productId.compareTo(((Product)another).productId);
		}
		return -1;
	}
}
