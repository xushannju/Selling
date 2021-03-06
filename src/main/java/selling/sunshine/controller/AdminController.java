package selling.sunshine.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import selling.sunshine.form.AdminLoginForm;
import common.sunshine.model.selling.admin.Admin;
import selling.sunshine.model.BackOperationLog;
import common.sunshine.model.selling.user.User;
import common.sunshine.pagination.DataTablePage;
import common.sunshine.pagination.DataTableParam;
import selling.sunshine.service.AdminService;
import selling.sunshine.service.LogService;
import selling.sunshine.service.ToolService;
import selling.sunshine.service.UserService;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by sunshine on 7/5/16.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ToolService toolService;
    
    @Autowired
    private LogService logService;

    @RequestMapping(method = RequestMethod.GET, value = "/overview")
    public ModelAndView overview() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/admin/admin_management");
        return view;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/overview")
    public DataTablePage<User> overview(DataTableParam param) {
        DataTablePage<User> result = new DataTablePage<>(param);
        if (StringUtils.isEmpty(param)) {
            return result;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("isAdmin", true);
        ResultData fetchResponse = userService.fetchUser(condition, param);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result = (DataTablePage<User>) fetchResponse.getData();
        }
        return result;
    }
    
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/delete/{userId}")
    public ResultData deleteAdmin(@PathVariable("userId") String userId,HttpServletRequest request) {
        ResultData response = new ResultData();

        Map<String, Object> condition = new HashMap<>();
        condition.put("userId", userId);
        User user=(User)userService.fetchUser(condition).getData();
        if ("ROL00000001".equals(user.getRole().getRoleId())) {
			condition.clear();
			condition.put("roleId", "ROL00000001");
			DataTableParam param=new DataTableParam();
			param.setiDisplayStart(0);
			param.setiDisplayLength(10);
			List<User> userList=((DataTablePage<User>)userService.fetchUser(condition,param).getData()).getData();
			if (userList.size()>1) {
				response = adminService.deleteAdmin(user.getAdmin());
		        return response;
			}else {
			       //当只有一个admin时不允许删除
				  response.setResponseCode(ResponseCode.RESPONSE_NULL);
		          return response;
			}
		}else {
	        response = adminService.deleteAdmin(user.getAdmin());
            Subject subject = SecurityUtils.getSubject();
            User targetUser = (User) subject.getPrincipal();
            if (targetUser == null) {
            	response.setResponseCode(ResponseCode.RESPONSE_ERROR);
                return response;
            }
            Admin targetAdmin = targetUser.getAdmin();
            BackOperationLog backOperationLog = new BackOperationLog(
            		targetAdmin.getUsername(), toolService.getIP(request) ,"管理员" + targetAdmin.getUsername() + "删除了管理员："+user.getAdmin().getUsername());
            logService.createbackOperationLog(backOperationLog);
	        return response;
		}

    }
    

    @RequestMapping(method = RequestMethod.POST, value = "/modify/{adminId}")
    public ModelAndView updateAdmin(@PathVariable("adminId") String adminId, @Valid AdminLoginForm adminLoginForm, BindingResult result,HttpServletRequest request) {
        ResultData response = new ResultData();
        ModelAndView view = new ModelAndView();
        if (result.hasErrors()) {
            view.setViewName("redirect:/admin/overview");
            return view;
        }
        Admin admin = new Admin(adminLoginForm.getUsername(), adminLoginForm.getPassword());
        admin.setAdminId(adminId);
        response = adminService.updateAdmin(admin);
        if (response.getResponseCode() == ResponseCode.RESPONSE_OK) {
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            if (user == null) {
            	view.setViewName("redirect:/admin/overview");
                return view;
            }
            Admin targetAdmin = user.getAdmin();
            BackOperationLog backOperationLog = new BackOperationLog(
            		targetAdmin.getUsername(), toolService.getIP(request) ,"管理员" + targetAdmin.getUsername() + "修改了管理员："+adminLoginForm.getUsername()+"的密码");
            logService.createbackOperationLog(backOperationLog);
        	view.setViewName("redirect:/admin/overview");
            return view;
        } else {
        	view.setViewName("redirect:/admin/overview");
            return view;
        }
    }
}
