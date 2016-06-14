package selling.sunshine.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import selling.sunshine.form.CustomerAddressForm;
import selling.sunshine.form.CustomerForm;
import selling.sunshine.form.SortRule;
import selling.sunshine.model.Agent;
import selling.sunshine.model.Customer;
import selling.sunshine.model.CustomerAddress;
import selling.sunshine.model.CustomerPhone;
import selling.sunshine.model.OrderItem;
import selling.sunshine.model.User;
import selling.sunshine.pagination.DataTablePage;
import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.service.AgentService;
import selling.sunshine.service.CustomerService;
import selling.sunshine.service.OrderService;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by sunshine on 4/11/16.
 */
@RequestMapping("/customer")
@RestController
public class CustomerController {
    private Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private OrderService orderService;

    @RequestMapping(method = RequestMethod.GET, value = "/overview/{agentId}")
    public ModelAndView overview(@PathVariable String agentId) {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/customer/overview");
        view.addObject("agentId", agentId);
        return view;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/overview/{agentId}")
    public DataTablePage<Customer> overview(@PathVariable String agentId,DataTableParam param) {
        DataTablePage<Customer> result = new DataTablePage<Customer>();
        if (StringUtils.isEmpty(result)) {
            return result;
        }
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("agentId", agentId);
        ResultData fetchResponse = customerService.fetchCustomer(condition, param);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result = (DataTablePage<Customer>) fetchResponse.getData();
        }
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/detail/{customerId}")
    public ModelAndView detail(@PathVariable String customerId) {
        ModelAndView view = new ModelAndView();
        Map<String, Object> condition = new HashMap<>();
        condition.put("customerId", customerId);
        Customer customer=((List<Customer>)customerService.fetchCustomer(condition).getData()).get(0);      
        Map<String, Object> agentCondition = new HashMap<>();
        agentCondition.put("agentId", customer.getAgent().getAgentId());
        Agent agent=((List<Agent>)agentService.fetchAgent(agentCondition).getData()).get(0);
        Map<String, Object> orderItemCondition = new HashMap<>();
        orderItemCondition.put("customerId", customerId);
        List<OrderItem> orderItemList=(List<OrderItem>)orderService.fetchOrderItem(orderItemCondition).getData();

        List<CustomerAddress> addressList=(List<CustomerAddress>)customerService.fetchCustomerAddress(condition).getData();
        List<CustomerPhone> phoneList=(List<CustomerPhone>)customerService.fetchCustomerPhone(condition).getData();
        
        view.addObject("customer",customer);
        view.addObject("agent",agent);
        view.addObject("orderItemList",orderItemList);	
        view.addObject("addressList",addressList);	
        view.addObject("phoneList",phoneList);	
        view.setViewName("/backend/customer/detail");
        return view;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public ResultData addCustomer(@Valid CustomerForm customerForm, BindingResult result) {
        ResultData resultData = new ResultData();
        if (result.hasErrors()) {
            resultData.setResponseCode(ResponseCode.RESPONSE_ERROR);
            return resultData;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("phone", customerForm.getPhone());
        condition.put("blockFlag", false);
        condition.put("customerBlockFlag", false);
        resultData = customerService.fetchCustomerPhone(condition);
        if (((List<CustomerPhone>) resultData.getData()).size() != 0) {
            resultData.setResponseCode(ResponseCode.RESPONSE_ERROR);
            return resultData;
        }
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        selling.sunshine.model.lite.Agent agent = user.getAgent();
        Customer customer = new Customer(customerForm.getName(),
                customerForm.getAddress(), customerForm.getPhone(), agent);
        resultData = customerService.createCustomer(customer);
        return resultData;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/modify/{customerId}")
    public ResultData updateCustomer(@PathVariable("customerId") String customerId, @Valid CustomerForm customerForm, BindingResult result) {
        ResultData response = new ResultData();
        if (result.hasErrors()) {
            response.setResponseCode(ResponseCode.RESPONSE_ERROR);
            return response;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("phone", customerForm.getPhone());
        condition.put("blockFlag", false);
        condition.put("customerBlockFlag", false);
        response = customerService.fetchCustomerPhone(condition);
        if (((List<CustomerPhone>) response.getData()).size() != 0) {
            CustomerPhone target = ((List<CustomerPhone>) response.getData()).get(0);
            if (!target.getCustomer().getCustomerId().equals(customerId)) {
                response.setResponseCode(ResponseCode.RESPONSE_ERROR);
                return response;
            }
        }
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        selling.sunshine.model.lite.Agent agent = user.getAgent();
        Customer customer = new Customer(customerForm.getName(),
                customerForm.getAddress(), customerForm.getPhone(), agent);
        customer.setCustomerId(customerId);
        ResultData updateResponse = customerService.updateCustomer(customer);
        if (updateResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            response.setData(updateResponse.getData());
        } else {
            response.setResponseCode(updateResponse.getResponseCode());
            response.setDescription(updateResponse.getDescription());
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/modifyAddress/{customerId}")
    public ResultData updateCustomerAddress(@PathVariable("customerId") String customerId, @Valid CustomerAddressForm customerAddressForm, BindingResult result) {
        ResultData response = new ResultData();
        if (result.hasErrors()) {
            response.setResponseCode(ResponseCode.RESPONSE_ERROR);
            return response;
        }
        Map<String, Object> condition = new HashMap<>();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        selling.sunshine.model.lite.Agent agent = user.getAgent();
        Customer customer = new Customer(null, customerAddressForm.getAddress(), null, agent);
        customer.setCustomerId(customerId);
        ResultData updateResponse = customerService.updateCustomer(customer);
        if (updateResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            response.setData(updateResponse.getData());
        } else {
            response.setResponseCode(updateResponse.getResponseCode());
            response.setDescription(updateResponse.getDescription());
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/delete/{customerId}")
    public ResultData deleteCustomer(@PathVariable String customerId) {
        ResultData response = new ResultData();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("agentId", user.getAgent().getAgentId());
        condition.put("customerId", customerId);
        ResultData fetchCustomerResponse = customerService.fetchCustomer(condition);
        if (fetchCustomerResponse.getResponseCode() != ResponseCode.RESPONSE_OK) {
            response.setResponseCode(fetchCustomerResponse.getResponseCode());
            response.setDescription(fetchCustomerResponse.getDescription());
            return response;
        }
        Customer customer = ((List<Customer>) fetchCustomerResponse.getData()).get(0);
        ResultData updateResponse = customerService.deleteCustomer(customer);
        if (updateResponse.getResponseCode() != ResponseCode.RESPONSE_OK) {
            response.setResponseCode(updateResponse.getResponseCode());
            response.setDescription(updateResponse.getDescription());
            return response;
        }
        response.setData(updateResponse.getData());
        return response;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/{customerId}")
    public ResultData fetchCustomer(@PathVariable("customerId") String customerId) {
        ResultData result = new ResultData();
        Map<String, Object> condition = new HashMap<>();
        condition.put("customerId", customerId);
        condition.put("blockFlag", false);
        ResultData fetchResponse = customerService.fetchCustomer(condition);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result.setData(((List<Customer>) fetchResponse.getData()).get(0));
        } else {
            fetchResponse.setResponseCode(fetchResponse.getResponseCode());
            fetchResponse.setDescription(fetchResponse.getDescription());
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/address/{customerId}")
    public ResultData fetchCustomerAddress(@PathVariable("customerId") String customerId) {
        ResultData result = new ResultData();
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("customerId", customerId);
        List<SortRule> rule = new ArrayList<SortRule>();
        rule.add(new SortRule("create_time", "desc"));
        condition.put("sort", rule);
        ResultData fetchResponse = customerService.fetchCustomerAddress(condition);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            List<CustomerAddress> addressList = (List<CustomerAddress>) fetchResponse.getData();
            HashSet<CustomerAddress> addressSet = new HashSet<CustomerAddress>(addressList);
            addressList.clear();
            addressList.addAll(addressSet);
            int size = addressList.size() > 5 ? 5 : addressList.size();
            result.setData(addressList.subList(0, size));
        } else {
            result.setResponseCode(fetchResponse.getResponseCode());
            result.setDescription(fetchResponse.getDescription());
        }

        return result;
    }


}
