package common.sunshine.model.selling.bill;

import common.sunshine.model.selling.bill.support.BillStatus;
import common.sunshine.model.selling.order.CustomerOrder;

public class CustomerOrderBill extends Bill {
	private CustomerOrder customerOrder;
	
	public CustomerOrderBill() {
        super();
    }

    public CustomerOrderBill(double billAmount, String channel, String clientIp, CustomerOrder customerOrder) {
        super(billAmount, channel, clientIp);
        this.customerOrder = customerOrder;
    }

    public CustomerOrderBill(double billAmount, String channel, String clientIp, BillStatus status, CustomerOrder customerOrder) {
        super(billAmount, channel, clientIp, status);
        this.customerOrder = customerOrder;
    }

	public CustomerOrder getCustomerOrder() {
		return customerOrder;
	}

	public void setCustomerOrder(CustomerOrder customerOrder) {
		this.customerOrder = customerOrder;
	}

}
