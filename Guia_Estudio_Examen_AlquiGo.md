# 📱 Guía de Estudio Paso a Paso para el Examen — Jetpack Compose & Navigation

---

## 🧭 Flujo Maestro Paso a Paso

### 📄 PASO 1: Crear la Nueva Pantalla (`MiNuevaPantalla.kt`)

Crea un archivo Kotlin en la carpeta `ui/` de tu proyecto.

```kotlin
package com.example.alquigo.ui.ejemplo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MiNuevaPantalla(
    onVolver: () -> Unit // Callback para regresar atrás
) {
    // 1. Estado para guardar el texto ingresado en el Input
    var textoIngresado by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para volver atrás
        Button(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Esta es la Nueva Pantalla",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. DIBUJAR EL INPUT (Caja de Texto)
        OutlinedTextField(
            value = textoIngresado,
            onValueChange = { textoIngresado = it },
            label = { Text("Escribe algo aquí...") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Mostrar en tiempo real lo escrito
        if (textoIngresado.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ingresado: $textoIngresado",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

### 🧭 PASO 2: Registrar la Ruta en `NavGraph.kt`

Abre `app/src/main/java/com/example/alquigo/navigation/NavGraph.kt`:

#### A. Definir la ruta en `sealed class Screen`:
```kotlin
sealed class Screen(val route: String) {
    // ... rutas existentes ...
    object MiNuevaPantalla : Screen("mi_nueva_pantalla") // 👈 Agregar esta línea
}
```

#### B. Registrar la pantalla dentro de `NavHost`:
```kotlin
composable(Screen.MiNuevaPantalla.route) {
    MiNuevaPantalla(
        onVolver = { navController.popBackStack() } // 👈 Vuelve a la pantalla anterior
    )
}
```

---

### 🔘 PASO 3: Crear el Botón en la Pantalla Origen

En cualquier pantalla (por ejemplo `HomeScreen.kt` o `ProfileScreen.kt`):

```kotlin
Button(
    onClick = { 
        navController.navigate(Screen.MiNuevaPantalla.route) // 👈 Abre la nueva pantalla
    },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Abrir Formulario con Input")
}
```

---

### ✏️ PASO 4: Mostrar Input en la MISMA Pantalla al hacer clic

Si te piden mostrar una caja de texto sin cambiar de pantalla:

```kotlin
// 1. Estados
var mostrarInput by remember { mutableStateOf(false) }
var textoIngresado by remember { mutableStateOf("") }

// 2. Botón que activa la visibilidad
Button(
    onClick = { mostrarInput = true }
) {
    Text("Mostrar Caja de Texto")
}

// 3. Dibuja el Input si mostrarInput es true
if (mostrarInput) {
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = textoIngresado,
        onValueChange = { textoIngresado = it },
        label = { Text("Escribe un dato aquí") },
        modifier = Modifier.fillMaxWidth()
    )
}
```

---

### 💡 Comandos Mnemotécnicos Clave
1. **Crear Input**: `OutlinedTextField(value = texto, onValueChange = { texto = it })`
2. **Navegar a otra Pantalla**: `navController.navigate("ruta")`
3. **Volver a Pantalla Anterior**: `navController.popBackStack()`
