// Calificación de una recomendación (E-13): de 1 a 5 estrellas y comentario opcional, una sola vez.
import { calificarRecomendacion, obtenerConsulta } from "./api.js";
import { crearFecha } from "./componentes.js";
import { crear, nuevoId } from "./dom.js";
import { enfocarPrimerError, limpiarAlerta, limpiarErrores, mostrarAlerta, mostrarErroresDeCampo } from "./formulario.js";

const MAXIMO_COMENTARIO = 500;
const VALORES = [1, 2, 3, 4, 5];

const textoEstrellas = (valor) => (valor === 1 ? "1 estrella" : `${valor} estrellas`);

function crearEstrellasLectura(puntuacion) {
  return crear("div", { clase: "estrellas-fila" }, [
    crear(
      "span",
      { clase: "estrellas-lectura", atributos: { role: "img", "aria-label": `${puntuacion} de 5 estrellas` } },
      VALORES.map((valor) =>
        crear("span", { clase: valor <= puntuacion ? "estrella--llena" : "", texto: valor <= puntuacion ? "★" : "☆" }),
      ),
    ),
    crear("span", { clase: "estrellas-valor", texto: `${puntuacion} de 5`, atributos: { "aria-hidden": "true" } }),
  ]);
}

function pintarRegistrada(partes, calificacion, aviso) {
  partes.titulo.textContent = "Tu calificación";
  partes.aviso.textContent = aviso ?? "";
  partes.cuerpo.replaceChildren(
    crearEstrellasLectura(calificacion.puntuacion),
    calificacion.comentario
      ? crear("p", { clase: "calificacion-comentario", texto: calificacion.comentario })
      : crear("p", { clase: "calificacion-comentario calificacion-comentario--vacio", texto: "Sin comentario." }),
    crear("p", { clase: "resultado-meta" }, ["Calificada el ", crearFecha(calificacion.fecha)]),
  );
}

