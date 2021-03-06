package selling.sunshine.controller;


import com.csvreader.CsvReader;

import common.sunshine.model.selling.admin.Admin;
import common.sunshine.model.selling.order.CustomerOrder;
import common.sunshine.model.selling.order.EventOrder;
import common.sunshine.model.selling.order.Order;
import common.sunshine.model.selling.order.OrderItem;
import common.sunshine.model.selling.order.support.OrderItemStatus;
import common.sunshine.model.selling.order.support.OrderStatus;
import common.sunshine.model.selling.user.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import selling.sunshine.model.*;
import common.sunshine.model.selling.express.Express;
import common.sunshine.model.selling.express.Express4Agent;
import common.sunshine.model.selling.express.Express4Application;
import common.sunshine.model.selling.express.Express4Customer;
import selling.sunshine.service.EventService;
import selling.sunshine.service.ExpressService;
import selling.sunshine.service.LogService;
import selling.sunshine.service.OrderService;
import selling.sunshine.service.ToolService;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping("/express")
@RestController
public class ExpressController {

    private Logger logger = LoggerFactory.getLogger(ExpressController.class);

    @Autowired
    private ExpressService expressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private LogService logService;
    
	@Autowired
	private EventService eventService;

    @RequestMapping(method = RequestMethod.POST, value = "/detail/{id}")
    public ResultData detail(@PathVariable("id") String id) {
        ResultData resultData = new ResultData();
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> condition = new HashMap<>();
        if (!StringUtils.isEmpty(id) && id.startsWith("ORI")) {
            condition.put("orderItemId", id);
            ResultData queryResponse = expressService
                    .fetchExpress4Agent(condition);
            if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                Express4Agent express = ((List<Express4Agent>) queryResponse
                        .getData()).get(0);
                dataMap.put("express", express);
                resultData.setData(dataMap);
            } else {
                resultData.setResponseCode(ResponseCode.RESPONSE_NULL);
            }
        } else if (id.startsWith("CUO")) {
            condition.put("orderId", id);
            ResultData queryResponse = expressService
                    .fetchExpress4Customer(condition);
            if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                Express4Customer express = ((List<Express4Customer>) queryResponse
                        .getData()).get(0);
                dataMap.put("express", express);
                resultData.setData(dataMap);
            } else {
                resultData.setResponseCode(ResponseCode.RESPONSE_NULL);
            }
        }else{
        	 condition.put("orderId", id);
        	 ResultData queryResponse = expressService
                     .fetchExpress4Application(condition);
             if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            	 Express4Application express = ((List<Express4Application>) queryResponse
                         .getData()).get(0);
                 dataMap.put("express", express);
                 resultData.setData(dataMap);
             } else {
                 resultData.setResponseCode(ResponseCode.RESPONSE_NULL);
             }
        }
        return resultData;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/upload")
    public ModelAndView overview() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/express/express_upload");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public ModelAndView overview(MultipartHttpServletRequest request)
            throws IOException {
        ResultData resultData = new ResultData();
        ModelAndView view = new ModelAndView();
        MultipartFile file = request.getFile("excelFile");
        if (file.isEmpty()) {
            resultData.setResponseCode(ResponseCode.RESPONSE_NULL);
            view.setViewName("/backend/express/express_upload");
            return view;
        }
        InputStream input = file.getInputStream();

        CsvReader csvReader = new CsvReader(input, Charset.forName("GBK"));
        List<String[]> csvList = new ArrayList<String[]>(); // 用来保存数据
        //读取表头
        csvReader.readHeaders();
        //逐条读取记录，直至读完
        while (csvReader.readRecord()) {
            csvList.add(csvReader.getValues());
        }
        csvReader.close();
        for (int row = 0; row < csvList.size() - 1; row++) {
            if (csvList.get(row).length < 36) {
                view.setViewName("/backend/express/express_upload");
                return view;
            }
            if (csvList.get(row)[7].equals("")) {
                continue;
            }
            Map<String, Object> con = new HashMap<>();
            con.put("expressNumber", csvList.get(row)[7]);
            if (expressService.fetchExpress(con).getResponseCode() == ResponseCode.RESPONSE_NULL) {
                String linkID = csvList.get(row)[8]; //取得第row行第0列的数据
                // String address = csvList.get(row)[2] + csvList.get(row)[3] + csvList.get(row)[4] + csvList.get(row)[5];
                String address = csvList.get(row)[5];
                if (linkID.startsWith("ORI")) {
                    Express4Agent express = new Express4Agent(csvList.get(row)[7],
                            csvList.get(row)[14], csvList.get(row)[15],
                            csvList.get(row)[20], csvList.get(row)[0],
                            csvList.get(row)[1], address,
                            csvList.get(row)[27]);
                    OrderItem temp = new OrderItem();
                    Map<String, Object> condition = new HashMap<>();
                    condition.put("orderItemId", linkID);
                    ResultData fetchResponse = orderService.fetchOrderItem(condition);
                    if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                        temp = ((List<OrderItem>) fetchResponse.getData()).get(0);
                        temp.setStatus(OrderItemStatus.SHIPPED);
                        express.setItem(temp);
                        expressService.createExpress(express);
                        orderService.updateOrderItem(temp);
                        condition.clear();
                        condition.put("orderId", temp.getOrder().getOrderId());
                        fetchResponse = orderService.fetchOrder(condition);
                        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                            Order order = ((List<Order>) fetchResponse.getData()).get(0);
                            List<OrderItem> orderItems=order.getOrderItems();
                            boolean flag=true;
                            for (OrderItem item:orderItems) {
								if (item.getStatus()!=OrderItemStatus.SHIPPED) {
									flag=false;
									break;
								}
							}
                            if (flag) {							
                               order.setStatus(OrderStatus.FULLY_SHIPMENT);
                               orderService.modifyOrder(order);
                            } else {
                                order.setStatus(OrderStatus.PATIAL_SHIPMENT);
                                orderService.modifyOrder(order);
                            }
                        }
                    }

                } else if(linkID.startsWith("CUO")){
                    Express4Customer express = new Express4Customer(
                            csvList.get(row)[7],
                            csvList.get(row)[14], csvList.get(row)[15],
                            csvList.get(row)[20], csvList.get(row)[0],
                            csvList.get(row)[1], address,
                            csvList.get(row)[27]);
                    Map<String, Object> condition = new HashMap<>();
                    condition.put("orderId", linkID);
                    ResultData fetchResponse = orderService.fetchCustomerOrder(condition);
                    if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                        CustomerOrder temp = ((List<CustomerOrder>) fetchResponse.getData()).get(0);
                        temp.setStatus(OrderItemStatus.SHIPPED);
                        express.setOrder(temp);
                        expressService.createExpress(express);
                        orderService.updateCustomerOrder(temp);
                    }

                }else{
                	Express4Application express = new Express4Application(
                            csvList.get(row)[7],
                            csvList.get(row)[14], csvList.get(row)[15],
                            csvList.get(row)[20], csvList.get(row)[0],
                            csvList.get(row)[1], address,
                            csvList.get(row)[27]);
                	 Map<String, Object> condition = new HashMap<>();
                     condition.put("orderId", linkID);
                     ResultData fetchResponse = eventService.fetchEventOrder(condition);
                     if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                    	 EventOrder temp = ((List<EventOrder>) fetchResponse.getData()).get(0);
                         temp.setOrderStatus(OrderItemStatus.SHIPPED);
                         express.setEventOrder(temp);
                         expressService.createExpress(express);
                         eventService.updateEventOrder(temp);
                     }
                }
            }
        }
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (user == null) {
            view.setViewName("/backend/express/express_upload");
            return view;
        }
        Admin admin = user.getAdmin();
        BackOperationLog backOperationLog = new BackOperationLog(
                admin.getUsername(), toolService.getIP((HttpServletRequest) request), "管理员" + admin.getUsername() + "上传了快递单信息");
        logService.createbackOperationLog(backOperationLog);
        resultData.setResponseCode(ResponseCode.RESPONSE_OK);
        view.setViewName("redirect:/order/check");
        return view;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/queryExpress/{expressNumber}")
    public ResultData queryExpress(@PathVariable("expressNumber") String expressNumber) {
        ResultData result = new ResultData();
        ResultData traceResponse = expressService.traceExpress(expressNumber, "TRACES");
        if (traceResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result.setData(traceResponse.getData());
        } else {
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
        }
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{orderId}/query")
    public ResultData queryExpressByOrderId(@PathVariable("orderId") String orderId){
    	ResultData result = new ResultData();
    	Map<String, Object> condition = new HashMap<String, Object>();
    	ResultData fetchExpressResponse = null;
    	if(orderId.startsWith("ORI")){
    		condition.clear();
			condition.put("orderItemId", orderId);
			condition.put("blockFlag", false);
			fetchExpressResponse = expressService.fetchExpress4Agent(condition);
    	} else if(orderId.startsWith("CUO")){
    		condition.clear();
			condition.put("orderId", orderId);
			condition.put("blockFlag", false);
			fetchExpressResponse = expressService.fetchExpress4Customer(condition);
    	} else if(orderId.startsWith("EOI")){
    		condition.clear();
			condition.put("orderId", orderId);
			condition.put("blockFlag", false);
			fetchExpressResponse = expressService.fetchExpress4Application(condition);
    	}
    	if(fetchExpressResponse == null){
    		result.setResponseCode(ResponseCode.RESPONSE_NULL);
			result.setDescription("订单号不正确");
			return result;	
    	}
    	if(((List<Express>)fetchExpressResponse.getData()).isEmpty()){
			result.setResponseCode(ResponseCode.RESPONSE_NULL);
			result.setDescription("该订单无快递信息");
			return result;
		}
		if(fetchExpressResponse.getResponseCode() == ResponseCode.RESPONSE_ERROR){
			result.setResponseCode(fetchExpressResponse.getResponseCode());
			result.setDescription(fetchExpressResponse.getDescription());
			logger.error(fetchExpressResponse.getDescription());
			return result;
		}
		Express express = ((List<Express>) fetchExpressResponse.getData()).get(0);
		ResultData traceResponse = expressService.traceExpress(express.getExpressNumber(), "TRACES");
		result.setData(traceResponse.getData());
		result.setDescription(express.getExpressNumber() );
		return result;
    }

}
