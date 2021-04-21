import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatFilter {
    ArrayList<String> bad = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {
        try {
            Scanner scanner = new Scanner(new File(badWordsFileName));
            while (scanner.hasNextLine()) {
                bad.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String filter(String msg) {
        String censor = "";
        for (int i = 0; i < bad.size(); i++) {
            if (msg.contains(bad.get(i))) {
                for (int j = 0; j < bad.get(i).length(); j++) {
                    censor += "*";
                }
                msg = msg.replaceAll(bad.get(i), censor);
            }
        }
        return msg;
    }
}
