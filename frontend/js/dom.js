// Creación de nodos. Los datos de la API se insertan siempre como texto (textContent o nodos de texto), nunca como HTML.

const SVG = "http://www.w3.org/2000/svg";
let secuencia = 0;

/** Id único para enlazar etiquetas, errores y regiones creadas en tiempo de ejecución. */
export const nuevoId = (prefijo) => `${prefijo}-${++secuencia}`;

/**
 * Crea un elemento. `texto` va a textContent; los hijos de tipo string se agregan como nodos de texto.
 * Los atributos con valor null, undefined o false se omiten; true crea el atributo vacío.
 */
export function crear(etiqueta, { clase, texto, atributos } = {}, hijos = []) {
  const elemento = document.createElement(etiqueta);
  if (clase) {
    elemento.className = clase;
  }
  if (texto !== undefined && texto !== null) {
    elemento.textContent = String(texto);
  }
  for (const [nombre, valor] of Object.entries(atributos ?? {})) {
    if (valor !== null && valor !== undefined && valor !== false) {
      elemento.setAttribute(nombre, valor === true ? "" : String(valor));
    }
  }
  for (const hijo of [].concat(hijos)) {
    if (hijo !== null && hijo !== undefined && hijo !== false) {
      elemento.append(typeof hijo === "string" || typeof hijo === "number" ? String(hijo) : hijo);
    }
  }
  return elemento;
}

/** Ícono del sprite de index.html (decorativo). */
export function crearIcono(nombre) {
  const svg = document.createElementNS(SVG, "svg");
  svg.setAttribute("class", "icono");
  svg.setAttribute("aria-hidden", "true");
  svg.setAttribute("focusable", "false");
  const uso = document.createElementNS(SVG, "use");
  uso.setAttribute("href", `#icono-${nombre}`);
  svg.append(uso);
  return svg;
}
