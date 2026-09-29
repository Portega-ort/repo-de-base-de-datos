import { escaparHtml } from './util.js';

export function renderPerfil(contenedor, usuario, acciones) {
  contenedor.innerHTML = `
    <div class="section-head">
      <p class="eyebrow">TU CUENTA</p>
      <h1>Perfil</h1>
      <p class="meta-line">Miembro desde ${escaparHtml(usuario.fechaRegistro)}</p>
    </div>
    <p data-role="aviso" class="aviso hidden" role="status"></p>
    <form id="form-perfil" class="form-panel" novalidate>
      <label>Nombre
        <input name="nombre" type="text" maxlength="80" value="${escaparHtml(usuario.nombre)}" required>
      </label>
      <label>Apellido
        <input name="apellido" type="text" maxlength="80" value="${escaparHtml(usuario.apellido)}" required>
      </label>
      <label>Correo electrónico
        <input name="email" type="email" value="${escaparHtml(usuario.email)}" required>
      </label>
      <h2>Cambiar contraseña</h2>
      <p class="hint">Déjala vacía para conservar la contraseña actual.</p>
      <label>Nueva contraseña
        <input name="password" type="password" minlength="8" autocomplete="new-password">
      </label>
      <p data-role="error" class="form-error hidden"></p>
      <div class="acciones">
        <button type="submit" class="primary">Guardar cambios</button>
      </div>
    </form>`;

  contenedor.querySelector('#form-perfil').addEventListener('submit', evento => {
    evento.preventDefault();
    acciones.onGuardar(Object.fromEntries(new FormData(evento.currentTarget)));
  });
}