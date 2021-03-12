public class UserInfo {
    private String username;
    private int uid;
    private String address;
    public UserInfo(String username , int uid , String address){
        this.username = username;
        this.uid = uid;
        this.address = address;
    }
    public String getUsername(){
        return username;
    }
    public int getUid(){
        return uid;
    }
    public String getAddress(){
        return address;
    }
}
