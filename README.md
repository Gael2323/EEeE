# Tower Defense Game

## Objetivo

Desarrollar un juego Tower Defence en Java aplicando POO y el patrón MVC provisto en `com.game2d`.

## Enunciado

Colocar torres al costado de un camino por el que viajan enemigos. Si un enemigo llega al final, se descuenta vida; si la vida llega a cero, el jugador pierde. Al eliminar enemigos se acredita oro para comprar y mejorar torres.

### Tipos de torres

* **Torre Simple** — un enemigo, daño básico.
* **Torre de Área** — N enemigos cercanos.
* **Cañón** — daño en un área.
* **Torre de Frío** — ralentiza, no daña.
* **Torre Eléctrica** — poco daño común, mucho a enemigos eléctricos.

### Tipos de enemigos

* **Enemigo Simple** — N vida; a 0 muere.
* **Enemigo Múltiple** — al morir genera uno más débil.
* **Enemigo con Escudo Eléctrico** — poco daño normal, mucho eléctrico.

### Requisitos mínimos

* Al menos **5 niveles** con complejidad creciente.
* Colocar y **mejorar** torres con oro.
* **TOP 10** jugadores (puntaje y fecha).

## Consideraciones

## Demo de referencia

[Video Tower Defence](https://www.youtube.com/watch?v=XhUqV456MFI)

---

## Cómo ejecutar

**Requisitos:** Java 21+, Gradle (incluido `gradlew`).

```bash
./gradlew run      # juego (hoy: Snake de ejemplo, ver build.gradle)
./gradlew test     # tests
./gradlew build    # compilar
```

**IDE:** proyecto Gradle, JDK 21. Clase principal en `build.gradle` → `application.mainClass`.

Por defecto corre el **Snake de demostración** (`build.gradle` → `mainClass`). Otra entrada: cambiar `mainClass` y ver [Snake Example](#snake-example).

---

## MVC

| Capa | Paquete |
|------|---------|
| **Modelo** | `com.game2d.model` |
| **Vista** | `com.game2d.view` |
| **Controlador** | `com.game2d.controller` |

### Flujo

1. `controller` llama `model.update(dt)` si `SessionState == RUNNING`.
2. `controller` obtiene `model.capture()` y llama `view.render(frame)`.
3. Entradas (clic, tecla) → `model.dispatch(GameInput)`.
4. El modelo devuelve en `capture()` qué dibujar, barra de estado y errores.

### Inicialización

```java
GameView view = GameViews.getInstance().getView();
GameViewMessages.getInstance().bind(view);
GameModel model = new TowerDefenseModel(view);

DefaultGameController controller = new DefaultGameController();
controller.getKeyCommands()
        .bind(KeyEvent.VK_P, GameCommands.PAUSE);

controller.bind(model, view);
controller.start();
```

`mainClass` en `build.gradle` apunta a la clase con este `main`.

### Modelo — interfaces

| Interfaz | Métodos clave |
|----------|----------------|
| `GameModel` | `capture()`, `update(float)`, `dispatch(GameInput)` |
| `FrameSnapshot` | `getState()`, `getWorldWidth/Height()`, `getDrawables()`, `getMenu()`, `getStatus()`, `getErrorMessage()` |
| `GameStatus` | `getScore()`, `getGold()`, `getLives()` — `-1` muestra `—` en la barra |
| `Drawable` | posición/tamaño `Float`, imagen opcional (`Renderable`) |
| `Menu` / `Action` | botones (implementados; UI del menú desactivada por defecto) |
| `GameInput` | entradas; el controller usa `SimpleGameInput` |

**Barra superior (navbar):** `GameStatus` en cada `capture()` (puntaje, oro, vidas).

```java
@Override
public GameStatus getStatus() {
    return new GameStatus() {
        public int getScore() { return puntaje; }
        public int getGold() { return oro; }
        public int getLives() { return vidas; }
    };
}
```

**Estados:** `SessionState` — `READY`, `RUNNING`, `PAUSED`, `FINISHED`.

**Imágenes:** `getImagePath()` (archivo) o `getImageUrl()`. Si fallan, color/forma con `getFallbackColor()` / `getFallbackShape()`.

### Mensajes y errores en pantalla

| Método | Uso |
|--------|-----|
| `GameViewMessages.getInstance().success("...")` | Texto verde, 3 s |
| `GameViewMessages.getInstance().error(ex)` | Texto rojo, 3 s |
| `view.errorMessage("...")` | Igual, con referencia a `GameView` |
| `FrameSnapshot.getErrorMessage()` | El controller lo muestra en el frame |

Excepción no capturada en `update` / `dispatch` / `capture` → el controller llama `showError` automáticamente.

```java
try {
    colocarTorre(x, y);
} catch (IllegalStateException ex) {
    GameViewMessages.getInstance().error(ex);
}
```

### Teclas → acciones

Mismo `String` id en teclas y en `Action` del menú:

```java
controller.getKeyCommands()
        .bind(KeyEvent.VK_UP, GameCommands.MOVE_UP)
        .bind("P", GameCommands.PAUSE);
```

| Origen | Llega al modelo como |
|--------|----------------------|
| Tecla mapeada | `InputKind.ACTION` + `actionId` |
| Tecla sin mapear | `InputKind.KEY_PRESSED` + nombre |
| Clic | `POINTER_DOWN` / `POINTER_UP` + coordenadas mundo |

```java
@Override
public void dispatch(GameInput input) {
    if (input.getKind() == InputKind.ACTION) {
        switch (input.getActionId().orElse("")) {
            case GameCommands.PAUSE -> pausar();
            // ...
        }
    } else if (input.getKind() == InputKind.POINTER_DOWN) {
        float x = input.getX().orElse(0f);
        float y = input.getY().orElse(0f);
        colocarTorre(x, y);
    }
}
```

Constantes sugeridas: `GameCommands` (`START`, `PAUSE`, `MOVE_UP`, …).

### Vista — clases

| Clase | Rol |
|-------|-----|
| `SwingGameView` / `GameViews` | Ventana y render |
| `NavBarPanel` | Puntaje, Oro, Vidas |
| `GamePanel` | Fondo + `Drawable`s |
| `MessageToastOverlay` | Mensajes toast |
| `BackgroundSettings` | Fondo (singleton, editable) |
| `ImageResolvers` | Carga de imágenes (singleton) |
| `GameViewMessages` | Mensajes (singleton) |
| `ViewSettings.MENU_UI_ENABLED` | `false` — menú oculto para no frenar el juego |

### Controlador — clases

| Clase | Rol |
|-------|-----|
| `DefaultGameController` | Loop ~60 FPS, entradas, errores |
| `KeyCommandRegistry` | `getKeyCommands().bind(...)` |
| `GameCommands` | ids de acciones |

### Tests

`./gradlew test` — contratos del modelo, imágenes, controller, Snake de ejemplo.

---

## Prompts

Archivo de registro: **[prompts.md](prompts.md)**.

Contiene el historial de pedidos al asistente de IA y las clases o archivos que se generaron en cada uno. Si se usa Cursor u otra IA en este repo, debe **detectar `prompts.md`**, seguir el formato allí definido y **agregar automáticamente** una entrada numerada al finalizar cada pedido (siguiente `#### N.`, separador `---`).

---

## Snake Example

Juego mínimo en `com.game2d.example.snake` para probar el MVC.

### Ejecutar

```bash
./gradlew run
```

`build.gradle`:

```gradle
application {
    mainClass = 'com.game2d.example.snake.SnakeMain'
}
```

También: `com.App` → delega a `SnakeMain`.

### Archivos

| Archivo | Rol |
|---------|------|
| `SnakeMain` | `main`, vista, modelo, controller, teclas |
| `SnakeGameModel` | Implementa `GameModel` |

### Wiring (`SnakeMain`)

```java
GameView view = GameViews.getInstance().getView();
GameViewMessages.getInstance().bind(view);
GameModel model = new SnakeGameModel(view);

DefaultGameController controller = new DefaultGameController();
controller.getKeyCommands()
        .bind(KeyEvent.VK_UP, GameCommands.MOVE_UP)
        .bind(KeyEvent.VK_DOWN, GameCommands.MOVE_DOWN)
        .bind(KeyEvent.VK_LEFT, GameCommands.MOVE_LEFT)
        .bind(KeyEvent.VK_RIGHT, GameCommands.MOVE_RIGHT)
        .bind(KeyEvent.VK_W, GameCommands.MOVE_UP)
        .bind(KeyEvent.VK_S, GameCommands.MOVE_DOWN)
        .bind(KeyEvent.VK_A, GameCommands.MOVE_LEFT)
        .bind(KeyEvent.VK_D, GameCommands.MOVE_RIGHT)
        .bind(KeyEvent.VK_P, GameCommands.PAUSE)
        .bind(KeyEvent.VK_SPACE, GameCommands.PAUSE)
        .bind(KeyEvent.VK_R, GameCommands.RESTART);

controller.bind(model, view);
controller.start();
```

### Controles

| Acción | Tecla |
|--------|-------|
| Mover | Flechas / `W` `A` `S` `D` |
| Pausar / continuar | `Espacio` / `P` |
| Reiniciar (pausado) | `R` |

### Comportamiento del ejemplo

* Arranca en `RUNNING` sin pantalla inicial.
* No hay game over: wrap en bordes, no muere al chocar consigo misma.
* Navbar: **Puntaje** actual; **Oro** y **Vidas** en `—` (`getGold()` / `getLives()` = `-1`).
* Menú implementado en el modelo; **vista sin menú** (`ViewSettings.MENU_UI_ENABLED = false`).
* Mensajes: verde al comer; rojo vía `error` / `showError`.

### `GameStatus` en Snake

```java
private GameStatus currentStatus() {
    return new SimpleGameStatus(score, -1, -1);  // oro y vidas no usados
}
```

### `dispatch` (dirección)

```java
case GameCommands.MOVE_UP -> queueDirection(0, -1);
case GameCommands.PAUSE -> togglePause();
case GameCommands.RESTART -> restartGame();
```

