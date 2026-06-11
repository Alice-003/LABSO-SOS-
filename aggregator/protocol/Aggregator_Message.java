package aggregator.protocol;

public class Aggregator_Message {

    private final String command;
    private final String[] args;

    // costruttore privato
    private Aggregator_Message(String command, String[] args) {
        this.command = command;
        this.args    = args;
    }

    // factory method
    public static Aggregator_Message parse(String x) {
        if (x == null || x.isBlank()) {
            throw new IllegalArgumentException("Messaggio non valido");
        }

        String[] parts = x.trim().split(" ", 3);
        String command = parts[0].toUpperCase();

        String[] args;
        if (parts.length > 1) {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        } else {
            args = new String[0];
        }

        return new Aggregator_Message(command, args);
    }

    // getters
    public String getCommand() {
        return command;
    }

    public String getArg(int index) {
        if (index < 0 || index >= args.length) {
            throw new IndexOutOfBoundsException("Argomento " + index + " non esiste");
        }
        return args[index];
    }

    public int getArgCount() {
        return args.length;
    }

    public boolean hasAtLeast(int n) {
        return args.length >= n;
    }

    @Override
    public String toString() {
        if (args.length == 0) return command;
        return command + " " + String.join(" ", args);
    }
}
