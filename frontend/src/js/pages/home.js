import { API, ApiError } from '../services/api.js';
import { abrirDialogo, cerrarDialogo } from '../components/dialogo.js';
import { renderLista, formularioNuevo, formularioAvance, inicializarSugerencias } from '../components/habitos.js';
import { renderProgreso } from '../components/progreso.js';
import { renderPerfil } from '../components/perfil.js';
import { renderAdmin } from '../components/admin.js';
import { mostrarAviso } from '../components/util.js';
import { iniciarTour, tourVisto } from '../components/tour.js';

const authView = document.querySelector('#auth-view');
const appView = document.querySelector('#app-view');
const authForm = document.querySelector('#auth-form');
const authToggle = document.querySelector('#auth-toggle');
const authTitle = document.querySelector('#auth-title');
const authSubmit = document.querySelector('#auth-submit');
const authError = document.querySelector('#auth-error');
const authCamposRegistro = document.querySelector('#auth-campos-registro');
const userName = document.querySelector('#user-name');
const logoutButton = document.querySelector('#logout-button');

const viewHabitos = document.querySelector('#view-habitos');
const viewProgreso = document.querySelector('#view-progreso');
const viewPerfil = document.querySelector('#view-perfil');
const viewAdmin = document.querySelector('#view-admin');
const navAdmin = document.querySelector('#nav-admin');
const navLinks = [...document.querySelectorAll('.nav-link')];

const estado = { usuario: null, categorias: [], habitos: [] };

iniciar();

async function iniciar() {
  configurarAuth();
  configurarNavegacion();
  userName.addEventListener('click', () => cambiarVista('perfil'));
  logoutButton.addEventListener('click', cerrarSesion);
  const tourButton = document.querySelector('#tour-button');
  tourButton.addEventListener('click', () => iniciarTour({
    esAdmin: estado.usuario?.esAdmin || false,
    tieneHabitos: estado.habitos.length > 0,
    cambiarVista
  }));

  try {
    await cargarSesion();
    mostrarApp();
  } catch {
    API.setToken(null);
    mostrarAuth();
  }
}

function configurarAuth() {
  let modo = 'login';

  authToggle.addEventListener('click', () => {
    modo = modo === 'login' ? 'registro' : 'login';
    authCamposRegistro.classList.toggle('hidden', modo !== 'registro');
    authTitle.textContent = modo === 'login' ? 'Inicia sesión' : 'Crea tu cuenta';
    authSubmit.textContent = modo === 'login' ? 'Entrar' : 'Crear cuenta';
    authToggle.textContent = modo === 'login' ? '¿No tienes cuenta? Regístrate' : '¿Ya tienes cuenta? Inicia sesión';
  });

  authForm.addEventListener('submit', async evento => {
    evento.preventDefault();
    const datos = Object.fromEntries(new FormData(authForm));
    ocultarError(authError);
    try {
      const respuesta = modo === 'registro'
        ? await API.registrarse(datos)
        : await API.iniciarSesion(datos);
      API.setToken(respuesta.token);
      await cargarSesion();
      authForm.reset();
      mostrarApp();
    } catch (error) {
      mostrarError(authError, error);
    }
  });
}

function configurarNavegacion() {
  navLinks.forEach(enlace => {
    enlace.addEventListener('click', () => cambiarVista(enlace.dataset.view));
  });
}

async function cargarSesion() {
  estado.usuario = await API.perfil();
  estado.categorias = await API.categorias();
  estado.habitos = await API.habitos();
}

function mostrarApp() {
  authView.classList.add('hidden');
  appView.classList.remove('hidden');
  userName.textContent = `${estado.usuario.nombre} ${estado.usuario.apellido}`;
  navAdmin.classList.toggle('hidden', !estado.usuario.esAdmin);
  cambiarVista('habitos');
  if (!tourVisto()) {
    setTimeout(() => iniciarTour({
      esAdmin: estado.usuario.esAdmin,
      tieneHabitos: estado.habitos.length > 0,
      cambiarVista
    }), 400);
  }
}

function mostrarAuth() {
  appView.classList.add('hidden');
  authView.classList.remove('hidden');
}

function cambiarVista(vista) {
  navLinks.forEach(enlace => enlace.classList.toggle('active', enlace.dataset.view === vista));
  viewHabitos.classList.toggle('hidden', vista !== 'habitos');
  viewProgreso.classList.toggle('hidden', vista !== 'progreso');
  viewPerfil.classList.toggle('hidden', vista !== 'perfil');
  viewAdmin.classList.toggle('hidden', vista !== 'admin');

  if (vista === 'habitos') renderVistaHabitos();
  else if (vista === 'progreso') renderVistaProgreso();
  else if (vista === 'perfil') renderVistaPerfil();
  else if (vista === 'admin') renderVistaAdmin();
}

function colorDe(categoria) {
  const encontrada = estado.categorias.find(c => c.nombre === categoria);
  return encontrada ? encontrada.colorHex : null;
}

