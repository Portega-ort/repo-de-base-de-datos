import { escaparHtml, fmtNumero } from './util.js';

export function renderAdmin(contenedor, reporte) {
  const { resumen, actividad, categorias, usuarios } = reporte;
  contenedor.innerHTML = `
    <div class="section-head">
      <p class="eyebrow">ADMINISTRACIÓN</p>
      <h1>Reportes</h1>
      <p class="meta-line">Información de la plataforma y sus usuarios.</p>
    </div>
    <div class="admin-tarjetas">
      ${tarjeta('Usuarios', fmtNumero(resumen.totalUsuarios), 'Cuentas registradas')}
      ${tarjeta('Altas (7 días)', fmtNumero(resumen.altas7dias), 'Usuarios nuevos')}
      ${tarjeta('Hábitos activos', fmtNumero(resumen.habitosActivos), 'En seguimiento')}
      ${tarjeta('Registros', fmtNumero(resumen.registrosTotales), 'Avances totales')}
      ${tarjeta('Cumplimiento', `${resumen.cumplimientoGlobal}%`, 'Promedio global')}
    </div>

    <div class="admin-cols">
      <section class="admin-panel">
        <h2>Actividad diaria (últimos 14 días)</h2>
        ${barrasActividad(actividad)}
      </section>
      <section class="admin-panel">
        <h2>Hábitos por categoría</h2>
        ${barrasCategorias(categorias)}
      </section>
    </div>

    <section class="admin-panel">
      <div class="admin-tabla-cabecera">
        <h2>Usuarios</h2>
        <label class="admin-buscar">Buscar
          <input id="admin-buscar" type="search" placeholder="Nombre o correo…">
        </label>
      </div>
      <div class="admin-tabla-wrap">
        <table class="registros admin-tabla">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Alta</th>
              <th class="num">Hábitos</th>
              <th class="num">Registros</th>
              <th class="num">Cumplimiento</th>
              <th class="num">Racha</th>
              <th>Último registro</th>
            </tr>
          </thead>
          <tbody id="admin-tabla-filas">
            ${filasUsuarios(usuarios)}
          </tbody>
        </table>
      </div>
    </section>
  `;

  const buscador = contenedor.querySelector('#admin-buscar');
  buscador.addEventListener('input', () => {
    const termino = buscador.value.trim().toLowerCase();
    contenedor.querySelector('#admin-tabla-filas').innerHTML = filasUsuarios(
      usuarios.filter(u => !termino || `${u.nombre} ${u.apellido} ${u.email}`.toLowerCase().includes(termino))
    );
  });
}

function tarjeta(titulo, valor, detalle) {
  return `
    <div class="admin-tarjeta">
      <span class="admin-tarjeta-titulo">${escaparHtml(titulo)}</span>
      <strong>${escaparHtml(valor)}</strong>
      <span class="admin-tarjeta-detalle">${escaparHtml(detalle)}</span>
    </div>`;
}

function barrasActividad(actividad) {
  const porDia = new Map(actividad.map(d => [d.fecha, d.cantidad]));
  const maximo = Math.max(1, ...actividad.map(d => d.cantidad));
  const dias = [];
  const hoy = new Date();
  for (let i = 13; i >= 0; i--) {
    const fecha = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - i);
    const iso = aFechaLocal(fecha);
    dias.push({ iso, cantidad: porDia.get(iso) || 0 });
  }
  return `
    <div class="admin-barras" data-role="actividad">
      ${dias.map(d => `
        <div class="admin-barra" title="${escaparHtml(d.iso)} · ${d.cantidad}">
          <span class="admin-barra-fill" style="height:${Math.round((d.cantidad / maximo) * 100)}%"></span>
          <small>${d.iso.slice(8, 10)}</small>
        </div>`).join('')}
    </div>`;
}

function barrasCategorias(categorias) {
  const maximo = Math.max(1, ...categorias.map(c => c.cantidad));
  return `
    <div class="admin-ranking">
      ${categorias.map(c => `
        <div class="admin-ranking-fila">
          <span>${escaparHtml(c.nombre)}</span>
          <div class="admin-ranking-track">
            <div class="admin-ranking-fill" style="width:${Math.round((c.cantidad / maximo) * 100)}%"></div>
          </div>
          <strong>${c.cantidad}</strong>
        </div>`).join('')}
    </div>`;
}

function filasUsuarios(usuarios) {
  if (!usuarios.length) return '<tr><td colspan="7" class="vacio">Sin resultados.</td></tr>';
  return usuarios.map(u => `
    <tr>
      <td>
        <span class="admin-nombre">${escaparHtml(u.nombre)} ${escaparHtml(u.apellido)}</span>
        <span class="admin-correo">${escaparHtml(u.email)}${u.esAdmin ? ' · ADMIN' : ''}</span>
      </td>
      <td class="celda-fecha">${u.fechaRegistro.slice(0, 10)}</td>
      <td class="num">${u.habitos}</td>
      <td class="num">${u.registros}</td>
      <td class="num">${u.cumplimiento}%</td>
      <td class="num">${u.racha} d</td>
      <td class="celda-fecha">${u.ultimoRegistro ? escaparHtml(u.ultimoRegistro) : '—'}</td>
    </tr>`).join('');
}

function aFechaLocal(fecha) {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}