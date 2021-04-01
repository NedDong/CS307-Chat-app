import java.io.Serializable;
import java.util.ArrayList;

public class GroupChat implements Serializable{
    private transient int groupID;
    private transient User groupOwner;
    private transient ArrayList<User> groupMembers;
    private transient ArrayList<User> managers;
    private transient String groupName;

    public GroupChat(int id, User owner, ArrayList<User> members, String name) {
        groupID = id;
        groupOwner = owner;
        groupMembers = members;
        managers.add(owner);
        groupName = name;
    }

    public int getGroupID() {
        return groupID;
    }

    public User getGroupOwner() {
        return groupOwner;
    }

    public ArrayList<User> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void removeMember(User user) {
        int uid = user.getUid();
        for(int i = 0; i < groupMembers.size(); i++) {
            if(groupMembers.get(i).getUid() == uid) {
                groupMembers.remove(i);
            }
        }
        for(int j = 0; j < managers.size(); j++) {
            if(managers.get(j).getUid() == uid) {
                managers.remove(j);
            }
        }
    }

    public void addManager(ArrayList<User> users) {
        for(int i = 0; i < users.size(); i++) {
            managers.add(users.get(i));
        }
    }

    public void removeManager(User user) {
        for(int j = 0; j < managers.size(); j++) {
            if(managers.get(j).getUid() == user.getUid()) {
                managers.remove(j);
            }
        }
    }

    public void addGroupMembers(ArrayList<User> members) {
        for(int i = 0; i < members.size(); i++) {
            groupMembers.add(members.get(i));
        }
    }

    public void setGroupOwner(User groupOwner) {
        this.groupOwner = groupOwner;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
