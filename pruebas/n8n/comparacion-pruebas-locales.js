// Pruebas sin red ni cuota: ejecuta el código de los nodos «Validar vectores» y «Calcular similitud»
// del flujo con datos simulados. Uso, desde la raíz: node pruebas/n8n/comparacion-pruebas-locales.js
const fs = require('fs');
const path = require('path');

const flujo = JSON.parse(fs.readFileSync(path.join(__dirname, '../../n8n/rutaia-comparacion-preguntas.json'), 'utf8'))[0];
const codigoDe = (nodo) => flujo.nodes.find((n) => n.name === nodo).parameters.jsCode;
const config = { modeloEmbeddings: 'nvidia/nemotron-3-embed-1b:free', dimensionVector: 2048 };
const preguntas = [{ json: { campo: 'preguntaA', texto: 'A' } }, { json: { campo: 'preguntaB', texto: 'B' } }];
// Simula $('Nodo') y $input de n8n.
const $ = (nodo) => (nodo === 'Configuración'
  ? { first: () => ({ json: config }) }
  : { all: () => preguntas });
const ejecutar = (nodo, $input) => new Function('$', '$input', codigoDe(nodo))($, $input)[0].json;

let fallas = 0;
const revisar = (nombre, obtenido, esperado) => {
  const bien = obtenido === esperado;
  if (!bien) fallas++;
  console.log(`${bien ? 'OK ' : 'MAL'} ${nombre}: ${obtenido}${bien ? '' : ` (esperado ${esperado})`}`);
};

console.log('== Calcular similitud (coseno)');
const norma = (v) => Math.sqrt(v.reduce((s, x) => s + x * x, 0));
const coseno = (va, vb) => ejecutar('Calcular similitud', {
  first: () => ({ json: {
    a: { texto: 'A', vector: va, norma: norma(va) },
    b: { texto: 'B', vector: vb, norma: norma(vb) },
  } }),
}).similitud;
revisar('vectores iguales', coseno([1, 2, 3], [1, 2, 3]), 1);
revisar('misma dirección, otra escala', coseno([1, 2, 3], [2, 4, 6]), 1);
revisar('ortogonales', coseno([1, 0], [0, 1]), 0);
revisar('opuestos', coseno([1, 2], [-1, -2]), -1);
revisar('a 45 grados', coseno([1, 0], [1, 1]), 0.7071);

console.log('== Validar vectores');
const vec = (n, valor = 0.01) => Array.from({ length: n }, () => valor);
const ok = (embedding, model = 'm') => ({ json: { statusCode: 200, body: { model, data: [{ embedding }] } } });
const validar = (respuestas) => {
  try {
    const r = ejecutar('Validar vectores', { all: () => respuestas });
    return `pasa con ${r.a.vector.length} y ${r.b.vector.length} dimensiones`;
  } catch (e) {
    return `rechaza: ${e.message}`;
  }
};
const conNaN = vec(2048);
conNaN[5] = NaN;
revisar('dos vectores válidos', validar([ok(vec(2048)), ok(vec(2048, -0.02))]), 'pasa con 2048 y 2048 dimensiones');
revisar('dimensiones distintas', validar([ok(vec(2048)), ok(vec(1024))]), 'rechaza: Los vectores no son compatibles: tienen 2048 y 1024 dimensiones');
revisar('otra dimensión en ambos', validar([ok(vec(1024)), ok(vec(1024))]), 'rechaza: Los vectores tienen 1024 dimensiones y el modelo debe dar 2048');
revisar('valor NaN', validar([ok(vec(2048)), ok(conNaN)]), 'rechaza: El vector de preguntaB tiene valores que no son números finitos');
revisar('vector de ceros', validar([ok(vec(2048)), ok(vec(2048, 0))]), 'rechaza: El vector de preguntaB es todo ceros y no se puede comparar');
revisar('vector vacío', validar([ok(vec(2048)), ok([])]), 'rechaza: OpenRouter no devolvió un vector para preguntaB');
revisar('modelos distintos', validar([ok(vec(2048), 'm1'), ok(vec(2048), 'm2')]), 'rechaza: Los vectores salieron de modelos distintos: m1 y m2');
revisar('OpenRouter 429', validar([ok(vec(2048)), { json: { statusCode: 429, body: { error: { code: 429, message: 'rate limited' } } } }]), 'rechaza: OpenRouter respondió HTTP 429 al generar el embedding de preguntaB: rate limited');
revisar('falta una respuesta', validar([ok(vec(2048))]), 'rechaza: Se esperaban 2 respuestas de OpenRouter y llegaron 1');

console.log(fallas === 0 ? 'Todas las pruebas pasaron' : `${fallas} pruebas fallaron`);
process.exit(fallas === 0 ? 0 : 1);
