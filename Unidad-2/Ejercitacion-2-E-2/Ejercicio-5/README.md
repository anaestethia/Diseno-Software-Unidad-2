# Ejercicio 5 - Gestor concurrente de reservas

Treinta hilos compiten por diez lugares. El Singleton usa inicialización segura con `Holder`; reservar (validar duplicado/cupo y registrar) y cancelar son métodos sincronizados para mantener el estado atómico.

![Salida real del ejercicio 5](captura/salida.png)

Desde esta carpeta:

```powershell
javac -d out src\Main.java
java -cp out Main
```
