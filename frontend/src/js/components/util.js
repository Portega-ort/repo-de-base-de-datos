export function escaparHtml(texto) {
  if (texto == null) return '';
  return String(texto)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

export function fmtNumero(valor) {
  if (valor == null) return '';
  return Number(valor).toLocaleString('es');
}

export function hoy() {
  return aFechaLocal(new Date());
}

export function aFechaLocal(fecha) {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}

export function estadoVacio(mensaje) {
  return `
    <div class="empty-state">
      <div class="empty-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="var(--accent-ink)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M13 2 4.1 12.4a2 2 0 0 0 1.5 3.3H11l-1 6 8.9-10.2a2 2 0 0 0-1.5-3.3H13l1-6Z"/>
        </svg>
      </div>
      <p class="vacio">${escaparHtml(mensaje)}</p>
    </div>`;
}

export function mostrarAviso(contenedor, mensaje, esError = false) {
  let aviso = contenedor.querySelector('[data-role="aviso"]');
  if (!aviso) {
    aviso = document.createElement('p');
    aviso.dataset.role = 'aviso';
    contenedor.prepend(aviso);
  }
  aviso.textContent = mensaje;
  aviso.className = esError ? 'aviso aviso-error' : 'aviso aviso-exito';
}