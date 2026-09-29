import { aFechaLocal, escaparHtml, estadoVacio, fmtNumero } from './util.js';

export async function renderProgreso(contenedor, habitos, obtenerRegistros, acciones) {
  if (habitos.length === 0) {
    contenedor.innerHTML = `
      <div class="section-head">
        <p class="eyebrow">PROGRESO</p>
        <h1>Progreso</h1>
      </div>
      ${estadoVacio('Crea hábitos para empezar a ver tu progreso.')}`;
    return;
  }

  const fragmento = [];
  const resumen = { racha: 0, mejorRacha: 0, hechos: 0, diasEsperados: habitos.length * 7 };
  for (const habito of habitos) {
    const registros = await obtenerRegistros(habito.id);
    fragmento.push(bloqueHabito(habito, registros));
    const stats = estadisticas(registros);
    resumen.racha = Math.max(resumen.racha, stats.racha);
    resumen.mejorRacha = Math.max(resumen.mejorRacha, stats.mejorRacha);
    resumen.hechos += stats.hechos;
  }

  contenedor.innerHTML = `
    <div class="section-head">
      <p class="eyebrow">PROGRESO</p>
      <h1>Progreso</h1>
    </div>
    <div class="admin-tarjetas resumen-tarjetas">
      ${tarjetaResumen('Racha actual', `${resumen.racha} d`, 'Máximo de tus hábitos')}
      ${tarjetaResumen('Mejor racha', `${resumen.mejorRacha} d`, 'Consecutivos cumplidos')}
      ${tarjetaResumen('Semana', `${Math.round((resumen.hechos / resumen.diasEsperados) * 100)}%`, `${resumen.hechos}/${resumen.diasEsperados} días cumplidos`)}
    </div>
    <p data-role="aviso" class="aviso hidden" role="status"></p>
    <div class="progreso-list">
      ${fragmento.join('')}
    </div>`;

  contenedor.querySelectorAll('button[data-accion="registrar"]').forEach(boton => {
    const habito = habitos.find(h => h.id === Number(boton.dataset.id));
    boton.addEventListener('click', () => acciones.onRegistrar(habito));
  });
}

function tarjetaResumen(titulo, valor, detalle) {
  return `
    <div class="admin-tarjeta">
      <span class="admin-tarjeta-titulo">${escaparHtml(titulo)}</span>
      <strong>${escaparHtml(valor)}</strong>
      <span class="admin-tarjeta-detalle">${escaparHtml(detalle)}</span>
    </div>`;
}

function estadisticas(registros) {
  const porFecha = new Map(registros.map(r => [r.fecha, r]));
  const hoy = aFechaLocal(new Date());

  let racha = 0;
  let dia = porFecha.has(hoy) ? 0 : 1;
  while (true) {
    const fecha = aFechaLocal(offsetDesde(new Date(), -dia));
    const r = porFecha.get(fecha);
    if (!r || !r.cumplido) break;
    racha++;
    dia++;
  }

  const ordenados = [...registros].sort((a, b) => a.fecha.localeCompare(b.fecha));
  let mejor = 0;
  let serie = 0;
  let anterior = null;
  for (const r of ordenados) {
    const consecutiva = anterior != null && diasEntre(anterior, r.fecha) === 1;
    serie = r.cumplido ? (consecutiva ? serie + 1 : 1) : 0;
    mejor = Math.max(mejor, serie);
    anterior = r.fecha;
  }

  const diasSemana = new Set();
  for (let i = 0; i < 7; i++) diasSemana.add(aFechaLocal(offsetDesde(new Date(), -i)));
  const hechos = registros.filter(r => r.cumplido && diasSemana.has(r.fecha)).length;

  return { racha, mejorRacha: mejor, hechos };
}

function offsetDesde(fecha, dias) {
  const copia = new Date(fecha);
  copia.setDate(copia.getDate() + dias);
  return copia;
}

function diasEntre(a, b) {
  const [aa, ma, da] = a.split('-').map(Number);
  const [ab, mb, db] = b.split('-').map(Number);
  const fechaA = new Date(aa, ma - 1, da);
  const fechaB = new Date(ab, mb - 1, db);
  return Math.round((fechaB - fechaA) / 86400000);
}

function bloqueHabito(habito, registros) {
  const porFecha = new Map(registros.map(r => [r.fecha, r]));
  const dias = ultimosDias(7);
  const celdas = dias
    .map(d => {
      const r = porFecha.get(d.fecha);
      const relleno = r ? (r.cumplido ? 'hecho' : 'fallado') : '';
      const titulo = r ? `${d.etiqueta}: ${fmtNumero(r.valor)} ${r.cumplido ? '✓' : '✗'}` : d.etiqueta;
      return `<span class="dia ${relleno}" title="${escapes(titulo)}"><b>${d.numero}</b></span>`;
    })
    .join('');

  const filas = registros
    .slice(0, 14)
    .map(r => `
      <tr>
        <td>${escaparHtml(r.fecha)}</td>
        <td>${fmtNumero(r.valor)} ${escaparHtml(habito.unidadMedida || '')}</td>
        <td class="${r.cumplido ? 'hecho' : 'fallado'}">${r.cumplido ? 'Cumplido' : 'No cumplido'}</td>
        ${r.nota ? `<td>${escaparHtml(r.nota)}</td>` : '<td></td>'}
      </tr>`)
    .join('');

  return `
    <article class="progreso-bloque">
      <div class="progreso-cabecera">
        <h2>${escaparHtml(habito.nombre)}</h2>
        <button type="button" data-accion="registrar" data-id="${habito.id}">Registrar</button>
      </div>
      <div class="dias">${celdas}</div>
      ${filas ? `
        <table class="registros">
          <thead><tr><th>Fecha</th><th>Valor</th><th>Estado</th><th>Nota</th></tr></thead>
          <tbody>${filas}</tbody>
        </table>` : '<p class="vacio">Sin registros todavía.</p>'}
    </article>`;
}

function ultimosDias(cantidad) {
  const resultado = [];
  for (let i = cantidad - 1; i >= 0; i--) {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() - i);
    const iso = aFechaLocal(fecha);
    resultado.push({ fecha: iso, numero: Number(iso.slice(8, 10)), etiqueta: iso });
  }
  return resultado;
}

function escapes(texto) {
  return String(texto).replaceAll('"', '&quot;');
}