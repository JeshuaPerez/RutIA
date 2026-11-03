// Historial (E-04) y detalle de cada consulta (E-12).
import { obtenerConsulta, obtenerHistorial } from "./api.js";
import { claseEstado, crearEstado, crearFecha } from "./componentes.js";
import { crear, nuevoId } from "./dom.js";
import { alCambiarEstudiante, estudianteActivo } from "./estudiante.js";
import { limpiarAlerta, mostrarAlerta } from "./formulario.js";
import { alMostrarPestana, pestanaActual } from "./pestanas.js";
import { crearResultado, enfocarResultado } from "./resultado.js";

const sinEstudiante = document.getElementById("historial-sin-estudiante");
const contenido = document.getElementById("historial-contenido");
const vistaLista = document.getElementById("historial-vista-lista");
const estado = document.getElementById("historial-estado");
const vacio = document.getElementById("historial-vacio");
const alerta = document.getElementById("historial-alerta");
const lista = document.getElementById("historial-lista");
const vistaDetalle = document.getElementById("historial-vista-detalle");
const volver = document.getElementById("historial-volver");
const detalleEstado = document.getElementById("historial-detalle-estado");
const detalleAlerta = document.getElementById("historial-detalle-alerta");
const detalle = document.getElementById("historial-detalle");

let solicitudLista = 0;
let solicitudDetalle = 0;
let botonOrigen = null;

function crearCursos(cursos) {
  if (!cursos?.length) {
    return crear("p", { clase: "sin-cursos", texto: "Sin cursos recomendados." });
  }
  return crear("ul", { clase: "chips" }, cursos.map((curso) => crear("li", { clase: "chip chip--curso", texto: curso.nombre })));
}

function crearItem(item) {
  const idPregunta = nuevoId("historial-pregunta");
  const boton = crear("button", {
    clase: "boton boton-secundario",
    texto: "Ver detalle",
    atributos: { type: "button", "aria-describedby": idPregunta },
  });
  boton.addEventListener("click", () => abrirDetalle(item.consultaId, boton));
  return crear("li", { clase: `bitacora-item bitacora-item--${claseEstado(item.estado)}` }, [
    crear("article", { clase: "tarjeta tarjeta-historial", atributos: { "aria-labelledby": idPregunta } }, [
      crear("div", { clase: "tarjeta-historial-cabecera" }, [crearFecha(item.fecha), crearEstado(item.estado)]),
      crear("h3", { texto: item.pregunta, atributos: { id: idPregunta } }),
      crear("p", { clase: "resumen", texto: item.resumen }),
      crear("div", { clase: "cursos-recomendados" }, [
        crear("p", { clase: "etiqueta", texto: "Cursos recomendados" }),
        crearCursos(item.cursosRecomendados),
      ]),
      crear("div", { clase: "acciones" }, [boton]),
    ]),
  ]);
}

async function cargarHistorial() {
  const estudiante = estudianteActivo();
  if (!estudiante) {
    return;
  }
  const numero = ++solicitudLista;
  limpiarAlerta(alerta);
  vacio.hidden = true;
  lista.setAttribute("aria-busy", "true");
  estado.textContent = "Cargando historial…";
  try {
    const items = await obtenerHistorial(estudiante.id);
    if (numero !== solicitudLista) {
      return;
    }
    lista.replaceChildren(...items.map(crearItem));
    vacio.hidden = items.length > 0;
    estado.textContent = items.length
      ? `${items.length === 1 ? "1 consulta" : `${items.length} consultas`}, de la más reciente a la más antigua.`
      : "";
  } catch (error) {
    if (numero !== solicitudLista) {
      return;
    }
    lista.replaceChildren();
    estado.textContent = "";
    mostrarAlerta(alerta, error.message);
  } finally {
    if (numero === solicitudLista) {
      lista.setAttribute("aria-busy", "false");
    }
  }
}

async function abrirDetalle(consultaId, origen) {
  const numero = ++solicitudDetalle;
  botonOrigen = origen;
  vistaLista.hidden = true;
  vistaDetalle.hidden = false;
  detalle.replaceChildren();
  limpiarAlerta(detalleAlerta);
  detalleEstado.textContent = "Cargando el detalle…";
  window.scrollTo(0, 0);
  volver.focus();
  try {
    const datos = await obtenerConsulta(consultaId);
    if (numero !== solicitudDetalle) {
      return;
    }
    detalleEstado.textContent = "";
    const vista = crearResultado(datos, { titulo: "Detalle de la consulta" });
    detalle.replaceChildren(vista);
    enfocarResultado(vista);
  } catch (error) {
    if (numero === solicitudDetalle) {
      detalleEstado.textContent = "";
      mostrarAlerta(detalleAlerta, error.message);
    }
  }
}

function cerrarDetalle({ enfocarOrigen }) {
  solicitudDetalle++;
  vistaDetalle.hidden = true;
  vistaLista.hidden = false;
  detalle.replaceChildren();
  detalleEstado.textContent = "";
  limpiarAlerta(detalleAlerta);
  if (enfocarOrigen && botonOrigen?.isConnected) {
    botonOrigen.focus();
  }
  botonOrigen = null;
}

function pintarEstudiante(estudiante, cambioDeId) {
  sinEstudiante.hidden = Boolean(estudiante);
  contenido.hidden = !estudiante;
  if (!cambioDeId) {
    return;
  }
  cerrarDetalle({ enfocarOrigen: false });
  lista.replaceChildren();
  estado.textContent = "";
  vacio.hidden = true;
  limpiarAlerta(alerta);
  if (estudiante && pestanaActual() === "historial") {
    cargarHistorial();
  }
}

export function iniciarHistorial() {
  volver.addEventListener("click", () => cerrarDetalle({ enfocarOrigen: true }));
  alMostrarPestana("historial", () => {
    cerrarDetalle({ enfocarOrigen: false });
    cargarHistorial();
  });
  alCambiarEstudiante(pintarEstudiante);
  pintarEstudiante(estudianteActivo(), false);
}
