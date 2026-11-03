// Estudiante activo: su id vive en localStorage y el resto de la página se entera de los cambios.

const CLAVE = "rutaia.estudianteId";
const oyentes = [];
let activo = null;

/** Id guardado, o null si no hay uno válido o el almacenamiento no está disponible. */
export function leerIdGuardado() {
  try {
    const valor = localStorage.getItem(CLAVE);
    const id = Number(valor);
    return valor && Number.isInteger(id) && id > 0 ? id : null;
  } catch {
    return null;
  }
}

function guardarId(id) {
  try {
    localStorage.setItem(CLAVE, String(id));
  } catch {
    // Sin almacenamiento el estudiante activo dura lo que dure la página.
  }
}

function borrarId() {
  try {
    localStorage.removeItem(CLAVE);
  } catch {
    // Nada que borrar si el almacenamiento no está disponible.
  }
}

export const estudianteActivo = () => activo;

export const nombreVisible = (estudiante) => estudiante.nombreCompleto ?? `Estudiante ${estudiante.id}`;

/** Cada oyente recibe el estudiante (o null) y si cambió el id; un mismo id con más datos no cuenta como cambio. */
export function alCambiarEstudiante(oyente) {
  oyentes.push(oyente);
}

function avisar(cambioDeId) {
  for (const oyente of oyentes) {
    oyente(activo, cambioDeId);
  }
}

/** Acepta un Estudiante completo o, mientras se valida con E-03, solo { id }. */
export function activarEstudiante(estudiante) {
  const cambioDeId = activo?.id !== estudiante.id;
  activo = estudiante;
  guardarId(estudiante.id);
  avisar(cambioDeId);
}

export function olvidarEstudiante() {
  const cambioDeId = activo !== null;
  activo = null;
  borrarId();
  avisar(cambioDeId);
}
