// Errores por campo (detalles de C6) y alertas de los formularios.
import { crear } from "./dom.js";

const capitalizar = (texto) => (texto ? texto.charAt(0).toUpperCase() + texto.slice(1) : "");

function controlesDe(formulario, campo) {
  const control = formulario.elements.namedItem(campo);
  if (!control) {
    return [];
  }
  return control instanceof RadioNodeList ? [...control] : [control];
}

export function limpiarErrores(formulario) {
  for (const destino of formulario.querySelectorAll("[data-error-de]")) {
    destino.textContent = "";
  }
  for (const control of formulario.querySelectorAll("[aria-invalid]")) {
    control.removeAttribute("aria-invalid");
  }
}

/** Escribe cada detalle bajo su campo y devuelve los que no tienen un campo en el formulario. */
export function mostrarErroresDeCampo(formulario, detalles = []) {
  const porCampo = new Map();
  const sinCampo = [];
  for (const detalle of detalles) {
    const destino = formulario.querySelector(`[data-error-de="${CSS.escape(String(detalle.campo))}"]`);
    if (!destino) {
      sinCampo.push(detalle);
      continue;
    }
    porCampo.set(destino, [...(porCampo.get(destino) ?? []), capitalizar(detalle.mensaje)]);
    for (const control of controlesDe(formulario, detalle.campo)) {
      control.setAttribute("aria-invalid", "true");
    }
  }
  for (const [destino, mensajes] of porCampo) {
    destino.textContent = `${mensajes.join(". ")}.`;
  }
  return sinCampo;
}

/** Enfoca el primer control con error; devuelve false si no había ninguno. */
export function enfocarPrimerError(formulario) {
  const control = formulario.querySelector('[aria-invalid="true"]');
  control?.focus();
  return Boolean(control);
}

export function mostrarAlerta(alerta, mensaje, detalles = []) {
  alerta.replaceChildren(crear("p", { texto: mensaje }));
  if (detalles.length) {
    alerta.append(crear("ul", {}, detalles.map(({ campo, mensaje: texto }) => crear("li", { texto: `${campo}: ${texto}` }))));
  }
}

export const limpiarAlerta = (alerta) => alerta.replaceChildren();
