# Prompts

Registro cronológico de pedidos al asistente de IA y del código o archivos que se generaron en cada uno.

## Para asistentes de IA

Al trabajar en este repositorio:

1. Leer este archivo antes de implementar cambios.
2. Al terminar cada pedido del usuario, **agregar una entrada nueva** en `## Historial` (siguiente número disponible).
3. Usar el formato `#### N. Título`, bloque **Prompt**, bloque **Clases / métodos generados**, y cerrar con `---`.

## Formato de cada entrada

| Parte | Contenido |
|-------|-----------|
| Título | `#### N.` + tema corto |
| **Prompt** | Texto del pedido (o resumen fiel) |
| **Clases / métodos generados** | Lista o bloques ` ```java ` ` |
| Separador | Línea `---` entre entrada y entrada |

Orden: el **1** es el más antiguo; el último número es el pedido más reciente.

---

## Historial

#### 1. Diagrama de clases → modelo base del Tower Defense

**Prompt:**

> Te adjunto una imagen con mi diagrama de clases del Tower Defense. Implementá solo el paquete del modelo (`com.miJuego.model` o similar): clases concretas y que implementen las interfaces de `com.game2d.model` (`GameModel`, `FrameSnapshot`, `Drawable`, `GameStatus`, etc.). No modifiques vista ni controller. Generá un `TowerDefenseModel` que compile y un `TowerDefenseMain` mínimo que lo conecte al `DefaultGameController`.

**Clases / métodos generados:**

```java
TowerDefenseModel     → implements GameModel; capture(), update(), dispatch()
TowerDefenseMain      → bind(model, view), start()
Torre, Enemigo, Nivel → dominio del juego
TorreDrawable         → implements Drawable
TowerDefenseSnapshot  → implements FrameSnapshot, getStatus(), getDrawables()
TowerDefenseStatus    → implements GameStatus (score, gold, lives)
```

```java
GameView view = GameViews.getInstance().getView();
GameModel model = new TowerDefenseModel(view);
DefaultGameController controller = new DefaultGameController();
controller.bind(model, view);
controller.start();
```

---

#### 2. Implementación completa del modelo de Tower Defense en com.miJuego.model

**Prompt:**

> Te adjunto una imagen con mi diagrama de clases del Tower Defense. Implementá solo el paquete del modelo (com.miJuego.model o similar): clases concretas y que implementen las interfaces de com.game2d.model (GameModel, FrameSnapshot, Drawable, GameStatus, etc.). No modifiques vista ni controller. Generá un TowerDefenseModel que compile y un TowerDefenseMain mínimo que lo conecte al DefaultGameController. Cualquier comentario que quieras decirme del uml sera bienvenido como consulta.

**Clases / métodos generados:**

```java
com.miJuego.model.EstadoJuego          → enum { START, PLAYING, GAME_OVER, VICTORY }
com.miJuego.model.Jugador              → score, health (vida), moneda (coins)
com.miJuego.model.DañoDeTorre          → interface con ataque(Enemigo): double
com.miJuego.model.Torre                → abstract class implementing Drawable (costoTorre, tiempoRecarga, towertype, upgrade())
com.miJuego.model.TorreComun           → extends Torre implements DañoDeTorre
com.miJuego.model.TorreDeArea          → extends Torre implements DañoDeTorre
com.miJuego.model.Cañon                → extends Torre implements DañoDeTorre
com.miJuego.model.TorreFuerte          → extends Torre implements DañoDeTorre
com.miJuego.model.TorreDeFuego         → extends Torre implements DañoDeTorre
com.miJuego.model.TorreDeHielo         → extends Torre implements DañoDeTorre
com.miJuego.model.TorreElectrica       → extends Torre implements DañoDeTorre
com.miJuego.model.Enemigo              → abstract class implementing Drawable (vida, monedasGeneradas, scoreGenerado, morir())
com.miJuego.model.Duende               → extends Enemigo
com.miJuego.model.EnemigoMultiple      → extends Enemigo (se divide al morir)
com.miJuego.model.EnemigoComun         → extends Enemigo (tieneEscudoElectrico)
com.miJuego.model.Bala                 → implements Drawable (proyectiles)
com.miJuego.model.Nivel                → maneja caminos (waypoints) y oleadas de enemigos
com.miJuego.model.Juego                → bucle principal, actualización de lógica de juego, puntuaciones
com.miJuego.model.Scoreboard           → persistencia del TOP 10 en archivo scoreboard.txt
com.miJuego.model.TowerDefenseModel    → implements GameModel (despacha clicks y teclado VK_1-VK_7, VK_U, VK_S, VK_N, VK_R)
com.miJuego.model.TowerDefenseMain     → inicializa el juego, asocia atajos y arranca DefaultGameController
```

