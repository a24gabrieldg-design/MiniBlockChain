import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private static final int DIFICULTAD = 4;
    private static final int NUM_WORKERS = 4;

    public static class Bloque {
        public final String datos;
        public final String hashAnterior;
        public final long nonce;
        public final String hash;

        public Bloque(String datos, String hashAnterior, long nonce, String hash) {
            this.datos = datos;
            this.hashAnterior = hashAnterior;
            this.nonce = nonce;
            this.hash = hash;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce la seed: ");
        String seed = sc.nextLine();
        System.out.print("Introduce la ruta del fichero: ");
        String ruta = sc.nextLine();


        if (seed.trim().isEmpty()) {
            System.out.println("Error: la seed no puede estar vacía.");
            return;
        }
        if (DIFICULTAD < 1 || DIFICULTAD > 64) {
            System.out.println("Error: la dificultad debe estar entre 1 y 64.");
            return;
        }

        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: no se puede leer el fichero " + ruta);
            return;
        }
        if (lineas.isEmpty()) {
            System.out.println("No hay bloques que procesar.");
            return;
        }


        List<Bloque> cadena = new ArrayList<>();
        String hashAnterior = Hash.sha256(seed);

        try {
            for (int i = 0; i < lineas.size(); i++) {
                Bloque b = minar(i + 1, lineas.get(i), hashAnterior);
                cadena.add(b);
                hashAnterior = b.hash;
                System.out.println("Bloque incorporado a la cadena.");
                System.out.println("Workers de esta búsqueda: todos finalizados.");
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        System.out.println("Cadena completa: " + cadena.size() + " bloques.");
    }


    private static Bloque minar(int numero, String datos, String hashAnterior) throws Exception {
        List<Process> procesos = new ArrayList<>();
        List<BufferedReader> lectores = new ArrayList<>();
        String cp = System.getProperty("java.class.path");
        String prefijo = "0".repeat(DIFICULTAD);

        try {
            for (int i = 0; i < NUM_WORKERS; i++) {
                ProcessBuilder pb = new ProcessBuilder(
                        "java", "-cp", cp, "Worker",
                        hashAnterior, datos,
                        String.valueOf(DIFICULTAD), String.valueOf(i), String.valueOf(NUM_WORKERS)
                );
                pb.redirectErrorStream(true);
                Process p = pb.start();
                procesos.add(p);
                lectores.add(new BufferedReader(new InputStreamReader(p.getInputStream())));
            }

            while (true) {
                boolean algunoVivo = false;
                for (int i = 0; i < NUM_WORKERS; i++) {
                    boolean vivo = procesos.get(i).isAlive();
                    if (lectores.get(i).ready()) {
                        String linea = lectores.get(i).readLine();
                        String[] partes = linea.trim().split(" ");
                        if (partes.length == 2) {
                            String hashRecibido = partes[1];
                            try {
                                long nonce = Long.parseLong(partes[0]);

                                String hashRecalculado = Hash.sha256(hashAnterior + "|" + datos + "|" + nonce);
                                if (hashRecalculado.equals(hashRecibido) && hashRecalculado.startsWith(prefijo)) {
                                    System.out.println("BLOQUE " + numero + " VALIDADO");
                                    System.out.println("Datos: " + datos);
                                    System.out.println("Hash anterior: " + hashAnterior);
                                    System.out.println("Dificultad: " + DIFICULTAD);
                                    System.out.println("Worker ganador: " + i + " — PID: " + procesos.get(i).pid());
                                    System.out.println("Nonce encontrado: " + nonce);
                                    System.out.println("Hash recibido:");
                                    System.out.println(hashRecibido);
                                    System.out.println("Hash recalculado por el principal:");
                                    System.out.println(hashRecalculado);
                                    System.out.println("Validación: hashes coincidentes y prefijo de " + DIFICULTAD + " ceros correcto.");
                                    return new Bloque(datos, hashAnterior, nonce, hashRecalculado);
                                }
                            } catch (NumberFormatException e) {

                            }
                        }
                        System.out.println("Solución rechazada del worker " + i);
                    }
                    if (vivo) {
                        algunoVivo = true;
                    }
                }
                if (!algunoVivo) {
                    throw new Exception("Los workers terminaron sin dar una solución válida.");
                }
                Thread.sleep(10);
            }
        } finally {

            for (Process p : procesos) {
                p.destroy();
            }
            for (Process p : procesos) {
                p.waitFor();
            }
        }
    }
}
