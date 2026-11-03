// Consultar (E-11): formulario de la necesidad, espera con «Trazando tu ruta…» y resultado.
import { registrarConsulta } from "./api.js";
import { alCambiarEstudiante, estudianteActivo, nombreVisible } from "./estudiante.js";
import { enfocarPrimerError, limpiarAlerta, limpiarErrores, mostrarAlerta, mostrarErroresDeCampo } from "./formulario.js";
import { crearResultado, enfocarResultado } from "./resultado.js";

const MAXIMO = 500;

const sinEstudiante = document.getElementById("consultar-sin-estudiante");
const contenido = document.getElementById("consultar-contenido");
const nombreEstudiante = document.getElementById("consultar-estudiante");
const formulario = document.getElementById("form-consulta");
const pregunta = document.getElementById("consulta-pregunta");
const contador = document.getElementById("consulta-contador");
const alerta = document.getElementById("consulta-alerta");
const boton = document.getElementById("consulta-enviar");
const cargando = document.getElementById("consulta-cargando");
const plantillaCarga = document.getElementById("plantilla-carga");
const resultado = document.getElementById("consulta-resultado");

function actualizarContador() {
  const usados = pregunta.value.length;
  contador.textContent = `${usados} / ${MAXIMO}`;
  contador.classList.toggle("contador--cerca", usados >= MAXIMO * 0.9);
}

function ponerEnEspera(esperando) {
  boton.disabled = esperando;
  formulario.setAttribute("aria-busy", String(esperando));
  if (esperando) {
    cargando.replaceChildren(plantillaCarga.content.cloneNode(true));
  } else {
    cargando.replaceChildren();
  }
}

async function consultar(evento) {
  evento.preventDefault();
  const estudiante = estudianteActivo();
  if (boton.disabled || !estudiante) {
    return;
  }
  limpiarErrores(formulario);
  limpiarAlerta(alerta);
  // Solo se evita enviar una pregunta vacía; el resto de reglas las aplica la API.
  if (!pregunta.value.trim()) {
    mostrarErroresDeCampo(formulario, [{ campo: "pregunta", mensaje: "escribe tu necesidad antes de enviarla" }]);
    enfocarPrimerError(formulario);
    return;
  }

  resultado.replaceChildren();
  ponerEnEspera(true);
  try {
    const datos = await registrarConsulta({ estudianteId: estudiante.id, pregunta: pregunta.value });
    if (estudianteActivo()?.id !== estudiante.id) {
      return;
    }
    const vista = crearResultado(datos, { titulo: "Resultado de tu consulta" });
    resultado.replaceChildren(vista);
    enfocarResultado(vista);
  } catch (error) {
    const sinCampo = mostrarErroresDeCampo(formulario, error.detalles);
    mostrarAlerta(alerta, error.message, sinCampo);
    if (!enfocarPrimerError(formulario)) {
      pregunta.focus();
    }
  } finally {
    ponerEnEspera(false);
  }
}

function pintarEstudiante(estudiante, cambioDeId) {
  sinEstudiante.hidden = Boolean(estudiante);
  contenido.hidden = !estudiante;
  nombreEstudiante.textContent = estudiante ? nombreVisible(estudiante) : "";
  if (cambioDeId) {
    resultado.replaceChildren();
    limpiarErrores(formulario);
    limpiarAlerta(alerta);
  }
}

export function iniciarConsulta() {
  formulario.addEventListener("submit", consultar);
  pregunta.addEventListener("input", actualizarContador);
  alCambiarEstudiante(pintarEstudiante);
  pintarEstudiante(estudianteActivo(), false);
  actualizarContador();
}
