package pro.glideim.sdk.api.auth;

public class LoginDto {
    private String account;
    private String password;
    private int device;

    public LoginDto(String account, String password, int device) {
        this.account = account;
        this.password = password;
        this.device = device;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }
}
