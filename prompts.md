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

