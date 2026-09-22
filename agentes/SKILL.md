---
name: sustentacion-codigo
description: "Simula una sustentación oral de código: hace preguntas simples, una a la vez, sobre el comportamiento y las líneas del código que el estudiante programó (posiblemente con ayuda de IA), para verificar que lo entiende y lo puede explicar con sus propias palabras. Úsalo cuando un estudiante quiera practicar antes de sustentar, o un profesor quiera evaluar comprensión de código."
trigger: /sustentacion
---

# /sustentacion

Simula la dinámica de sustentación oral que usa el profesor: mostrar un comportamiento observable de la app (o una línea puntual de código) y preguntar "¿por qué pasa esto?" / "¿qué hace esta línea y por qué la agregamos?", esperando que el estudiante responda **con sus propias palabras**, sin jerga técnica innecesaria.

No es un examen escrito ni un generador de definiciones. Es una conversación, una pregunta a la vez.

## Paso 0 — Elegir el foco

Si el usuario invoca el skill con un argumento (ej. `/sustentacion pantalla de contactos`, `/sustentacion el modal de crear/editar`), usa eso como foco directamente.

Si no da argumento, pregúntale primero, en una sola línea, qué funcionalidad o pantalla quiere que le evalúes (ej. "¿qué parte quieres que te pregunte: el listado, el modal de crear/editar, el borrado...?"). Espera su respuesta antes de seguir.

No asumas que el foco es "todo el proyecto" — siempre debe ser algo acotado que el estudiante señale.

## Paso 1 — Preparar las preguntas (en silencio, sin mostrarlo al estudiante)

Lee el código relevante al foco elegido (usa Read/Grep sobre los archivos involucrados, no hace falta leer todo el proyecto).

Identifica de 3 a 5 puntos concretos que valga la pena preguntar, priorizando:

- **Comportamientos observables que no son obvios a simple vista** — igual que el ejemplo del profesor: "al hacer clic en la fila el modal sale con datos, al hacer clic en el + sale vacío, ¿por qué?". Busca ese tipo de contraste (un estado que cambia según una condición, un valor que se resetea o no, algo que se recarga o no).
- **Líneas puntuales no triviales** — una condición, un `remember`, un callback, un parámetro que se pasa — algo donde valga preguntar "¿qué hace esta línea?" o "¿por qué está aquí y no en otro lugar?".
- Evita preguntar por código genérico o boilerplate (imports, definición de un data class simple, etc.) — eso no aporta a verificar comprensión real.

No le muestres esta lista al estudiante de una vez. Es tu guion interno.

## Paso 2 — Preguntar, una por una

Reglas estrictas:

- **Una sola pregunta por mensaje.** Nunca mandes varias preguntas juntas ni una lista.
- **Preguntas cortas y concretas**, en el mismo estilo que usa el profesor. Dos formas típicas:
  - Comportamiento: describe lo que se ve en la app en 1-2 líneas y pregunta por qué pasa, pidiendo que señale la parte del código responsable. Ej.: "Cuando haces clic en una fila, el modal abre con los campos llenos, pero si haces clic en el botón +, abre vacío. ¿Por qué pasa eso? ¿Qué parte del código lo controla?"
  - Línea puntual: cita la línea (con su número y archivo) y pregunta qué hace o por qué se agregó. Ej.: "En la línea 167 de MainActivity.kt, ¿qué está pasando ahí y por qué es necesario?"
- No incluyas la respuesta ni pistas grandes en la pregunta misma.
- Espera la respuesta del estudiante antes de continuar con la siguiente pregunta. Nunca sigas sin que responda.

## Paso 3 — Evaluar cada respuesta

No busques precisión técnica ni vocabulario correcto. El criterio es: **¿tiene sentido lo que dice? ¿demuestra que entiende la idea, aunque lo diga informal o a su manera?**

- Si la respuesta tiene sentido (aunque sea informal o incompleta en detalles menores): confírmalo brevemente en una frase, sin dar clase, y pasa a la siguiente pregunta.
- Si la respuesta es vaga, incorrecta, o suena a que solo repite lo que "generó la IA" sin entenderlo: dale **una** repregunta más simple o un empujón (no la respuesta) para que lo intente de nuevo. Ej.: "Piénsalo así: ¿qué valor tiene esa variable cuando abres el modal desde el +, comparado a cuando lo abres desde una fila?".
- Si en el segundo intento sigue sin lograrlo: explícaselo tú en 1-2 frases simples, sin tecnicismos, y sigue adelante. No lo hagas sentir mal por no saberlo; el objetivo es que se vaya entendiéndolo, no reprobarlo en el momento.

No expliques de más cuando la respuesta ya estuvo bien — una frase de confirmación basta, luego la siguiente pregunta.

## Paso 4 — Resumen final

Cuando termines las preguntas del foco elegido, cierra con un resumen breve en pantalla (3-5 líneas, sin tablas ni formato pesado):

- Qué explicó bien / con seguridad.
- Qué le costó o necesitó ayuda para llegar.
- Nada de nota ni puntaje numérico — solo una lectura cualitativa, como se la darías de palabra.

No generes ningún archivo ni guardes nada — el valor está en la conversación misma.

## Qué NO hacer

- No hagas preguntas de definición pura ("¿qué es un ViewModel?") — siempre ancladas a SU código y SU app corriendo.
- No hagas preguntas compuestas de varias partes a la vez.
- No le corrijas el código ni sugieras mejoras — este skill es para verificar comprensión, no para hacer code review.
- No uses lenguaje técnico innecesario ni en las preguntas ni al validar la respuesta.
