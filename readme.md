# Tarea 09 - Gestor de Descargas

**Niveles realizados:** Nivel 1 y Nivel 2.

---

## Nivel 1

### Clase Descarga
Hereda de `Thread` para simular la descarga de un archivo. En el `run()`, genera un tiempo aleatorio entre 100 y 500 ms por bloque y simula el avance del 10% al 100% con un bucle y `Thread.sleep()`. Al acabar, guarda la duración total calculando la diferencia con `System.currentTimeMillis()`.

```java
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
```

### Clase GestorDescargas

Es la clase principal. Crea las descargas y las arranca todas a la vez en un primer bucle con .start(). Después, en otro bucle separado usa .join() para que el hilo principal espere a que terminen todas antes de calcular el tiempo total y mostrar la comparativa.

```java
public class GestorDescargas {

    public static void main(String[] args) {

        String[] nombresArchivos = {
            "cuarzos.png",
            "meditacion.mp4",
            "mantras.mp3",
            "horoscopo.pdf"
        };

        Descarga[] descargas = new Descarga[nombresArchivos.length];
        for (int i = 0; i < nombresArchivos.length; i++) {
            descargas[i] = new Descarga(nombresArchivos[i]);
        }

        long tiempoInicioReal = System.currentTimeMillis();

        // 1. Arrancar todas las descargas
        for (Descarga descarga : descargas) {
            descarga.start();
        }

        // 2. Esperar a que terminen las descargas
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

        System.out.println("Todas las descargas han terminado.");
        System.out.println("Tiempo real: " + tiempoRealTotal + " ms");
        System.out.println("Si se hubieran descargado una detrás de otra: " + tiempoSumaSecuencial + " ms");
    }
}
```
## Pruebas del Nivel 1:
| **Ejecución** |     **Descarga más lenta**     | **Tiempo real(ms)** | **Suma** |
|:---:|:------------------------------:|:-------------------:|:--------:|
|1|   meditacion.mp4 (4658 ms)     |       4659 ms       | 12442 ms |
|2|   mantras.mp3 (4720 ms)        |       4721 ms       | 11900 ms |
|3|   horoscopo.pdf (4120 ms)      |       4122 ms       | 10850 ms |

## Respuestas a las preguntas:
¿Por qué el tiempo real es mucho menor que la suma?

Porque las descargas van todas a la vez en paralelo. Mientras un hilo está parado esperando en su Thread.sleep(), la CPU aprovecha para avanzar los otros. Por eso el tiempo total es casi igual a lo que tarda la descarga más lenta, en vez de sumar todas (12442 ms).

¿Qué pasa si hacéis start() y join() dentro del mismo bucle?

Se rompe el paralelismo. El programa arranca una descarga y se queda bloqueado esperando a que termine antes de pasar a la siguiente. Pasa a ser una ejecución secuencial normal. Al probarlo así, el tiempo real total fue de 12440 ms, casi igual a la suma de los tiempos.

## Nivel 2
### Clase Monitor

Implementa Runnable para usar la otra forma de crear hilos en Java. Corre en un bucle comprobando cada 500 ms cuántas descargas siguen vivas con .isAlive(). Cuando el contador llega a cero, avisa y termina de forma limpia.

```java
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
```

## Clase GestorDescargas (Actualización Nivel 2)

Se le añade la lectura de nombres por línea de comandos (args). Si no se le pasa nada, usa la lista por defecto. También arranca el hilo del Monitor justo después de iniciar las descargas.

```java
public class GestorDescargas {

    public static void main(String[] args) {

        // Capturar nombres desde línea de comandos (Nivel 2)
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

        // 2. Crear e iniciar el Monitor
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

        int tiempoSumaSecuencial = 0;
        for (Descarga descarga : descargas) {
            tiempoSumaSecuencial += descarga.getTiempoTotal();
        }

        System.out.println("Todas las descargas han terminado.");
        System.out.println("Tiempo real: " + tiempoRealTotal + " ms");
        System.out.println("Si se hubieran descargado una detrás de otra: " + tiempoSumaSecuencial + " ms");
    }
}
```

## Salida por consola

```
[mantras.mp3] 10%
[cuarzos.png] 10%
[horoscopo.pdf] 10%
[mantras.mp3] 20%
[meditacion.mp4] 10%
[cuarzos.png] 20%
[Monitor] Descargas en curso: 4
[mantras.mp3] 30%
[horoscopo.pdf] 20%
[cuarzos.png] 30%
[mantras.mp3] 40%
[mantras.mp3] 50%
[meditacion.mp4] 20%
[cuarzos.png] 40%
[Monitor] Descargas en curso: 4
[horoscopo.pdf] 30%
[mantras.mp3] 60%
[cuarzos.png] 50%
[mantras.mp3] 70%
[meditacion.mp4] 30%
[horoscopo.pdf] 40%
[cuarzos.png] 60%
[mantras.mp3] 80%
[Monitor] Descargas en curso: 4
[mantras.mp3] 90%
[cuarzos.png] 70%
[horoscopo.pdf] 50%
[mantras.mp3] completada en 1851 ms
[meditacion.mp4] 40%
[cuarzos.png] 80%
[Monitor] Descargas en curso: 3
[horoscopo.pdf] 60%
[cuarzos.png] 90%
[meditacion.mp4] 50%
[cuarzos.png] completada en 2397 ms
[horoscopo.pdf] 70%
[Monitor] Descargas en curso: 2
[meditacion.mp4] 60%
[horoscopo.pdf] 80%
[Monitor] Descargas en curso: 2
[horoscopo.pdf] 90%
[meditacion.mp4] 70%
[Monitor] Descargas en curso: 2
[horoscopo.pdf] completada en 3536 ms
[meditacion.mp4] 80%
[Monitor] Descargas en curso: 1
[meditacion.mp4] 90%
[Monitor] Descargas en curso: 1
[meditacion.mp4] completada en 4658 ms
Todas las descargas han terminado.
Tiempo real: 4659 ms
Si se hubieran descargado una detrás de otra: 12442 ms
[Monitor] No queda ninguna descarga en curso
```

## Uso de IA

En esta práctica he utilizado Gemini para:
* Ayudarme a organizar este readme.md.
* Repasar y resolver dudas sobre los hilos en Java y el uso de `join()` y `isAlive()`.
* Formatear y revisar que la salida por consola del programa fuera clara.
