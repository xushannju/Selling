package selling.sunshine.dao.impl;

import common.sunshine.model.selling.agent.lite.Agent;
import common.sunshine.utils.IDGenerator;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import common.sunshine.dao.BaseDao;
import selling.sunshine.dao.OrderDao;
import common.sunshine.model.selling.order.Order;
import common.sunshine.model.selling.order.OrderItem;
import selling.sunshine.model.OrderPool;
import selling.sunshine.model.RefundConfig;
import common.sunshine.model.selling.goods.Goods4Agent;
import common.sunshine.pagination.DataTablePage;
import common.sunshine.pagination.DataTableParam;
import common.sunshine.pagination.MobilePage;
import common.sunshine.pagination.MobilePageParam;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderDaoImpl extends BaseDao implements OrderDao {
    private Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);

    private Object lock = new Object();

    /**
     * 添加订单,在order表中添加记录,并将所有的订单项添加到order item中
     *
     * @param order
     * @return
     */
    @Transactional
    @Override
    public ResultData insertOrder(Order order) {
        ResultData result = new ResultData();
        List<OrderItem> orderItems = order.getOrderItems();
        synchronized (lock) {
            try {
                order.setOrderId(IDGenerator.generate("ODR"));
                sqlSession.insert("selling.order.insert", order);
                for (OrderItem orderItem : orderItems) {
                    orderItem.setOrderItemId(IDGenerator.generate("ORI"));
                    orderItem.setOrder(order);
                }
                sqlSession.insert("selling.order.item.insertBatch", orderItems);
                Map<String, Object> condition = new HashMap<>();
                condition.put("orderId", order.getOrderId());
                order = sqlSession.selectOne("selling.order.query", condition);
                result.setData(order);
            } catch (Exception e) {
                logger.error(e.getMessage());
                result.setResponseCode(ResponseCode.RESPONSE_ERROR);
                result.setDescription(e.getMessage());
            } finally {
                return result;
            }
        }
    }

    /**
     * 查询符合查询条件的订单列表
     *
     * @param condition
     * @return
     */
    @Override
    public ResultData queryOrder(Map<String, Object> condition) {
        ResultData result = new ResultData();
        try {
            condition = handle(condition);
            List<Order> list = sqlSession.selectList("selling.order.query", condition);
            result.setData(list);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription(e.getMessage());
        } finally {
            return result;
        }
    }

    @Override
    public ResultData queryOrderByPage(Map<String, Object> condition, MobilePageParam param) {
        ResultData result = new ResultData();
        MobilePage<Order> page = new MobilePage<>();
        ResultData total = queryOrder(condition);
        if (total.getResponseCode() != ResponseCode.RESPONSE_OK) {
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription(total.getDescription());
            return result;
        }
        page.setTotal(((List<Order>) total.getData()).size());
        List<Order> current = queryOrderByPage(condition, param.getStart(), param.getLength());
        if (current.size() == 0) {
            result.setResponseCode(ResponseCode.RESPONSE_NULL);
        }
        page.setData(current);
        result.setData(page);
        return result;
    }

    @Override
    public ResultData queryOrderByPage(Map<String, Object> condition, DataTableParam param) {
        ResultData result = new ResultData();
        DataTablePage<Order> page = new DataTablePage<>();
        condition = handle(condition);
        if (!StringUtils.isEmpty(param.getsSearch())) {
            String searchParam = param.getsSearch().replace("/", "-");
            condition.put("search", "%" + searchParam + "%");
        }
        ResultData total = queryOrder(condition);
        if (total.getResponseCode() != ResponseCode.RESPONSE_OK) {
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription(total.getDescription());
            return result;
        }
        page.setiTotalRecords(((List) total.getData()).size());
        page.setiTotalDisplayRecords(((List) total.getData()).size());
        List<Order> current = queryOrderByPage(condition, param.getiDisplayStart(), param.getiDisplayLength());
        if (current.size() == 0) {
            result.setResponseCode(ResponseCode.RESPONSE_NULL);
        }
        page.setData(current);
        result.setData(page);
        return result;
    }

    @Transactional
    @Override
    public ResultData updateOrder(Order order) {
        ResultData result = new ResultData();
        synchronized (lock) {
            try {
                sqlSession.update("selling.order.update", order);
                Map<String, Object> condition = new HashMap<>();
                condition.put("orderId", order.getOrderId());
                Order target = sqlSession.selectOne("selling.order.query", condition);
                if (target == null) {
                    result.setResponseCode(ResponseCode.RESPONSE_ERROR);
                    result.setDescription("Order does not exist.");
                }
                List<OrderItem> primary = target.getOrderItems();
                List<OrderItem> now = order.getOrderItems();
                List<OrderItem> toDelete = new ArrayList<OrderItem>();
                List<OrderItem> toAdd = new ArrayList<OrderItem>();
                List<OrderItem> toModify = new ArrayList<OrderItem>();
                if (primary.size() == 0) {
                    for (OrderItem item : now) {
                        item.setOrderItemId(IDGenerator.generate("ORI"));
                        Order temp = new Order();
                        temp.setOrderId(order.getOrderId());
                        item.setOrder(temp);
                    }
                    sqlSession.insert("selling.order.item.insertBatch", now);
                }
                if (now.size() == 0) {
                    toDelete.addAll(primary);
                    sqlSession.delete("selling.order.item.delete", toDelete);
                }
                if (primary.size() != 0 && now.size() != 0) {
                    for (OrderItem nowItem : now) {
                        boolean isMatch = false;
                        for (OrderItem primaryItem : primary) {
                            if (nowItem.getOrderItemId().equals(primaryItem.getOrderItemId())) {
                                toModify.add(nowItem);
                                primary.remove(primaryItem);
                                isMatch = true;
                                break;
                            }
                        }
                        if (!isMatch) {
                            toAdd.add(nowItem);
                        }
                    }
                }
                toDelete.addAll(primary);
                if (toDelete.size() > 0) {
                    sqlSession.delete("selling.order.item.delete", toDelete);
                }
                if (toAdd.size() > 0) {
                    for (OrderItem item : toAdd) {
                        item.setOrderItemId(IDGenerator.generate("ORI"));
                        Order temp = new Order();
                        temp.setOrderId(order.getOrderId());
                        item.setOrder(temp);
                    }
                    sqlSession.insert("selling.order.item.insertBatch", toAdd);
                }
                if (toModify.size() > 0) {
                    for (OrderItem item : toModify) {
                        sqlSession.update("selling.order.item.update", item);
                    }
                }
                result.setData(order);
            } catch (Exception e) {
                logger.error(e.getMessage());
                result.setResponseCode(ResponseCode.RESPONSE_ERROR);
                result.setDescription(e.getMessage());
            } finally {
                return result;
            }
        }
    }

    private List<Order> queryOrderByPage(Map<String, Object> condition, int start, int length) {
        List<Order> result = new ArrayList<>();
        try {
            result = sqlSession.selectList("selling.order.query", condition, new RowBounds(start, length));
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            return result;
        }
    }

    @Override
    public ResultData updateOrderLite(Order order) {
        ResultData result = new ResultData();
        synchronized (lock) {
            try {
                sqlSession.update("selling.order.update", order);
                result.setData(order);
            } catch (Exception e) {
                logger.error(e.getMessage());
                result.setResponseCode(ResponseCode.RESPONSE_ERROR);
                result.setDescription(e.getMessage());
            } finally {
                return result;
            }
        }
    }

    @Override
    public ResultData sumOrder() {
        ResultData result = new ResultData();
        // 获取当月的前一个月的日期 xxxx年xx月
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        Timestamp lastMonth = new Timestamp(c.getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        String date = dateFormat.format(lastMonth);
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("date", date + "%");
            List<Map<String, Object>> sumOrderList = sqlSession.selectList(
                    "selling.order.pool.sumOrder", condition);
            List<Map<String, Object>> sumCustomerOrderList = sqlSession.selectList(
                    "selling.customer.order.sumCustomerOrder", condition);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (sumOrderList.size() == 0) {
                resultList = sumCustomerOrderList;
            } else if (sumCustomerOrderList.size() == 0) {
                resultList = sumOrderList;
            } else {
                boolean flag = false;
                for (int i = 0; i < sumOrderList.size(); i++) {
                    for (int j = 0; j < sumCustomerOrderList.size(); j++) {
                        if ((sumOrderList.get(i).get("agent").equals(sumCustomerOrderList.get(j).get("agent"))) && (sumOrderList.get(i).get("goods").equals(sumCustomerOrderList.get(j).get("goods")))) {
                            flag = true;
                            sumCustomerOrderList.get(j).put("quantity", Integer.parseInt(sumCustomerOrderList.get(j)
                                    .get("quantity").toString()) + Integer.parseInt(sumOrderList.get(i)
                                    .get("quantity").toString()));
                            sumCustomerOrderList.get(j).put("price", Double.parseDouble(sumCustomerOrderList.get(j)
                                    .get("price").toString()) + Double.parseDouble(sumOrderList.get(i)
                                    .get("price").toString()));
                            break;
                        }
                    }
                    if (!flag) {
                        resultList.add(sumOrderList.get(i));
                    } else {
                        flag = false;
                    }
                }
                resultList.addAll(sumCustomerOrderList);
            }
            if (resultList.size() != 0) {
                Map<String, Object> configCondition = new HashMap<>();
                for (int i = 0; i < resultList.size(); i++) {
                    OrderPool pool = new OrderPool();
                    pool.setOrderPoolId(IDGenerator.generate("OPL"));
                    pool.setPrice(Double.parseDouble(resultList.get(i).get("price")
                            .toString()));
                    pool.setQuantity(Integer.parseInt(resultList.get(i).get("quantity").toString()));
                    pool.setPoolDate(new Date(c.getTimeInMillis()));
                    Agent agent = new Agent();
                    agent.setAgentId((String) resultList.get(i).get("agent"));
                    pool.setAgent(agent);
                    Goods4Agent goods = new Goods4Agent();
                    goods.setGoodsId((String) resultList.get(i).get("goods"));
                    pool.setGoods(goods);
                    configCondition.put("goodsId", resultList.get(i).get("goods"));
                    configCondition.put("blockFlag", false);
                    List<RefundConfig> configs=sqlSession.selectList("selling.refund.config.query", configCondition);
                    if (configs.size()>0) {//当返现配置存在时才能生成order pool
                    	 boolean flag=false;
                         RefundConfig configMonth = null;
                         RefundConfig configAll = null;
                         for (RefundConfig config:configs) {
     						if (!config.isUniversal()) {
     							flag=true;
     							configMonth=config;
     						}else {
     							configAll=config;
     						}
     					}
                         if (flag) {
     						int monthConfig=configMonth.getUniversalMonth();
     						configCondition.clear();
     						configCondition.put("agentId", agent.getAgentId());
     						common.sunshine.model.selling.agent.Agent agent2 = sqlSession.selectOne("selling.agent.query", configCondition);
     						
     						Calendar calendar = Calendar.getInstance();
     						calendar.setTime(agent2.getCreateAt());
     						int month=c.get(Calendar.MONTH)-calendar.get(Calendar.MONTH);
     						System.out.println(c.get(Calendar.MONTH));
     						if(month<monthConfig){
     							pool.setRefundConfig(configMonth);
     		                    if (pool.getQuantity() >= configMonth.getAmountTrigger()) {
     		                        pool.setBlockFlag(false);
     		                        pool.setRefundAmount(Double.parseDouble(resultList.get(i).get("quantity").toString()) * configMonth.getLevel1Percent());
     		                    } else {
     		                        pool.setBlockFlag(true);
     		                        pool.setRefundAmount(0);
     		                    }		                    
     						}else {
     							pool.setRefundConfig(configAll);
     		                    if (pool.getQuantity() >= configAll.getAmountTrigger()) {
     		                        pool.setBlockFlag(false);
     		                        pool.setRefundAmount(Double.parseDouble(resultList.get(i).get("quantity").toString()) * configAll.getLevel1Percent());
     		                    } else {
     		                        pool.setBlockFlag(true);
     		                        pool.setRefundAmount(0);
     		                    }		      
     						}
     					}else {
     						 RefundConfig config = configs.get(0);
     		                    pool.setRefundConfig(config);
     		                    if (pool.getQuantity() >= config.getAmountTrigger()) {
     		                        pool.setBlockFlag(false);
     		                        pool.setRefundAmount(Double.parseDouble(resultList.get(i).get("quantity").toString()) * config.getLevel1Percent());
     		                    } else {
     		                        pool.setBlockFlag(true);
     		                        pool.setRefundAmount(0);
     		                    }		                   
     					}
                         sqlSession.insert("selling.order.pool.insert", pool);
                         configCondition.clear();
					}                   
                }              
            }
            result.setData(resultList);
            result.setResponseCode(ResponseCode.RESPONSE_OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription(e.getMessage());
        } finally {
            return result;
        }

    }

}
