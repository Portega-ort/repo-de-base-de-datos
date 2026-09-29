const backdrop = document.querySelector('#dialogo');
const titulo = document.querySelector('#dialogo-titulo');
const contenido = document.querySelector('#dialogo-contenido');

export function abrirDialogo(nuevoTitulo, html) {
  titulo.textContent = nuevoTitulo;
  contenido.innerHTML = html;
  contenido.querySelectorAll('[data-role="cancelar"]').forEach(boton => {
    boton.addEventListener('click', cerrarDialogo);
  });
  backdrop.addEventListener('click', evento => {
    if (evento.target === backdrop) cerrarDialogo();
  });
  backdrop.classList.remove('hidden');
}

export function cerrarDialogo() {
  backdrop.classList.add('hidden');
  contenido.innerHTML = '';
}