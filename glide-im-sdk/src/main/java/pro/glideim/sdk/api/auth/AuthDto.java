package pro.glideim.sdk.api.auth;

public class AuthDto {
    private String token;
    private int device;

    public AuthDto(String token, int device) {
        this.token = token;
        this.device = device;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }
}
