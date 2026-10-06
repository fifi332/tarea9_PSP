import java.util.Random;

public class Descarga extends Thread {

    private String nombreArchivo;
    private Random random;
    private int tiempoTotal;

    public Descarga(String nombreArchivo) {
        super("Descarga-" + nombreArchivo);
        this.nombreArchivo = nombreArchivo;
        this.random = new Random();
        this.tiempoTotal = 0;
    }

    public void run() {
        // Genera un tiempo al azar entre 100 y 500 ms por bloque
        int tiempoBloque = random.nextInt(401) + 100;
        long inicio = System.currentTimeMillis();

        for (int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(tiempoBloque);
            } catch (InterruptedException e) {
                return;
            }
            int porcentaje = i * 10;
            if (porcentaje < 100) {
                System.out.println("[" + nombreArchivo + "] " + porcentaje + "%");
            }
        }

        long fin = System.currentTimeMillis();
        this.tiempoTotal = (int) (fin - inicio);
        System.out.println("[" + nombreArchivo + "] completada en " + tiempoTotal + " ms");
    }

    public int getTiempoTotal() {
        return tiempoTotal;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}