package selling.sunshine.security;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import common.sunshine.model.selling.agent.Agent;
import common.sunshine.model.selling.user.Role;
import common.sunshine.model.selling.user.User;
import selling.sunshine.service.AgentService;
import selling.sunshine.service.UserService;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunshine on 5/26/16.
 */
public class OAuthRealm extends AuthorizingRealm {

    private Logger logger = LoggerFactory.getLogger(OAuthRealm.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        User user = (User) principalCollection.getPrimaryPrincipal();
        Role role = user.getRole();
        info.addRole(role.getName());
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String wechat = token.getUsername();
        Map<String, Object> condition = new HashMap<>();
        condition.put("wechat", wechat);
        condition.put("blockFlag", false);
        ResultData fetchResponse = agentService.fetchAgent(condition);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            Agent agent = ((List<Agent>) fetchResponse.getData()).get(0);
            condition.clear();
            condition.put("username", agent.getPhone());
            condition.put("blockFlag", false);
            fetchResponse = userService.fetchUser(condition);
            if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
                User user = (User) fetchResponse.getData();
                if (user != null) {
                    return new SimpleAuthenticationInfo(user, token.getPassword(), getName());
                }
            }
        }
        return null;
    }
}
