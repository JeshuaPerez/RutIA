// Pestañas accesibles (patrón tablist): flechas, Inicio y Fin mueven el foco y activan la pestaña.

const pestanas = [...document.querySelectorAll('[role="tab"]')];
const oyentes = new Map();
let actual = null;

export const pestanaActual = () => actual;

export function alMostrarPestana(nombre, oyente) {
  oyentes.set(nombre, [...(oyentes.get(nombre) ?? []), oyente]);
}

export function mostrarPestana(nombre, { enfocarTitulo = false } = {}) {
  const cambio = nombre !== actual;
  for (const pestana of pestanas) {
    const activa = pestana.dataset.panel === nombre;
    pestana.setAttribute("aria-selected", String(activa));
    pestana.tabIndex = activa ? 0 : -1;
    document.getElementById(pestana.getAttribute("aria-controls")).hidden = !activa;
  }
  actual = nombre;
  if (cambio) {
    window.scrollTo(0, 0);
  }
  if (enfocarTitulo) {
    document.querySelector(`#panel-${nombre} h2`)?.focus();
  }
  for (const oyente of oyentes.get(nombre) ?? []) {
    oyente();
  }
}

function moverConTeclado(evento) {
  const indice = pestanas.indexOf(evento.currentTarget);
  const destinos = {
    ArrowRight: pestanas[(indice + 1) % pestanas.length],
    ArrowLeft: pestanas[(indice - 1 + pestanas.length) % pestanas.length],
    Home: pestanas[0],
    End: pestanas[pestanas.length - 1],
  };
  const destino = destinos[evento.key];
  if (!destino) {
    return;
  }
  evento.preventDefault();
  mostrarPestana(destino.dataset.panel);
  destino.focus();
}

export function iniciarPestanas() {
  for (const pestana of pestanas) {
    pestana.addEventListener("click", () => mostrarPestana(pestana.dataset.panel));
    pestana.addEventListener("keydown", moverConTeclado);
  }
}
