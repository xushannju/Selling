package selling.sunshine.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import selling.sunshine.form.AdminForm;
import selling.sunshine.form.AdminLoginForm;
import selling.sunshine.model.Admin;
import selling.sunshine.model.Role;
import selling.sunshine.model.User;
import selling.sunshine.service.AdminService;
import selling.sunshine.service.RoleService;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunshine on 4/10/16.
 */
@RequestMapping("/")
@RestController
public class PlatformController {

    private Logger logger = LoggerFactory.getLogger(PlatformController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ModelAndView index() {
        ModelAndView view = new ModelAndView();
        view.setViewName("redirect:/login");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public ModelAndView login() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/login");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ModelAndView login(@Valid AdminLoginForm form, BindingResult result, HttpServletRequest request) {
        ModelAndView view = new ModelAndView();
        if (result.hasErrors()) {
            view.setViewName("redirect:/login");
            return view;
        }
        try {
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated() && subject.getPrincipal() != null) {
                subject.logout();
            }
            subject.login(new UsernamePasswordToken(form.getUsername(), form.getPassword()));
            User user = (User) subject.getPrincipal();
            HttpSession session = request.getSession();
            session.setAttribute("role", user.getRole().getName());
            switch (user.getRole().getName()) {
                case "admin":
                    view.setViewName("redirect:/dashboard");
                    return view;
                case "auditor":
                    view.setViewName("redirect:/agent/check");
                    return view;
                case "express":
                    view.setViewName("redirect:/order/check");
                    return view;
            }
        } catch (Exception e) {
            view.setViewName("redirect:/login");
            return view;
        }
        view.setViewName("redirect:/dashboard");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/register")
    public ModelAndView register() {
        ModelAndView view = new ModelAndView();
        Map<String, Object> condition = new HashMap<>();
        condition.put("blockFlag", false);
        ResultData fetchResponse = roleService.queryRole(condition);
        if (fetchResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            List<Role> list = (List<Role>) fetchResponse.getData();
            view.addObject("roles", list);
        }
        view.setViewName("/backend/admin/admin_register");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public ModelAndView register(@Valid AdminForm form, BindingResult result) {
        ModelAndView view = new ModelAndView();

        if (result.hasErrors()) {
            view.setViewName("redirect:/register");
            return view;
        }
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("username", form.getUsername());
            ResultData queryResult = adminService.fetchAdmin(condition);
            if (((List<Admin>) queryResult.getData()).size() != 0) {
                view.setViewName("redirect:/register");
                return view;
            }
            Role role = new Role();
            role.setRoleId(form.getRole());
            Admin admin = new Admin(form.getUsername(), form.getPassword());
            ResultData resultData = adminService.createAdmin(admin, role);
            if (resultData.getResponseCode() == ResponseCode.RESPONSE_OK) {
                view.setViewName("redirect:/admin/overview");
                return view;
            } else {
                view.setViewName("redirect:/register");
                return view;
            }
        } catch (Exception e) {
            view.setViewName("redirect:/register");
            return view;
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/{adminId}")
    public ModelAndView fetchAdmin(@PathVariable("adminId") String adminId) {
        ModelAndView view = new ModelAndView();
        ResultData result = new ResultData();
        Map<String, Object> condition = new HashMap<>();
        condition.put("adminId", adminId);
        result = adminService.fetchAdmin(condition);
        if (result.getResponseCode() == ResponseCode.RESPONSE_ERROR) {
            view.setViewName("/backend/admin/admin_management");
            return view;
        }
        Admin admin = ((List<Admin>) result.getData()).get(0);
        view.addObject("admin", admin);
        view.setViewName("/backend/admin/admin_update");
        return view;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/modify/{adminId}")
    public ModelAndView updateAdmin(@PathVariable("adminId") String adminId, @Valid AdminLoginForm adminLoginForm, BindingResult result) {
        ResultData response = new ResultData();
        ModelAndView view = new ModelAndView();
        if (result.hasErrors()) {
            view.setViewName("redirect:/manage");
            return view;
        }
        Admin admin = new Admin(adminLoginForm.getUsername(), adminLoginForm.getPassword());
        admin.setAdminId(adminId);
        response = adminService.updateAdmin(admin);
        if (response.getResponseCode() == ResponseCode.RESPONSE_OK) {
            view.setViewName("redirect:/manage");
            return view;
        } else {
            view.setViewName("redirect:/manage");
            return view;
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/delete/{adminId}")
    public ResultData deleteAdmin(@PathVariable("adminId") String adminId) {
        ResultData response = new ResultData();
        ModelAndView view = new ModelAndView();

        Map<String, Object> condition = new HashMap<>();
        response = adminService.fetchAdmin(condition);
        //当只有一个admin时不允许删除
        if (((List<Admin>) response.getData()).size() == 1) {
            response.setResponseCode(ResponseCode.RESPONSE_NULL);
            return response;
        }

        Admin admin = new Admin();
        admin.setAdminId(adminId);
        response = adminService.deleteAdmin(admin);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dashboard")
    public ModelAndView dashboard() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/dashboard");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/logout")
    public ModelAndView logout() {
        ModelAndView view = new ModelAndView();
        Subject subject = SecurityUtils.getSubject();
        if (subject != null) {
            subject.logout();
        }
        view.setViewName("redirect:/login");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/navigate")
    public ModelAndView navigate() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/navigate");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/log")
    public ModelAndView log() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/backend/system/log");
        return view;
    }
}
