import java.io.Serializable;
import java.util.ArrayList;

public class GroupChat implements Serializable{
    private transient int groupID;
    private transient User groupOwner;
    private transient ArrayList<User> groupMembers = new ArrayList<>();
    private transient ArrayList<User> managers = new ArrayList<>();
    private transient String groupName;
    private transient String avatarID;

    public GroupChat(int id, User owner, ArrayList<User> members, String name, String aid) throws Exception{
        if(members.size() > 8) {
            throw new Exception();
        } else {
            groupID = id;
            groupOwner = owner;
            groupMembers = members;
            managers.add(owner);
            groupName = name;
            avatarID = aid;
        }
    }

    public String getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(String id) {
        avatarID = id;
    }

    public boolean addMember(User member) {
        if(groupMembers.size() + 1 > 8) {
            return false;
        } else {
            groupMembers.add(member);
            return true;
        }
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

    public ArrayList<User> getManagers() {
        return managers;
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

    public boolean addManager(ArrayList<User> users) {
        if(managers.size() + users.size() > 5) {
            return false;
        }
        for(int i = 0; i < users.size(); i++) {
            managers.add(users.get(i));
        }
        return true;
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
