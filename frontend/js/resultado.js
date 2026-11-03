// Resultado de una consulta (ConsultaResultado de E-11 o ConsultaDetalle de E-12).
import { crearCalificacion } from "./calificacion.js";
import { claseEstado, crearChipsCurso, crearDescripcion, crearEstado, crearFecha, formatearSimilitud } from "./componentes.js";
import { crear, crearIcono, nuevoId } from "./dom.js";

/** La recomendación va como texto plano; el CSS respeta sus saltos de línea. */
function crearRecomendacion(respuesta) {
  const id = nuevoId("recomendacion");
  return crear("div", { clase: "recomendacion", atributos: { role: "region", "aria-labelledby": id } }, [
    crear("p", { clase: "recomendacion-etiqueta", atributos: { id } }, [crearIcono("consultar"), "Recomendación de RutaIA"]),
    crear("p", { clase: "recomendacion-texto", texto: respuesta }),
  ]);
}

function crearMensaje(estado, mensaje) {
  const icono = estado === "Pendiente" ? "reloj" : "alerta";
  return crear("div", { clase: `mensaje-estado mensaje-estado--${claseEstado(estado)}` }, [
    crearIcono(icono),
    crear("p", { texto: mensaje }),
  ]);
}

function crearParada(fuente, numero) {
  const idTitulo = nuevoId("fuente");
  const similitud = Math.min(Math.max(Number(fuente.similitud) || 0, 0), 1);
  const relleno = crear("span", { clase: "similitud-relleno" });
  relleno.style.width = `${(similitud * 100).toFixed(1)}%`;
  return crear("li", { clase: "parada" }, [
    crear("span", { clase: "parada-numero", texto: numero, atributos: { "aria-hidden": "true" } }),
    crear("article", { clase: "tarjeta tarjeta-fuente", atributos: { "aria-labelledby": idTitulo } }, [
      crear("h5", { texto: fuente.nombre, atributos: { id: idTitulo } }),
      crearChipsCurso(fuente),
      crearDescripcion(fuente.descripcion),
      crear("p", { clase: "similitud" }, [
        crear("span", { clase: "similitud-etiqueta", texto: "Similitud" }),
        crear("span", { clase: "similitud-barra", atributos: { "aria-hidden": "true" } }, [relleno]),
        crear("span", { clase: "similitud-valor", texto: formatearSimilitud(fuente.similitud) }),
      ]),
    ]),
  ]);
}

function crearFuentes(fuentes) {
  const id = nuevoId("fuentes");
  return crear("section", { clase: "fuentes", atributos: { "aria-labelledby": id } }, [
    crear("h4", { clase: "fuentes-titulo", atributos: { id } }, ["Fuentes del catálogo ", crear("span", { texto: `(${fuentes.length})` })]),
    crear("p", {
      clase: "texto-suave",
      texto: "Cursos en los que se basa la recomendación, de mayor a menor similitud con tu necesidad.",
    }),
    crear("ol", { clase: "ruta" }, fuentes.map((fuente, indice) => crearParada(fuente, indice + 1))),
  ]);
}

/**
 * `datos.calificacion` solo existe en E-12: undefined (E-11) o null muestran el formulario,
 * un objeto muestra la calificación registrada.
 */
export function crearResultado(datos, { titulo }) {
  const idTitulo = nuevoId("resultado");
  const seccion = crear("section", { clase: "resultado", atributos: { "aria-labelledby": idTitulo } }, [
    crear("header", { clase: "resultado-cabecera" }, [
      crear("div", {}, [
        crear("h3", { clase: "resultado-titulo", texto: titulo, atributos: { id: idTitulo, tabindex: "-1" } }),
        crear("p", { clase: "resultado-meta" }, [`Consulta ${datos.consultaId} · `, crearFecha(datos.fecha)]),
      ]),
      crearEstado(datos.estado),
    ]),
    crear("div", { clase: "pregunta-bloque" }, [
      crear("p", { clase: "etiqueta", texto: "Pregunta" }),
      crear("p", { clase: "pregunta-texto", texto: datos.pregunta }),
    ]),
  ]);

  if (datos.respuesta) {
    seccion.append(crearRecomendacion(datos.respuesta));
  }
  if (datos.mensaje) {
    seccion.append(crearMensaje(datos.estado, datos.mensaje));
  }
  if (datos.fuentes?.length) {
    seccion.append(crearFuentes(datos.fuentes));
  }
  if (datos.recomendacionId !== null && datos.recomendacionId !== undefined) {
    seccion.append(
      crearCalificacion({
        recomendacionId: datos.recomendacionId,
        consultaId: datos.consultaId,
        calificacion: datos.calificacion,
      }),
    );
  }
  return seccion;
}

export const enfocarResultado = (seccion) => seccion.querySelector(".resultado-titulo")?.focus();
