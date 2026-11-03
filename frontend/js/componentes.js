// Piezas de interfaz compartidas por catálogo, resultado e historial.
import { crear, nuevoId } from "./dom.js";

const CLASES_ESTADO = {
  "Pendiente": "pendiente",
  "Respondida": "respondida",
  "Sin resultados": "sin-resultados",
  "Error": "error",
};

export const claseEstado = (estado) => CLASES_ESTADO[estado] ?? "pendiente";

/** Estado de la consulta con texto y color (C5). */
export function crearEstado(estado) {
  return crear("span", { clase: `estado estado--${claseEstado(estado)}` }, [
    crear("span", { clase: "sr-only", texto: "Estado: " }),
    estado,
  ]);
}

const formatoFecha = new Intl.DateTimeFormat("es-GT", { dateStyle: "long", timeStyle: "short" });

/** Las fechas de la API son ISO-8601 sin zona en hora local; Date las interpreta como hora local. */
export function formatearFecha(iso) {
  const fecha = new Date(iso);
  return Number.isNaN(fecha.getTime()) ? String(iso ?? "") : formatoFecha.format(fecha);
}

export const crearFecha = (iso) => crear("time", { texto: formatearFecha(iso), atributos: { datetime: iso } });

export const formatearSimilitud = (similitud) => Number(similitud).toFixed(3);

const formatearDuracion = (horas) => `${horas} ${Number(horas) === 1 ? "hora" : "horas"}`;

function crearChip(modificador, prefijo, valor) {
  return crear("li", { clase: `chip ${modificador}` }, [crear("span", { clase: "sr-only", texto: prefijo }), valor]);
}

/** Categoría, nivel y duración de un curso o de una fuente. */
export function crearChipsCurso({ categoria, nivel, duracionHoras }) {
  return crear("ul", { clase: "chips" }, [
    crearChip("chip--categoria", "Categoría: ", categoria),
    crearChip("chip--nivel", "Nivel: ", nivel),
    crearChip("chip--duracion", "Duración: ", formatearDuracion(duracionHoras)),
  ]);
}

const LARGO_PLEGABLE = 220;

/** Descripción que se recorta a cuatro líneas si es larga, con un botón para leerla completa. */
export function crearDescripcion(texto) {
  const id = nuevoId("descripcion");
  const parrafo = crear("p", { clase: "descripcion", texto, atributos: { id } });
  if (!texto || texto.length <= LARGO_PLEGABLE) {
    return parrafo;
  }
  parrafo.classList.add("descripcion--plegada");
  const boton = crear("button", {
    clase: "boton-enlace boton-plegar",
    texto: "Leer descripción completa",
    atributos: { type: "button", "aria-expanded": "false", "aria-controls": id },
  });
  boton.addEventListener("click", () => {
    const abierta = boton.getAttribute("aria-expanded") === "true";
    boton.setAttribute("aria-expanded", String(!abierta));
    boton.textContent = abierta ? "Leer descripción completa" : "Mostrar menos";
    parrafo.classList.toggle("descripcion--plegada", abierta);
  });
  return crear("div", { clase: "descripcion-plegable" }, [parrafo, boton]);
}
