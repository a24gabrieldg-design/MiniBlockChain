public class Worker {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Uso: Worker <hashAnterior> <datos> <dificultad> <id> <totalWorkers>");
            System.exit(1);
        }

        String hashAnterior = args[0];
        String datos = args[1];
        int dificultad = Integer.parseInt(args[2]);
        int id = Integer.parseInt(args[3]);
        int total = Integer.parseInt(args[4]);

        String prefijo = "0".repeat(dificultad);


        long nonce = id;
        while (true) {
            String hash = Hash.sha256(hashAnterior + "|" + datos + "|" + nonce);
            if (hash.startsWith(prefijo)) {
                System.out.println(nonce + " " + hash);
                System.exit(0);
            }
            nonce += total;
        }
    }
}
