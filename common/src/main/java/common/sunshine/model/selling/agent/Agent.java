package common.sunshine.model.selling.agent;

import common.sunshine.model.Entity;
import org.springframework.util.StringUtils;


/**
 * Created by sunshine on 4/7/16.
 */
public class Agent extends Entity {
    private String agentId;
    private String name;
    private String gender;
    private String phone;
    private String address;
    private String card;
    private String password;
    private String wechat;
    private boolean granted;
    private double coffer;
    private double agentRefund;
    private int claimScale;
    private boolean customerService;
    private common.sunshine.model.selling.agent.lite.Agent upperAgent;

    public Agent() {
        super();
        granted = false;
        claimScale = 0;
    }

    public Agent(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }

    public Agent(String name, String gender, String phone, String address, String card, String password) {
        this();
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.card = card;
        this.password = password;
    }

    public Agent(String name, String gender, String phone, String address, String card, String password, String wechat) {
        this(name, gender, phone, address, card, password);
        if (!StringUtils.isEmpty(wechat)) {
            this.wechat = wechat;
        }
    }

    public Agent(String name, String gender, String phone, String address, String card, String password, String wechat, int claimScale) {
        this(name, gender, phone, address, card, password, wechat);
        this.claimScale = claimScale;
    }

    public Agent(String agentId, String name, String gender, String phone, String address, String card, String password,
			String wechat, boolean granted, double coffer, double agentRefund, int claimScale, boolean customerService,
			common.sunshine.model.selling.agent.lite.Agent upperAgent) {
		super();
		this.agentId = agentId;
		this.name = name;
		this.gender = gender;
		this.phone = phone;
		this.address = address;
		this.card = card;
		this.password = password;
		this.wechat = wechat;
		this.granted = granted;
		this.coffer = coffer;
		this.agentRefund = agentRefund;
		this.claimScale = claimScale;
		this.customerService = customerService;
		this.upperAgent = upperAgent;
	}

	public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public double getCoffer() {
        return coffer;
    }

    public void setCoffer(double coffer) {
        this.coffer = coffer;
    }

    public common.sunshine.model.selling.agent.lite.Agent getUpperAgent() {
        return upperAgent;
    }

    public void setUpperAgent(common.sunshine.model.selling.agent.lite.Agent upperAgent) {
        this.upperAgent = upperAgent;
    }

    public double getAgentRefund() {
        return agentRefund;
    }

    public void setAgentRefund(double agentRefund) {
        this.agentRefund = agentRefund;
    }

    public int getClaimScale() {
        return claimScale;
    }

    public void setClaimScale(int claimScale) {
        this.claimScale = claimScale;
    }

	public boolean isCustomerService() {
		return customerService;
	}

	public void setCustomerService(boolean customerService) {
		this.customerService = customerService;
	}
    
    
}
