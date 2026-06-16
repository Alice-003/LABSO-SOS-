package client;

public class CommandHandler {
    public int gestioneComandi(String input) {
        switch (input.split(" ")[0].toLowerCase()) {
            case "listdata":
                if (input.split(" ").length == 2) {
                    if (input.split(" ")[1].toLowerCase().equals("local")) {
                        return 1;
                    } else if (input.split(" ")[1].toLowerCase().equals("remote")) {
                        return 2;
                    } else {
                        return -1;
                    }
                }
            case "quit":
                return 3;

            case "add":
                return 4;

            case "download":
                return 5;

            default:
                return -1;
        }
    }

    public static void main(String[] args) {

    }
}
