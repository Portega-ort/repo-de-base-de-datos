const TOKEN_KEY = 'habit_tracker_token';

export class ApiError extends Error {
  constructor(mensaje, status) {
    super(mensaje);
    this.status = status;
  }
}

export const API = {
  get token() {
    return localStorage.getItem(TOKEN_KEY);
  },
  setToken(token) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  },

  async health() {
    return request('/api/health');
  },
  async registrarse(datos) {
    return request('/api/auth/register', { method: 'POST', body: JSON.stringify(datos) });
  },
  async iniciarSesion(datos) {
    return request('/api/auth/login', { method: 'POST', body: JSON.stringify(datos) });
  },
  async cerrarSesion() {
    return request('/api/auth/logout', { method: 'POST' });
  },
  async perfil() {
    return request('/api/auth/me');
  },
  async actualizarPerfil(datos) {
    return request('/api/auth/me', { method: 'PUT', body: JSON.stringify(datos) });
  },
  async categorias() {
    return request('/api/categorias');
  },
  async habitos() {
    return request('/api/habitos');
  },
  async crearHabito(datos) {
    return request('/api/habitos', { method: 'POST', body: JSON.stringify(datos) });
  },
  async actualizarHabito(id, datos) {
    return request(`/api/habitos/${id}`, { method: 'PUT', body: JSON.stringify(datos) });
  },
  async eliminarHabito(id) {
    return request(`/api/habitos/${id}`, { method: 'DELETE' });
  },
  async registrosDeHabito(id) {
    return request(`/api/habitos/${id}/registros`);
  },
  async registrarAvance(datos) {
    return request('/api/registros', { method: 'POST', body: JSON.stringify(datos) });
  },
  async reporteAdmin() {
    return request('/api/admin');
  }
};

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const token = API.token;
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(path, { ...options, headers });

  if (response.status === 401 && token) {
    API.setToken(null);
    window.location.reload();
    throw new ApiError('La sesión terminó. Inicia sesión de nuevo.', 401);
  }
  if (response.status === 204) return null;

  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const mensaje = data && data.error ? data.error : `Error HTTP ${response.status}`;
    throw new ApiError(mensaje, response.status);
  }
  return data;
}