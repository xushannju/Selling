package selling.sunshine.form;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by sunshine on 7/5/16.
 */
public class AdminForm {
    @NotBlank(message = "用户名不能为空/空格")
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
