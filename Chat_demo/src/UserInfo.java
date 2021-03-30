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

/*
public static void getUserAddress(Connection con) throws SQLException {
    String query = "select Address from UserInfo where UID = '" + Uid + "'";
    try(Statement stmt = con.createStetement()) {
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
            String password = rs.getString("Password");
            System.out.println(password);
        }
    } catch (SQLException e) {
        JDBCTutorialUtilities.printSQLException(e);
    }
}
 */