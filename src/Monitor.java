public class Monitor implements Runnable {
    private Descarga[] descargas;

    public Monitor(Descarga[] descargas) {
        this.descargas = descargas;
    }

    public void run() {
        boolean hayActivas = true;

        while (hayActivas) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }

            int activas = 0;
            for (Descarga descarga : descargas) {
                if (descarga != null && descarga.isAlive()) {
                    activas++;
                }
            }

            if (activas > 0) {
                System.out.println("[Monitor] Descargas en curso: " + activas);
            } else {
                System.out.println("[Monitor] No queda ninguna descarga en curso");
                hayActivas = false;
            }
        }
    }
}