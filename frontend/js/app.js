// Punto de entrada: pestañas, chip del estudiante activo y restauración del estudiante guardado.
import { obtenerEstudiante } from "./api.js";
import { iniciarCatalogo } from "./catalogo.js";
import { iniciarConsulta } from "./consulta.js";
import { crear, crearIcono } from "./dom.js";
import {
  activarEstudiante,
  alCambiarEstudiante,
  estudianteActivo,
  leerIdGuardado,
  nombreVisible,
  olvidarEstudiante,
} from "./estudiante.js";
import { iniciarHistorial } from "./historial.js";
import { iniciarPestanas, mostrarPestana } from "./pestanas.js";
import { iniciarRegistro } from "./registro.js";

const chip = document.getElementById("chip-estudiante");
const avisoGlobal = document.getElementById("aviso-global");
const avisoGlobalTexto = document.getElementById("aviso-global-texto");

function mostrarAviso(texto) {
  avisoGlobalTexto.textContent = texto;
  avisoGlobal.classList.add("aviso-global--visible");
}

function cerrarAviso() {
  avisoGlobalTexto.textContent = "";
  avisoGlobal.classList.remove("aviso-global--visible");
}

function pintarChip(estudiante, cambioDeId) {
  chip.classList.toggle("chip-estudiante--vacio", !estudiante);
  chip.replaceChildren(
    crear("span", { clase: "chip-avatar" }, [crearIcono("estudiante")]),
    crear("span", { clase: "chip-textos" }, [
      crear("span", { clase: "chip-etiqueta", texto: estudiante ? `Estudiante activo · id ${estudiante.id}` : "Sin estudiante activo" }),
      crear("span", { clase: "chip-nombre", texto: estudiante ? nombreVisible(estudiante) : "Regístrate o elige tu perfil" }),
    ]),
  );
  chip.title = estudiante ? "Cambiar de estudiante en Registro" : "Ir a Registro";
  if (cambioDeId && estudiante) {
    cerrarAviso();
  }
}

/** Valida el id guardado con E-03: se olvida si da 404 y se conserva si no hay conexión. */
async function restaurarEstudiante(id) {
  activarEstudiante({ id });
  try {
    const estudiante = await obtenerEstudiante(id);
    if (estudianteActivo()?.id === id) {
      activarEstudiante(estudiante);
    }
  } catch (error) {
    if (error.status === 404) {
      if (estudianteActivo()?.id === id) {
        olvidarEstudiante();
      }
      mostrarAviso(`El estudiante guardado (id ${id}) ya no existe. Regístrate o elige un estudiante existente.`);
    } else {
      mostrarAviso(`${error.message}. No se pudo verificar el estudiante guardado (id ${id}).`);
    }
  }
}

// Botones que llevan a otra pestaña (chip, «Ir a Registro», «Ir a Consultar»).
document.addEventListener("click", (evento) => {
  const enlace = evento.target.closest("[data-ir-a]");
  if (enlace) {
    mostrarPestana(enlace.dataset.irA, { enfocarTitulo: true });
  }
});
document.getElementById("aviso-global-cerrar").addEventListener("click", cerrarAviso);

iniciarPestanas();
iniciarRegistro();
iniciarCatalogo();
iniciarConsulta();
iniciarHistorial();
alCambiarEstudiante(pintarChip);
pintarChip(null, false);

const idGuardado = leerIdGuardado();
mostrarPestana(idGuardado ? "consultar" : "registro");
if (idGuardado) {
  restaurarEstudiante(idGuardado);
}
