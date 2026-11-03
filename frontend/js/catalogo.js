// Catálogo (E-05): tarjetas de cursos activos; el filtrado por categoría y nivel lo hace la API.
import { listarCursos } from "./api.js";
import { crearChipsCurso, crearDescripcion } from "./componentes.js";
import { crear, nuevoId } from "./dom.js";
import { limpiarAlerta, mostrarAlerta } from "./formulario.js";
import { alMostrarPestana } from "./pestanas.js";

const formFiltros = document.getElementById("form-filtros");
const filtroCategoria = document.getElementById("filtro-categoria");
const filtroNivel = document.getElementById("filtro-nivel");
const estado = document.getElementById("catalogo-estado");
const alerta = document.getElementById("catalogo-alerta");
const lista = document.getElementById("catalogo-lista");

let solicitud = 0;
let ultimaCargaCorrecta = false;

/** Las opciones de categoría salen de E-05 sin filtros. */
function pintarCategorias(cursos) {
  const elegida = filtroCategoria.value;
  const categorias = [...new Set(cursos.map((curso) => curso.categoria))].sort((a, b) => a.localeCompare(b, "es"));
  filtroCategoria.replaceChildren(
    crear("option", { texto: "Todas las categorías", atributos: { value: "" } }),
    ...categorias.map((categoria) => crear("option", { texto: categoria, atributos: { value: categoria } })),
  );
  filtroCategoria.value = categorias.includes(elegida) ? elegida : "";
}

function crearTarjeta(curso) {
  const idTitulo = nuevoId("curso");
  return crear("li", {}, [
    crear("article", { clase: "tarjeta tarjeta-curso", atributos: { "aria-labelledby": idTitulo } }, [
      crear("h3", { texto: curso.nombre, atributos: { id: idTitulo } }),
      crearChipsCurso(curso),
      crearDescripcion(curso.descripcion),
    ]),
  ]);
}

function describir(cantidad, { categoria, nivel }) {
  const filtros = [categoria, nivel].filter(Boolean).join(" · ");
  if (cantidad === 0) {
    return filtros ? `No hay cursos activos con estos filtros (${filtros}).` : "No hay cursos activos en el catálogo.";
  }
  const texto = cantidad === 1 ? "1 curso activo" : `${cantidad} cursos activos`;
  return filtros ? `${texto} · ${filtros}` : texto;
}

async function cargarCursos() {
  const numero = ++solicitud;
  const filtros = { categoria: filtroCategoria.value, nivel: filtroNivel.value };
  limpiarAlerta(alerta);
  lista.setAttribute("aria-busy", "true");
  estado.textContent = "Cargando cursos…";
  try {
    const cursos = await listarCursos(filtros);
    if (numero !== solicitud) {
      return;
    }
    if (!filtros.categoria && !filtros.nivel) {
      pintarCategorias(cursos);
    }
    lista.replaceChildren(...cursos.map(crearTarjeta));
    estado.textContent = describir(cursos.length, filtros);
    ultimaCargaCorrecta = true;
  } catch (error) {
    if (numero !== solicitud) {
      return;
    }
    ultimaCargaCorrecta = false;
    lista.replaceChildren();
    estado.textContent = "";
    mostrarAlerta(alerta, error.message, error.detalles);
  } finally {
    if (numero === solicitud) {
      lista.setAttribute("aria-busy", "false");
    }
  }
}

export function iniciarCatalogo() {
  formFiltros.addEventListener("submit", (evento) => evento.preventDefault());
  filtroCategoria.addEventListener("change", cargarCursos);
  filtroNivel.addEventListener("change", cargarCursos);
  // Si la última carga falló (por ejemplo, con la API detenida), se reintenta al volver a la pestaña.
  alMostrarPestana("catalogo", () => {
    if (!ultimaCargaCorrecta) {
      cargarCursos();
    }
  });
  cargarCursos();
}
