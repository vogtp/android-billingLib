package ch.almana.android.billing;

import ch.almana.android.billing.BillingManager.Managed;

public class Product implements Comparable<Object> {

	private String productId;
	private String name;
	private int count;
	private String desc;
	private Managed managed;

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
