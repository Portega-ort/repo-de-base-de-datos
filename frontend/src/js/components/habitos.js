import { escaparHtml, estadoVacio, fmtNumero, hoy } from './util.js';

const RECOMENDACIONES = {
  'Salud': [
    { nombre: 'Beber agua', tipoMeta: 'CUANTITATIVO', metaDiaria: 2, unidadMedida: 'litros' },
    { nombre: 'Caminar', tipoMeta: 'CUANTITATIVO', metaDiaria: 8000, unidadMedida: 'pasos' },
    { nombre: 'Entrenar', tipoMeta: 'CUANTITATIVO', metaDiaria: 30, unidadMedida: 'minutos' },
    { nombre: 'Dormir 7 horas', tipoMeta: 'BOOLEANO' }
  ],
  'Productividad': [
    { nombre: 'Planificar el día', tipoMeta: 'BOOLEANO' },
    { nombre: 'Trabajo profundo', tipoMeta: 'CUANTITATIVO', metaDiaria: 90, unidadMedida: 'minutos' },
    { nombre: 'Ordenar mi escritorio', tipoMeta: 'BOOLEANO' },
    { nombre: 'Responder correos', tipoMeta: 'BOOLEANO' }
  ],
  'Aprendizaje': [
    { nombre: 'Estudiar inglés', tipoMeta: 'CUANTITATIVO', metaDiaria: 30, unidadMedida: 'minutos' },
    { nombre: 'Leer', tipoMeta: 'CUANTITATIVO', metaDiaria: 20, unidadMedida: 'páginas' },
    { nombre: 'Practicar ejercicios', tipoMeta: 'CUANTITATIVO', metaDiaria: 20, unidadMedida: 'minutos' },
    { nombre: 'Ver una clase', tipoMeta: 'BOOLEANO' }
  ],
  'Finanzas': [
    { nombre: 'Registrar gastos', tipoMeta: 'BOOLEANO' },
    { nombre: 'Ahorrar', tipoMeta: 'CUANTITATIVO', metaDiaria: 100, unidadMedida: 'pesos' },
    { nombre: 'Revisar presupuesto', tipoMeta: 'BOOLEANO' }
  ],
  'Bienestar': [
    { nombre: 'Meditar', tipoMeta: 'CUANTITATIVO', metaDiaria: 10, unidadMedida: 'minutos' },
    { nombre: 'Escribir en un diario', tipoMeta: 'BOOLEANO' },
    { nombre: 'Estirar', tipoMeta: 'CUANTITATIVO', metaDiaria: 15, unidadMedida: 'minutos' },
    { nombre: 'Tiempo sin pantallas', tipoMeta: 'CUANTITATIVO', metaDiaria: 60, unidadMedida: 'minutos' }
  ]
};

const GENERICAS = [
  { nombre: 'Leer', tipoMeta: 'CUANTITATIVO', metaDiaria: 20, unidadMedida: 'páginas' },
  { nombre: 'Meditar', tipoMeta: 'CUANTITATIVO', metaDiaria: 10, unidadMedida: 'minutos' },
  { nombre: 'Planificar el día', tipoMeta: 'BOOLEANO' },
  { nombre: 'Registrar gastos', tipoMeta: 'BOOLEANO' }
];

export function renderLista(contenedor, habitos, colorDe, acciones) {
  const seccion = `
    <div class="section-head">
      <p class="eyebrow">MIS HÁBITOS</p>
      <h1>Hábitos</h1>
      <button id="nuevo-habito" type="button">Nuevo hábito</button>
    </div>
    <p data-role="aviso" class="aviso hidden" role="status"></p>
  `;

  if (habitos.length === 0) {
    contenedor.innerHTML = seccion + estadoVacio('Aún no tienes hábitos. Crea el primero con el botón «Nuevo hábito».');
  } else {
    contenedor.innerHTML = seccion + `
      <div class="habit-list">${habitos.map(h => tarjeta(h, colorDe(h.categoria))).join('')}</div>`;
  }

  contenedor.querySelector('#nuevo-habito').addEventListener('click', acciones.onNuevo);
  contenedor.querySelectorAll('button[data-accion]').forEach(boton => {
    const id = Number(boton.dataset.id);
    const habito = habitos.find(h => h.id === id);
    boton.addEventListener('click', () => {
      if (boton.dataset.accion === 'registrar') acciones.onRegistrar(habito);
      else if (boton.dataset.accion === 'eliminar') acciones.onEliminar(habito);
    });
  });
}

