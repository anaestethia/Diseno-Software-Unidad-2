# Ejercicio 4 - Administrador único de préstamos

Dos mostradores usan el mismo administrador de préstamos. El identificador corresponde a un ejemplar físico individual. Las operaciones son secuenciales; una devolución inexistente se rechaza sin alterar préstamos activos.

![Salida real del ejercicio 4](captura/salida.png)

Desde esta carpeta:

```powershell
javac -d out src\Main.java
java -cp out Main
```
