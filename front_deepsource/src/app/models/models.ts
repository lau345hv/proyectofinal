export interface UsuarioDTO {
  id?: number;
  nombre: string;
  apellido: string;
  correo: string;
  nombreUsuario: string;
  contrasena?: string;
  telefono: string;
  roles?: string[];
  rol?: string;
}

export interface HistorialConversionDTO {
  id?: number;
  nombreArchivoOriginal: string;
  formatoOrigen: string;
  formatoDestino: string;
  tipoArchivo: 'AUDIO' | 'VIDEO' | 'IMAGEN';
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'FALLIDO';
  urlDescarga?: string;
  fechaConversion: string;
  usuarioId?: number;
  nombreUsuario?: string;
}

export interface LoginResponse {
  mensaje: string;
  usuario: UsuarioDTO;
}

export interface ResumenAdmin {
  totalUsuarios: number;
  totalConversiones: number;
  conversionesPorTipo?: { [key: string]: number };
  conversionesPorEstado?: { [key: string]: number };
}

export type TipoArchivo = 'AUDIO' | 'VIDEO' | 'IMAGEN';
