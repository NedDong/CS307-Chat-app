import java.util.*;

public class Gender {
    private String gender;

    public Gender(String gender) {
        this.gender = gender;
    }

    //If no information about the gender, we will save it as no tell
    public Gender() {
        gender = "NO_TELL";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }
}
