public class GestorDescargas {

    public static void main(String[] args) {

        // NIVEL 2: Capturar nombres desde línea de comandos
        String[] nombresArchivos;
        if (args.length > 0) {
            nombresArchivos = args;
        } else {
            nombresArchivos = new String[]{
                    "cuarzos.png",
                    "meditacion.mp4",
                    "mantras.mp3",
                    "horoscopo.pdf"
            };
        }

        Descarga[] descargas = new Descarga[nombresArchivos.length];
        for (int i = 0; i < nombresArchivos.length; i++) {
            descargas[i] = new Descarga(nombresArchivos[i]);
        }

        long tiempoInicioReal = System.currentTimeMillis();

        // 1. Arrancar todas las descargas
        for (Descarga descarga : descargas) {
            descarga.start();
        }

        // 2. Crear e iniciar el Monitor (Nivel 2)
        Monitor monitor = new Monitor(descargas);
        Thread hiloMonitor = new Thread(monitor, "Hilo-Monitor");
        hiloMonitor.start();

        // 3. Esperar a que terminen las descargas
        for (Descarga descarga : descargas) {
            try {
                descarga.join();
            } catch (InterruptedException e) {
                System.err.println("Hilo principal interrumpido: " + e.getMessage());
            }
        }

        long tiempoFinReal = System.currentTimeMillis();
        long tiempoRealTotal = tiempoFinReal - tiempoInicioReal;

        // Calcular la suma de los tiempos individuales
        int tiempoSumaSecuencial = 0;
        for (Descarga descarga : descargas) {
            tiempoSumaSecuencial += descarga.getTiempoTotal();
        }

        // Mostrar el informe final tal como pide la plantilla del Nivel 1
        System.out.println("Todas las descargas han terminado.");
        System.out.println("Tiempo real: " + tiempoRealTotal + " ms");
        System.out.println("Si se hubieran descargado una detrás de otra: " + tiempoSumaSecuencial + " ms");
    }
}