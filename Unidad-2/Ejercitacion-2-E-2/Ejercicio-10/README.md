# Ejercicio 10 - Administrador concurrente de turnos

Recepción y autoservicio consultan la misma agenda. Veinte hilos intentan reservar el mismo horario y una sección sincronizada realiza conjuntamente la comprobación y asignación. Una cancelación inexistente no altera la agenda; una cancelación válida libera el horario.

![Salida real del ejercicio 10](captura/salida.png)

Desde esta carpeta:

```powershell
javac -d out src\Main.java
java -cp out Main
```
