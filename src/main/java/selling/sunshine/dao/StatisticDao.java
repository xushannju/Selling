package selling.sunshine.dao;

import java.util.Map;

import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.utils.ResultData;

/**
 * Created by sunshine on 6/24/16.
 */
public interface StatisticDao {
    ResultData queryOrderSum();
    
    ResultData orderStatistics(Map<String, Object> condition);
    
    ResultData orderStatisticsByPage(DataTableParam param);
    
    ResultData agentGoodsMonthByPage(DataTableParam param);
    
    ResultData agentGoodsMonth(Map<String, Object> condition);
    
    ResultData agentGoodsByPage(DataTableParam param);
    
    ResultData agentGoods(Map<String, Object> condition);
    
    ResultData orderMonth();
    
    ResultData orderByYear();
    
    ResultData topThreeAgent();
    
    ResultData purchaseRecord(Map<String, Object> condition);
}
