package selling.sunshine.dao;

import common.sunshine.model.selling.customer.Customer;
import common.sunshine.model.selling.customer.CustomerAddress;
import common.sunshine.pagination.DataTableParam;
import common.sunshine.utils.ResultData;

import java.util.Map;

/**
 * Created by sunshine on 5/5/16.
 */
public interface CustomerDao {
    ResultData insertCustomer(Customer customer);
    
    ResultData updateCustomer(Customer customer);
    
    ResultData deleteCustomer(Customer customer);

    ResultData queryCustomer(Map<String, Object> condition);

    ResultData queryCustomerByPage(Map<String, Object> condition, DataTableParam param);
    
    ResultData queryCustomerPhone(Map<String, Object> condition);
    
    ResultData queryCustomerAddress(Map<String, Object> condition);
    
    ResultData updateCustomerAddress(CustomerAddress customerAddress);
}
