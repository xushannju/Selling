package common.sunshine.model.selling.express;

import common.sunshine.model.selling.order.OrderItem;
import org.springframework.util.StringUtils;

/**
 * Created by sunshine on 6/22/16.
 */
public class Express4Agent extends Express {
    private OrderItem item;

    public Express4Agent() {
        super();
    }

    public Express4Agent(String expressNumber) {
        super(expressNumber);
    }

    public Express4Agent(String senderName, String senderPhone, String senderAddress, String receiverName, String receiverPhone, String receiverAddress) {
        super(senderName, senderPhone, senderAddress, receiverName, receiverPhone, receiverAddress);
    }

    public Express4Agent(String senderName, String senderPhone, String senderAddress, String receiverName, String receiverPhone, String receiverAddress, String goodsName) {
        super(senderName, senderPhone, senderAddress, receiverName, receiverPhone, receiverAddress, goodsName);
    }

    public Express4Agent(String expressNumber, String senderName, String senderPhone, String senderAddress, String receiverName, String receiverPhone, String receiverAddress, String goodsName) {
        super(expressNumber, senderName, senderPhone, senderAddress, receiverName, receiverPhone, receiverAddress, goodsName);
    }

    public OrderItem getItem() {
        return item;
    }

    public void setItem(OrderItem item) {
        this.item = item;
        if(!StringUtils.isEmpty(item.getDescription())) {
            setDescription(item.getDescription());
        }
        this.setLinkId(item.getOrderItemId());
    }
}