---

#### 3. Herramientas de desarrollador — SandboxConsole y DevConsoleMain

**Prompt:**

> Quería hacer algo aparte, como un nivel/juego separado para testear ciertas cosas relacionadas con el Tower Defense. Uno manejable por consola y otro con la interfaz gráfica con una consola de desarrollador tipo Counter-Strike (~).

**Clases / métodos generados:**

```java
com.miJuego.sandbox.SandboxConsole       → main() con loop Scanner; comandos: spawn, place, upgrade,
                                            sell, give gold/lives, run N, level N, list, reset, status
com.miJuego.sandbox.DevCommandExecutor   → execute(String): String; thread-safe (synchronized sobre Juego)
                                            Comandos: give, god, spawn, killall, place, upgrade, sell,
                                            next, level, speed, pause, resume, restart, status, clear
com.miJuego.sandbox.DevConsoleFrame      → JFrame flotante estilo terminal oscuro; JTextPane con colores,
                                            JTextField con historial arriba/abajo, timestamps por línea
com.miJuego.sandbox.DevConsoleMain       → main() que arranca TowerDefenseModel + DevConsoleFrame;
                                            tecla ~ / F1 abre/cierra la consola
com.miJuego.sandbox.DevConsoleModelWrapper → implements GameModel; intercepta DEV_CONSOLE_TOGGLE
```

```java
// Métodos agregados a clases existentes:
Enemigo.setRapidez(double)      → setter público (antes campo protected)
Enemigo.setDañoBase(double)     → setter público (antes campo protected)
Enemigo.tieneFuego()            → helper boolean para sandbox
Enemigo.tieneRalentizar()       → helper boolean para sandbox
Enemigo.tieneParalizacion()     → helper boolean para sandbox
TowerDefenseModel.getJuego()    → expone el Juego para DevCommandExecutor
```

---

#### 4. Corrección de límites de colocación en Juego.java

**Prompt:**

> nono, no quiero que vayamos a hacer grandes cambios, primero quiero que terminemos de plantear bien el nivel que tenemos ¿Si?

**Clases / métodos generados:**

```java
Juego.placeTower(int, int) → Se actualizó el chequeo de límites de ix < 0 || ix >= 20 || iy < 0 || iy >= 15 a ix >= 32 || iy >= 24
```

---

#### 5. Grilla en perspectiva isométrica y hover dinámico

**Prompt:**

> Okay, vamos por cosas sencillas. No se si ves el icono celeste de la cuadrilla, ese cuadrado que es como un fantasma, queria saber si no podrias poner toda la "grilla" en perspectiva asi hay una estetica cuidada

**Clases / métodos generados:**

```java
TowerDefenseModel.HoverHighlightDrawable → Nueva clase interna que representa el highlight flotante debajo del cursor
TowerDefenseModel.capture()              → Lógica para añadir un HoverHighlightDrawable en verde (válido) o rojo (inválido) al arrastrar el mouse con torre seleccionada
GamePanel.paintDrawable()                 → Intercepta drawables con ID conteniendo "highlight" para renderizar un polígono de rombo isométrico con bordes perfilados en lugar de un fillRect común
```

---

#### 6. Alineación de torre y cursor fantasma con el centro de la celda

**Prompt:**

> Perfecto, ahora analiza esta imagen, queria ver si podrias hacer que el sprite fantasma tambien estuviera en perspectiva

**Clases / métodos generados:**

```java
GamePanel.paintDrawable() → Se añadió un desplazamiento de -0.5 celdas en las coordenadas de dibujado (drawX, drawY) para las torres colocadas y el cursor fantasma, logrando centrar la base del sprite de tamaño 2x2 en la celda de tamaño 1x1 del grid.
```

---

#### 7. Restricciones físicas de área de construcción (hoja del documento) en Nivel 1

**Prompt:**

> Añadir los limites donde se puede y no se puede plantar torres, si queres aplicamos un metodo similar al de los waypoints

**Clases / métodos generados:**

```java
Nivel.isValidPlacementArea(int, int)   → Nueva validación que comprueba si la celda cae dentro de buildPolygon (hoja de papel) para Nivel 1, o dentro de la grilla para los niveles 2-5
Juego.placeTower(int, int)              → Utiliza la nueva validación isValidPlacementArea y lanza IllegalStateException si está fuera
TowerDefenseModel.capture()             → Modifica el cursor hover para validar isValidPlacementArea y pintar en rojo e impedir la visualización fantasma si está fuera de los límites de la hoja
TowerDefenseLogicTest                  → Se actualizaron las coordenadas de colocación en los tests a (15, 12) para caer dentro del área de la hoja del Nivel 1
```

---

