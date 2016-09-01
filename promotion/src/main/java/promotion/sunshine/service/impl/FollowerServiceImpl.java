package promotion.sunshine.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import common.sunshine.model.wechat.Follower;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;
import promotion.sunshine.dao.FollowerDao;
import promotion.sunshine.service.FollowerService;

/**
 * Created by sunshine on 5/25/16.
 */
@Service
public class FollowerServiceImpl implements FollowerService {
    private Logger logger = LoggerFactory.getLogger(FollowerServiceImpl.class);

    @Autowired
    private FollowerDao followerDao;

    @Override
    public ResultData subscribe(Follower follower) {
        ResultData result = new ResultData();
        ResultData insertResponse = followerDao.insertFollower(follower);
        result.setResponseCode(insertResponse.getResponseCode());
        if (insertResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result.setData(insertResponse.getData());
        } else {
            result.setDescription(insertResponse.getDescription());
        }
        return result;
    }

    @Override
    public ResultData unsubscribe(String openId) {
    	logger.debug("1234567890");
    	logger.debug(openId);
        ResultData result = new ResultData();
        ResultData blockResponse = followerDao.blockFollower(openId);
        result.setResponseCode(blockResponse.getResponseCode());
        result.setDescription(blockResponse.getDescription());
        return result;
    }

	@Override
	public ResultData fetchFollower(Map<String, Object> condition) {
		ResultData result = new ResultData();
		ResultData queryResponse = followerDao.fetchFollower(condition);
		result.setResponseCode(queryResponse.getResponseCode());
		if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
			if (((List) queryResponse.getData()).isEmpty()) {
				result.setResponseCode(ResponseCode.RESPONSE_NULL);
			}
			result.setData(queryResponse.getData());
		} else if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_ERROR) {
			result.setDescription(queryResponse.getDescription());
		}
		return result;
	}
}
