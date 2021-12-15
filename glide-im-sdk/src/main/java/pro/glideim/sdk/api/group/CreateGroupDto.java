package pro.glideim.sdk.api.group;

public class CreateGroupDto {
    private String name;

    public CreateGroupDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