export function formularioNuevo(categorias) {
  const opciones = categorias
    .map(c => `<option value="${c.id}">${escaparHtml(c.nombre)}</option>`)
    .join('');
  return `
    <form id="form-nuevo-habito" novalidate>
      <label>Categoría
        <select name="idCategoria" required>${opciones}</select>
      </label>
      <div data-role="sugerencias" class="sugerencias" hidden></div>
      <label>Nombre del hábito
        <input name="nombre" type="text" maxlength="120" required>
      </label>
      <label>Descripción (opcional)
        <textarea name="descripcion" maxlength="500" rows="2"></textarea>
      </label>
      <label>Tipo de meta
        <select name="tipoMeta">
          <option value="BOOLEANO">Completado / no completado</option>
          <option value="CUANTITATIVO">Cantidad diaria</option>
        </select>
      </label>
      <div class="cuantitativo hidden">
        <label>Meta diaria
          <input name="metaDiaria" type="number" min="0" step="any">
        </label>
        <label>Unidad de medida
          <input name="unidadMedida" type="text" maxlength="30">
        </label>
      </div>
      <p data-role="error" class="form-error hidden"></p>
      <div class="dialogo-acciones">
        <button type="button" data-role="cancelar" class="quiet">Cancelar</button>
        <button type="submit" class="primary">Crear hábito</button>
      </div>
    </form>`;
}

export function inicializarSugerencias(form) {
  const selectCategoria = form.querySelector('select[name="idCategoria"]');
  const contenedor = form.querySelector('[data-role="sugerencias"]');
  const bloqueCuantitativo = form.querySelector('.cuantitativo');

  const render = () => {
    const nombreCategoria = selectCategoria.selectedOptions[0]
      ? selectCategoria.selectedOptions[0].textContent.trim()
      : '';
    const sugeridas = RECOMENDACIONES[nombreCategoria] || (nombreCategoria ? GENERICAS : []);
    if (sugeridas.length === 0) {
      contenedor.hidden = true;
      contenedor.innerHTML = '';
      return;
    }
    contenedor.hidden = false;
    contenedor.innerHTML = `
      <span class="sugerencia-titulo">Sugerencias para «${escaparHtml(nombreCategoria)}»</span>
      ${sugeridas.map(sugerida => `
        <button type="button" class="sugerencia-chip" data-sugerencia="${escaparHtml(sugerida.nombre)}">
          ${escaparHtml(sugerida.nombre)}
        </button>`).join('')}`;
  };

  contenedor.addEventListener('click', evento => {
    const chip = evento.target.closest('[data-sugerencia]');
    if (!chip) return;
    const sugerida = [...form.querySelectorAll('[data-sugerencia]')].find(b => b === chip);
    const nombre = sugerida.dataset.sugerencia;
    const encontrada = (RECOMENDACIONES[selectCategoria.selectedOptions[0].textContent.trim()] || GENERICAS)
      .find(s => s.nombre === nombre);
    if (!encontrada) return;
    form.querySelector('input[name="nombre"]').value = encontrada.nombre;
    form.querySelector('select[name="tipoMeta"]').value = encontrada.tipoMeta;
    bloqueCuantitativo.classList.toggle('hidden', encontrada.tipoMeta !== 'CUANTITATIVO');
    form.querySelector('input[name="metaDiaria"]').value = encontrada.metaDiaria ?? '';
    form.querySelector('input[name="unidadMedida"]').value = encontrada.unidadMedida ?? '';
  });

  selectCategoria.addEventListener('change', render);
  render();
}

export function formularioAvance(habito) {
  const esBooleano = habito.tipoMeta === 'BOOLEANO';
  const metaTexto = esBooleano
    ? 'márcalo como completado'
    : `meta diaria: ${fmtNumero(habito.metaDiaria)} ${escaparHtml(habito.unidadMedida)}`;
  return `
    <form id="form-avance" novalidate>
      <p class="meta-line"><strong>${escaparHtml(habito.nombre)}</strong> — ${metaTexto}</p>
      <label>Fecha
        <input name="fecha" type="date" value="${hoy()}">
      </label>
      ${esBooleano
        ? `<label class="check-line"><input name="cumplido" type="checkbox" checked> Lo completé hoy</label>`
        : `<label>Valor logrado
            <input name="valor" type="number" min="0" step="any" required>
          </label>`}
      <label>Nota (opcional)
        <textarea name="nota" maxlength="255" rows="2"></textarea>
      </label>
      <p data-role="error" class="form-error hidden"></p>
      <div class="dialogo-acciones">
        <button type="button" data-role="cancelar" class="quiet">Cancelar</button>
        <button type="submit" class="primary">Guardar avance</button>
      </div>
    </form>`;
}

function tarjeta(habito, color) {
  const meta = habito.tipoMeta === 'BOOLEANO'
    ? 'Completar hoy'
    : `${fmtNumero(habito.metaDiaria)} ${escaparHtml(habito.unidadMedida)}`;
  const colorChip = color ? `style="--chip:${color}"` : '';
  return `
    <article class="habit-card">
      ${habito.categoria ? `<span class="tag" ${colorChip}>${escaparHtml(habito.categoria)}</span>` : ''}
      <h2>${escaparHtml(habito.nombre)}</h2>
      ${habito.descripcion ? `<p>${escaparHtml(habito.descripcion)}</p>` : ''}
      <p class="meta-line">Meta diaria: <strong>${meta}</strong></p>
      <div class="card-actions">
        <button type="button" class="primary" data-accion="registrar" data-id="${habito.id}">Registrar avance</button>
        <button type="button" class="danger-quiet" data-accion="eliminar" data-id="${habito.id}">Eliminar</button>
      </div>
    </article>`;
}