function renderVistaHabitos() {
  renderLista(viewHabitos, estado.habitos, colorDe, {
    onNuevo: abrirNuevoHabito,
    onRegistrar: abrirAvance,
    onEliminar: eliminarHabito
  });
}

function abrirNuevoHabito() {
  abrirDialogo('Nuevo hábito', formularioNuevo(estado.categorias));
  const form = document.querySelector('#form-nuevo-habito');
  const bloqueCuantitativo = form.querySelector('.cuantitativo');
  const selectTipo = form.querySelector('select[name="tipoMeta"]');
  const alternar = () => bloqueCuantitativo.classList.toggle('hidden', selectTipo.value !== 'CUANTITATIVO');
  selectTipo.addEventListener('change', alternar);
  alternar();
  inicializarSugerencias(form);

  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    const errorParrafo = form.querySelector('[data-role="error"]');
    ocultarError(errorParrafo);
    try {
      const datos = Object.fromEntries(new FormData(form));
      datos.idCategoria = Number(datos.idCategoria);
      if (datos.tipoMeta === 'BOOLEANO') {
        datos.metaDiaria = null;
        datos.unidadMedida = null;
      } else {
        datos.metaDiaria = Number(datos.metaDiaria);
      }
      const creado = await API.crearHabito(datos);
      estado.habitos.unshift(creado);
      cerrarDialogo();
      renderVistaHabitos();
      mostrarAviso(viewHabitos, 'Hábito creado correctamente.');
    } catch (error) {
      mostrarError(errorParrafo, error);
    }
  });
}

function abrirAvance(habito) {
  abrirDialogo('Registrar avance', formularioAvance(habito));
  const form = document.querySelector('#form-avance');
  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    const errorParrafo = form.querySelector('[data-role="error"]');
    ocultarError(errorParrafo);
    try {
      const datos = Object.fromEntries(new FormData(form));
      const valor = habito.tipoMeta === 'BOOLEANO'
        ? (datos.cumplido ? 1 : 0)
        : Number(datos.valor);
      await API.registrarAvance({ idHabito: habito.id, fecha: datos.fecha, valor, nota: datos.nota || null });
      cerrarDialogo();
      if (!viewProgreso.classList.contains('hidden')) {
        await renderVistaProgreso();
      } else {
        renderVistaHabitos();
      }
      mostrarAviso(viewHabitos, 'Avance registrado.');
    } catch (error) {
      mostrarError(errorParrafo, error);
    }
  });
}

async function eliminarHabito(habito) {
  if (!window.confirm(`¿Eliminar el hábito «${habito.nombre}»? Esta acción no se puede deshacer.`)) return;
  try {
    await API.eliminarHabito(habito.id);
    estado.habitos = estado.habitos.filter(h => h.id !== habito.id);
    renderVistaHabitos();
    mostrarAviso(viewHabitos, 'Hábito eliminado.');
  } catch (error) {
    mostrarAviso(viewHabitos, error.message, true);
  }
}

async function renderVistaProgreso() {
  viewProgreso.innerHTML = '<p class="vacio">Cargando progreso…</p>';
  await renderProgreso(viewProgreso, estado.habitos, API.registrosDeHabito, { onRegistrar: abrirAvance });
}

function renderVistaPerfil() {
  renderPerfil(viewPerfil, estado.usuario, { onGuardar: guardarPerfil });
}

async function renderVistaAdmin() {
  viewAdmin.innerHTML = '<p class="vacio">Cargando reportes…</p>';
  try {
    const reporte = await API.reporteAdmin();
    renderAdmin(viewAdmin, reporte);
  } catch (error) {
    viewAdmin.innerHTML = `<p class="form-error">${reporteError(error)}</p>`;
  }
}

function reporteError(error) {
  return error instanceof ApiError ? error.message : 'No fue posible cargar los reportes.';
}

async function guardarPerfil(datos) {
  const form = document.querySelector('#form-perfil');
  const errorParrafo = form.querySelector('[data-role="error"]');
  ocultarError(errorParrafo);
  try {
    datos.password = datos.password || null;
    const actualizado = await API.actualizarPerfil(datos);
    estado.usuario = actualizado;
    userName.textContent = `${actualizado.nombre} ${actualizado.apellido}`;
    mostrarAviso(viewPerfil, 'Perfil actualizado.');
    form.querySelector('input[name="password"]').value = '';
  } catch (error) {
    mostrarError(errorParrafo, error);
  }
}

async function cerrarSesion() {
  try {
    await API.cerrarSesion();
  } catch {
    // si el token ya no sirve, igual se limpia abajo
  }
  API.setToken(null);
  estado.usuario = null;
  estado.habitos = [];
  mostrarAuth();
}

function mostrarError(elemento, error) {
  elemento.textContent = error instanceof ApiError ? error.message : 'Algo salió mal. Inténtalo de nuevo.';
  elemento.classList.remove('hidden');
}

function ocultarError(elemento) {
  elemento.textContent = '';
  elemento.classList.add('hidden');
}