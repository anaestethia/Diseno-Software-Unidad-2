# Ejercicio 7 - Generador central de números de atención

Tres terminales alternan 21 solicitudes secuenciales al mismo contador Singleton. Cada ticket conserva su terminal, el historial está protegido y consultar el último número no modifica el contador.

![Salida real del ejercicio 7](captura/salida.png)

Desde esta carpeta:

```powershell
javac -d out src\Main.java
java -cp out Main
```