function pintarFormulario(partes, recomendacionId, consultaId) {
  const idErrorPuntuacion = nuevoId("error-puntuacion");
  const idComentario = nuevoId("comentario");
  const idContador = nuevoId("contador-comentario");
  const idErrorComentario = nuevoId("error-comentario");

  const opciones = VALORES.map((valor) => {
    const id = nuevoId("estrella");
    const glifo = crear("span", { texto: "☆", atributos: { "aria-hidden": "true" } });
    const etiqueta = crear("label", { clase: "estrella", atributos: { for: id } }, [
      glifo,
      crear("span", { clase: "sr-only", texto: textoEstrellas(valor) }),
    ]);
    const entrada = crear("input", {
      atributos: { type: "radio", id, name: "puntuacion", value: valor, "aria-describedby": idErrorPuntuacion },
    });
    return { valor, glifo, etiqueta, entrada };
  });
  const valorElegido = crear("span", { clase: "estrellas-valor", texto: "Sin elegir", atributos: { "aria-hidden": "true" } });
  const grupo = crear(
    "div",
    { clase: "estrellas" },
    opciones.map(({ entrada, etiqueta }) => crear("span", { clase: "estrella-opcion" }, [entrada, etiqueta])),
  );
  const comentario = crear("textarea", {
    atributos: {
      id: idComentario,
      name: "comentario",
      rows: 3,
      maxlength: MAXIMO_COMENTARIO,
      "aria-describedby": `${idContador} ${idErrorComentario}`,
    },
  });
  const contador = crear("p", { clase: "contador", texto: `0 / ${MAXIMO_COMENTARIO}`, atributos: { id: idContador } });
  const alerta = crear("div", { clase: "alerta", atributos: { role: "alert" } });
  const boton = crear("button", { clase: "boton", texto: "Enviar calificación", atributos: { type: "submit" } });

  const formulario = crear("form", { clase: "formulario", atributos: { novalidate: true } }, [
    crear("fieldset", { clase: "campo" }, [
      crear("legend", { texto: "Puntuación, de 1 a 5 estrellas" }),
      crear("div", { clase: "estrellas-fila" }, [grupo, valorElegido]),
      crear("p", { clase: "campo-error", atributos: { id: idErrorPuntuacion, "data-error-de": "puntuacion" } }),
    ]),
    crear("div", { clase: "campo" }, [
      crear("label", { texto: "Comentario (opcional)", atributos: { for: idComentario } }),
      comentario,
      crear("div", { clase: "campo-pie" }, [
        crear("p", { clase: "campo-error", atributos: { id: idErrorComentario, "data-error-de": "comentario" } }),
        contador,
      ]),
    ]),
    alerta,
    boton,
  ]);

  const elegida = () => Number(formulario.elements.puntuacion.value) || 0;
  const pintarEstrellas = (hasta) => {
    for (const { valor, glifo, etiqueta } of opciones) {
      glifo.textContent = valor <= hasta ? "★" : "☆";
      etiqueta.classList.toggle("estrella--llena", valor <= hasta);
    }
  };

  grupo.addEventListener("change", () => {
    pintarEstrellas(elegida());
    valorElegido.textContent = `${elegida()} de 5`;
  });
  for (const { valor, etiqueta } of opciones) {
    etiqueta.addEventListener("mouseenter", () => pintarEstrellas(valor));
  }
  grupo.addEventListener("mouseleave", () => pintarEstrellas(elegida()));
  comentario.addEventListener("input", () => {
    contador.textContent = `${comentario.value.length} / ${MAXIMO_COMENTARIO}`;
  });

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    if (boton.disabled) {
      return;
    }
    limpiarErrores(formulario);
    limpiarAlerta(alerta);
    const puntuacion = elegida();
    if (!puntuacion) {
      mostrarErroresDeCampo(formulario, [{ campo: "puntuacion", mensaje: "elige de 1 a 5 estrellas" }]);
      enfocarPrimerError(formulario);
      return;
    }

    boton.disabled = true;
    try {
      const calificacion = await calificarRecomendacion(recomendacionId, {
        puntuacion,
        comentario: comentario.value.trim() ? comentario.value : null,
      });
      pintarRegistrada(partes, calificacion, "Gracias: tu calificación quedó registrada.");
      partes.titulo.focus();
    } catch (error) {
      if (error.status === 409 && (await mostrarExistente(partes, consultaId))) {
        return;
      }
      const sinCampo = mostrarErroresDeCampo(formulario, error.detalles);
      mostrarAlerta(alerta, error.message, sinCampo);
      enfocarPrimerError(formulario);
      boton.disabled = error.status === 409;
    }
  });

  partes.titulo.textContent = "Califica esta recomendación";
  partes.cuerpo.replaceChildren(formulario);
}

/** Ante un 409 la calificación existente se lee del detalle de la consulta (E-12). */
async function mostrarExistente(partes, consultaId) {
  try {
    const detalle = await obtenerConsulta(consultaId);
    if (!detalle.calificacion) {
      return false;
    }
    pintarRegistrada(partes, detalle.calificacion, "Esta recomendación ya estaba calificada; esta es la calificación registrada.");
    partes.titulo.focus();
    return true;
  } catch {
    return false;
  }
}

export function crearCalificacion({ recomendacionId, consultaId, calificacion }) {
  const idTitulo = nuevoId("calificacion");
  const partes = {
    titulo: crear("h4", { atributos: { id: idTitulo, tabindex: "-1" } }),
    aviso: crear("p", { clase: "aviso-exito", atributos: { role: "status" } }),
    cuerpo: crear("div"),
  };
  if (calificacion) {
    pintarRegistrada(partes, calificacion);
  } else {
    pintarFormulario(partes, recomendacionId, consultaId);
  }
  return crear("section", { clase: "tarjeta calificacion", atributos: { "aria-labelledby": idTitulo } }, [
    partes.titulo,
    partes.aviso,
    partes.cuerpo,
  ]);
}
