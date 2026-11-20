# Mini block chain

Proyecto de PSP: una cadena de bloques sencilla en Java. El programa principal construye la cadena y, para cada bloque, lanza 4 procesos worker que buscan un nonce en paralelo (minado).

## Ficheros

- `Principal.java`: programa principal. Pide la seed y el fichero, construye la cadena y valida las soluciones.
- `Worker.java`: proceso que busca el nonce.
- `Hash.java`: calcula el SHA-256.
- `datos.txt`: fichero de ejemplo (una línea = un bloque).

## Compilar

```
javac *.java
```

## Ejecutar

```
java Principal
```

El programa pide la seed (cualquier texto) y la ruta del fichero (por ejemplo `datos.txt`).

## Cómo establecer la dificultad

La dificultad es la constante `DIFICULTAD` al principio de `Principal.java`:

```java
private static final int DIFICULTAD = 4;
```

Es el número de ceros que debe tener al principio cada hash. Hay que volver a compilar después de cambiarla. Con valores mayores (6 o más) tarda bastante más.

## Cómo funciona

- Hash inicial = SHA-256(seed).
- Hash de un bloque = SHA-256(hash anterior + "|" + datos + "|" + nonce).
- El worker `i` (0 a 3) prueba los nonces `i, i+4, i+8...`, así ninguno repite los de otro.
- El worker escribe `nonce hash` por su salida estándar; el principal lo lee, recalcula el hash y comprueba que coincide y que empieza por los ceros pedidos.
- Con la primera solución válida, el principal termina los workers y espera a que acaben antes de pasar al siguiente bloque.
