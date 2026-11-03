// Cliente de la API de RutaIA. Toda petición sale de API_BASE_URL y todo error se convierte en ApiError (C6).
import { API_BASE_URL } from "./config.js";

export const MSG_SIN_CONEXION = "No se pudo conectar con el servidor";
const MSG_RESPUESTA_INESPERADA = "El servidor respondió de forma inesperada. Intenta de nuevo más tarde.";

/** Error con el cuerpo de C6: status, mensaje y detalles por campo. status 0 = no hubo respuesta del servidor. */
export class ApiError extends Error {
  constructor(status, mensaje, detalles = []) {
    super(mensaje);
    this.name = "ApiError";
    this.status = status;
    this.detalles = Array.isArray(detalles) ? detalles : [];
  }
}

async function solicitar(ruta, { metodo = "GET", cuerpo } = {}) {
  const opciones = { method: metodo, headers: { Accept: "application/json" } };
  if (cuerpo !== undefined) {
    opciones.headers["Content-Type"] = "application/json";
    opciones.body = JSON.stringify(cuerpo);
  }

  let respuesta;
  let datos;
  try {
    respuesta = await fetch(API_BASE_URL + ruta, opciones);
    datos = await leerJson(respuesta);
  } catch {
    // fetch solo rechaza si no hubo respuesta: servidor detenido, red caída o CORS.
    throw new ApiError(0, MSG_SIN_CONEXION);
  }

  if (respuesta.ok && datos !== null) {
    return datos;
  }
  if (!respuesta.ok && typeof datos?.mensaje === "string") {
    throw new ApiError(respuesta.status, datos.mensaje, datos.detalles);
  }
  throw new ApiError(respuesta.status, MSG_RESPUESTA_INESPERADA);
}

async function leerJson(respuesta) {
  const texto = await respuesta.text();
  if (!texto) {
    return null;
  }
  try {
    return JSON.parse(texto);
  } catch {
    return null;
  }
}

const segmento = (valor) => encodeURIComponent(String(valor));

// E-01
export const registrarEstudiante = (datos) => solicitar("/estudiantes", { metodo: "POST", cuerpo: datos });

// E-02
export const listarEstudiantes = () => solicitar("/estudiantes");

// E-03
export const obtenerEstudiante = (estudianteId) => solicitar(`/estudiantes/${segmento(estudianteId)}`);

// E-04
export const obtenerHistorial = (estudianteId) => solicitar(`/estudiantes/${segmento(estudianteId)}/historial`);

// E-05: el filtrado lo hace la API
export function listarCursos({ categoria = "", nivel = "" } = {}) {
  const parametros = [];
  if (categoria) {
    parametros.push(`categoria=${encodeURIComponent(categoria)}`);
  }
  if (nivel) {
    parametros.push(`nivel=${encodeURIComponent(nivel)}`);
  }
  return solicitar(parametros.length ? `/cursos?${parametros.join("&")}` : "/cursos");
}

// E-11
export const registrarConsulta = (datos) => solicitar("/consultas", { metodo: "POST", cuerpo: datos });

// E-12
export const obtenerConsulta = (consultaId) => solicitar(`/consultas/${segmento(consultaId)}`);

// E-13
export const calificarRecomendacion = (recomendacionId, datos) =>
  solicitar(`/recomendaciones/${segmento(recomendacionId)}/calificacion`, { metodo: "POST", cuerpo: datos });
