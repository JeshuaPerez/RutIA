// Registro (E-01) y elección de un estudiante existente (E-02).
import { listarEstudiantes, registrarEstudiante } from "./api.js";
import { crear } from "./dom.js";
import { activarEstudiante, alCambiarEstudiante, estudianteActivo } from "./estudiante.js";
import { enfocarPrimerError, limpiarAlerta, limpiarErrores, mostrarAlerta, mostrarErroresDeCampo } from "./formulario.js";
import { alMostrarPestana } from "./pestanas.js";

const formRegistro = document.getElementById("form-registro");
const alertaRegistro = document.getElementById("registro-alerta");
const exitoRegistro = document.getElementById("registro-exito");
const formElegir = document.getElementById("form-elegir");
const selector = document.getElementById("elegir-estudiante");
const alertaElegir = document.getElementById("elegir-alerta");
const exitoElegir = document.getElementById("elegir-exito");

let estudiantes = [];
let solicitudLista = 0;

const botonIrAConsultar = () =>
  crear("button", { clase: "boton boton-secundario", texto: "Ir a Consultar", atributos: { type: "button", "data-ir-a": "consultar" } });

function limpiarMensajes() {
  limpiarErrores(formRegistro);
  limpiarErrores(formElegir);
  limpiarAlerta(alertaRegistro);
  limpiarAlerta(alertaElegir);
  exitoRegistro.replaceChildren();
  exitoElegir.replaceChildren();
}

async function registrar(evento) {
  evento.preventDefault();
  const boton = formRegistro.querySelector('[type="submit"]');
  if (boton.disabled) {
    return;
  }
  limpiarMensajes();
  const campos = formRegistro.elements;
  // Sin validación propia: la API decide y sus errores se muestran por campo.
  const datos = {
    nombreCompleto: campos.nombreCompleto.value.trim(),
    correo: campos.correo.value.trim(),
    nivelExperiencia: campos.nivelExperiencia.value || null,
    areaInteres: campos.areaInteres.value.trim(),
  };

  boton.disabled = true;
  try {
    const estudiante = await registrarEstudiante(datos);
    formRegistro.reset();
    activarEstudiante(estudiante);
    exitoRegistro.replaceChildren(
      crear("p", {}, [
        crear("strong", { texto: `Te registraste, ${estudiante.nombreCompleto}.` }),
        ` Tu id es ${estudiante.id} y ya eres el estudiante activo.`,
      ]),
      botonIrAConsultar(),
    );
    cargarEstudiantes();
  } catch (error) {
    const sinCampo = mostrarErroresDeCampo(formRegistro, error.detalles);
    mostrarAlerta(alertaRegistro, error.message, sinCampo);
    enfocarPrimerError(formRegistro);
  } finally {
    boton.disabled = false;
  }
}

function elegir(evento) {
  evento.preventDefault();
  limpiarMensajes();
  const estudiante = estudiantes.find((candidato) => String(candidato.id) === selector.value);
  if (!estudiante) {
    mostrarErroresDeCampo(formElegir, [{ campo: "estudiante", mensaje: "elige un estudiante de la lista" }]);
    enfocarPrimerError(formElegir);
    return;
  }
  activarEstudiante(estudiante);
  exitoElegir.replaceChildren(crear("p", { texto: `Ahora consultas como ${estudiante.nombreCompleto}.` }), botonIrAConsultar());
}

function pintarOpciones() {
  const indicacion = estudiantes.length ? "Elige un estudiante…" : "No hay estudiantes registrados";
  selector.replaceChildren(
    crear("option", { texto: indicacion, atributos: { value: "" } }),
    ...estudiantes.map((estudiante) =>
      crear("option", {
        texto: `${estudiante.nombreCompleto} · ${estudiante.nivelExperiencia} · ${estudiante.areaInteres}`,
        atributos: { value: estudiante.id },
      }),
    ),
  );
  sincronizarSelector(estudianteActivo());
}

function sincronizarSelector(estudiante) {
  const id = estudiante ? String(estudiante.id) : "";
  selector.value = estudiantes.some((candidato) => String(candidato.id) === id) ? id : "";
}

async function cargarEstudiantes() {
  const numero = ++solicitudLista;
  try {
    const lista = await listarEstudiantes();
    if (numero !== solicitudLista) {
      return;
    }
    estudiantes = lista;
    limpiarAlerta(alertaElegir);
    pintarOpciones();
  } catch (error) {
    if (numero === solicitudLista) {
      estudiantes = [];
      selector.replaceChildren(crear("option", { texto: "No se pudo cargar la lista", atributos: { value: "" } }));
      mostrarAlerta(alertaElegir, error.message);
    }
  }
}

export function iniciarRegistro() {
  formRegistro.addEventListener("submit", registrar);
  formElegir.addEventListener("submit", elegir);
  alMostrarPestana("registro", cargarEstudiantes);
  alCambiarEstudiante(sincronizarSelector);
}
